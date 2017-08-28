# Delete the old schema
curl -XDELETE 'localhost:9200/companydatabase'

# Define the schema
curl -XPUT 'localhost:9200/companydatabase?pretty' -H 'Content-Type: application/json' -d'{"settings":{"analysis":{"normalizer":{"text_keyword_normalizer":{"type":"custom","char_filter":[{"punctuation_remover":{"type":"mapping","mappings":[" =>",".=>",",=>","-=>","_=>"]}},{"type":"pattern_replace","pattern":"Female","replacement":"F"}],"filter":["lowercase"]}}}},"mappings":{"employees":{"properties":{"FirstName":{"type":"keyword","normalizer":"text_keyword_normalizer"},"LastName":{"type":"keyword","normalizer":"text_keyword_normalizer"},"Designation":{"type":"keyword","normalizer":"text_keyword_normalizer"},"Salary":{"type":"integer"},"DateOfJoining":{"type":"date","format":"yyyy-MM-dd"},"Address":{"type":"text"},"Gender":{"type":"keyword","normalizer":"text_keyword_normalizer"},"Age":{"type":"integer"},"MaritalStatus":{"type":"keyword","normalizer":"text_keyword_normalizer"},"Interests":{"type":"text"}}}}}'

# Load the data
curl -XPUT 'localhost:9200/companydatabase/_bulk' --data-binary @Employees.json
