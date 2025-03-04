kafka-acls:
  action: "--add"
  allow-principal: "User:team1"
  operations:
    - "WRITE"
    - "DESCRIBE"
  transactional-id: "team1-streams-app1"
  resource-pattern-type: "prefixed"
