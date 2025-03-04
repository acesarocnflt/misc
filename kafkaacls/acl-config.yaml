#!/bin/bash

set -e

# Path to the YAML file containing ACL configuration
CONFIG_FILE="acl-config.yaml"

# Function to get the value of a YAML field
get_yaml_value() {
    local key=$1
    grep "^\s*$key:" "$CONFIG_FILE" | awk -F ': ' '{print $2}'
}

# Extract parameters from the YAML file
action=$(get_yaml_value "kafka-acls.action")
principal=$(get_yaml_value "kafka-acls.allow-principal")
transactional_id=$(get_yaml_value "kafka-acls.transactional-id")
resource_pattern_type=$(get_yaml_value "kafka-acls.resource-pattern-type")
operations=($(grep "^\s*-" "$CONFIG_FILE" | awk '{print $2}'))

# Check if the ACL already exists
for operation in "${operations[@]}"; do
    if kafka-acls.sh --bootstrap-server <KAFKA_BROKER> --list --transactional-id "$transactional_id" | grep -q "$operation"; then
        echo "ACL for $operation on $transactional_id already exists. Skipping."
    else
        echo "Creating ACL for $operation on $transactional_id."
        kafka-acls.sh --bootstrap-server <KAFKA_BROKER> "$action" --allow-principal "$principal" \
            --operation "$operation" --transactional-id "$transactional_id" \
            --resource-pattern-type "$resource_pattern_type"
    fi
done
