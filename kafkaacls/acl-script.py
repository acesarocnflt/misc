#!/usr/bin/env python3
import yaml
import subprocess
import os

# Get Kafka broker from environment variable or default
# Exit with code 1 if not set and print its variable
KAFKA_BROKER = os.getenv("KAFKA_BROKER", "kafka.confluent.svc.cluster.local:9092")
if not KAFKA_BROKER:
    print("Error: KAFKA_BROKER environment variable is not set.")
    exit(1)
print(f"Using Kafka broker: {KAFKA_BROKER}")

# Path to the YAML file containing the ACL configuration
CONFIG_FILE = "/config/acl-config.yaml"

if not os.path.isfile(CONFIG_FILE):
    print(f"Error: Configuration file {CONFIG_FILE} not found.")
    exit(1)

# Load YAML file
with open(CONFIG_FILE, "r") as file:
    config = yaml.safe_load(file)

# Process each ACL entry
for acl in config.get("kafka-acls", []):
    action = acl.get("action")
    principal = acl.get("allow-principal")
    transactional_id = acl.get("transactional-id")
    resource_pattern_type = acl.get("resource-pattern-type")
    operations = acl.get("operations", [])

    for operation in operations:
        # Check if ACL already exists
        check_cmd = [
            "kafka-acls", "--bootstrap-server", KAFKA_BROKER,
            "--list", "--transactional-id", transactional_id
        ]
        result = subprocess.run(check_cmd, capture_output=True, text=True)

        acl_exists = False
        for line in result.stdout.splitlines():
            if (f"operation={operation}" in line and 
                f"principal={principal}" in line and 
                f"resourceType=TRANSACTIONAL_ID" in line and 
                f"name={transactional_id}" in line):
                acl_exists = True
                break  # If find ACL go out from the loop
        if acl_exists:
            print(f"ACL for {operation} on {transactional_id} already exists. Skipping.")            
        # if operation in result.stdout:
        #     print(f"ACL for {operation} on {transactional_id} already exists. Skipping.")
        else:
            print(f"Creating ACL for {operation} on {transactional_id}.")
            create_cmd = [
                "kafka-acls", "--bootstrap-server", KAFKA_BROKER,
                action, "--allow-principal", principal,
                "--operation", operation,
                "--transactional-id", transactional_id,
                "--resource-pattern-type", resource_pattern_type
            ]
            subprocess.run(create_cmd)