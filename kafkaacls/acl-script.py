#!/usr/bin/env python3
import yaml
import subprocess
import os
import re

# Get Kafka broker from environment variable or default
KAFKA_BROKER = os.getenv("KAFKA_BROKER", "localhost:9091")
if not KAFKA_BROKER:
    print("Error: KAFKA_BROKER environment variable is not set.")
    exit(1)
print(f"Using Kafka broker: {KAFKA_BROKER}")

# Path to the YAML file containing the ACL configuration
CONFIG_FILE = "acl-config.yaml"

if not os.path.isfile(CONFIG_FILE):
    print(f"Error: Configuration file {CONFIG_FILE} not found.")
    exit(1)

# Load YAML file
with open(CONFIG_FILE, "r") as file:
    config = yaml.safe_load(file)

# Fetch existing ACLs
check_cmd = [
    "kafka-acls", "--command-config", "client.properties", "--bootstrap-server", KAFKA_BROKER, "--list"
]
result = subprocess.run(check_cmd, capture_output=True, text=True)

# Parse ACLs from output
existing_acls = {}
current_resource = None

for line in result.stdout.splitlines():
    line = line.strip()
    
    # Match resource name from "Current ACLs for resource" line
    match = re.search(r'name=([^,]+), patternType=', line)
    if match:
        current_resource = match.group(1)
    
    # If we have an ACL entry, associate it with the correct resource
    if current_resource and "principal=" in line:
        if current_resource not in existing_acls:
            existing_acls[current_resource] = set()
        existing_acls[current_resource].add(line)

# Process ACLs from YAML
for acl in config.get("kafka-acls", []):
    action = acl.get("action")
    principal = acl.get("allow-principal")
    resource_pattern_type = acl.get("resource-pattern-type", "LITERAL")  # Default to LITERAL
    operations = acl.get("operations", [])
    
    # Determine resource type and name
    if "topic" in acl:
        resource_type = "--topic"
        resource_name = acl["topic"]
    elif "group" in acl:
        resource_type = "--group"
        resource_name = acl["group"]
    else:
        print("Error: ACL must specify either a topic or a group.") # The script ONLY supports topic and group
        continue
    
    for operation in operations:
        acl_string = f"(principal={principal}, host=*, operation={operation}, permissionType=ALLOW)"
        
        if resource_name in existing_acls and any(acl_string in acl for acl in existing_acls[resource_name]):
            print(f"ACL for {operation} on {resource_type} {resource_name} already exists. Skipping.")
        else:
            print(f"Creating ACL for {operation} on {resource_type} {resource_name}.")
            create_cmd = [
                "kafka-acls", "--command-config", "client.properties", "--bootstrap-server", KAFKA_BROKER,
                action, "--allow-principal", principal,
                "--operation", operation,
                resource_type, resource_name,
                "--resource-pattern-type", resource_pattern_type
            ]
            subprocess.run(create_cmd)
