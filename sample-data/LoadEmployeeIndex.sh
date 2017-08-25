# Delete the old schema
curl -XDELETE 'localhost:9200/companydatabase'

# Define the schema
curl -XPUT 'localhost:9200/companydatabase?pretty' -H 'Content-Type: application/json' -d' {"mappings" : { "employees" : { "properties" : { "FirstName" : { "type" : "keyword" }, "LastName" : { "type" : "keyword" }, "Designation" : { "type" : "keyword" }, "Salary" : { "type" : "integer" }, "DateOfJoining" : { "type" : "date", "format": "yyyy-MM-dd" }, "Address" : { "type" : "text" }, "Gender" : { "type" : "keyword" }, "Age" : { "type" : "integer" }, "MaritalStatus" : { "type" : "keyword" }, "Interests" : { "type" : "text" }}}}}'

# Load the data
curl -XPUT 'localhost:9200/companydatabase/_bulk' --data-binary @Employees.json
