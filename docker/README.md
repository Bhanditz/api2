Installation steps:
- copy europeana.properties to /api2/api2-war/src/main/resources (this step can be skipped if we would commit the file to git)
- mvn clean install (this step can be skipped if see above)
- Go to the docker folder and execute the command: docker-compose up
- do inserts of init.sql. This creates api2demo/verysecret api key and testuser test@test.com/test (this step can be automated with hibernate).

java/Tomcat debug port = 8000
