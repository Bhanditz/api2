Installation steps:
copy europeana.properties to /api2/api2-war/src/main/resources
mvn clean install
docker-compose up
do inserts of init.sql (creates api2demo/verysecret api key and testuser test@test.com/test)

java/Tomcat debug port = 8000
