# IPK BrAPI Validator

Test suite for BrAPI servers. Test your server at [http://webapps.ipk-gatersleben.de/brapivalidator/](http://webapps.ipk-gatersleben.de/brapivalidator/).

Under development, not carefully tested. If you find any errors or bugs, please report them as an issue!

## Usage

Requires Maven.

To run locally use:

`mvn jetty:run`

To deploy to tomcat use:

`mvn tomcat7:deploy`

The tomcat server location must be defined as parameters. For example: 

```
appserver.name=production_server
appserver.path=/brapivalidator
appserver.url=http://website.com:1234/manager/text
```
Also remember to add the server user and encrypted password to settings.xml.

Configure the local h2 db user and password, as well as any proxy settings in the config.properties file.

## Docs

This testing suite uses RestAssured for testing.

To validate the responses, JsonSchema is used. All schemas are located in src/test/resources/schemas.

The test configurations are stored in two ways. For the 'simple' tests, where one resource is tested for status code, content type and response schema, are stored in tests.json. 

The complex tests (Test Collections) are stored in the collections/ folder. They roughly follow Postman's collections format. Instead of using JavaScript for the tests, we define them using a few simple commands.

| Command  | Description  | Example  | 
|---|---|---|
|StatusCode:{int} | Check the status code of the response  | StatusCode:200 |
|ContentType:{String} | Check the Content Type of the response  | ContentType:application/json |
|Schema:{path} | Check the response against a Schema located in src/main/resources/schemas | Schema:/metadata |
|GetValue:{jsonpointer}:{variableName} | Stores the value of the response in the pointer location | GetValue:/result/data/0/germplasmDbId:germplasmDbId |
|IsEqual:{jsonpointer}:{variableName} | Checks the value of the response in the pointer location with a stored variable |IsEqual:/result/germplasmDbId:germplasmDbId |
|{command}:{value}:breakiffalse | This flag stops the current endpoint testing if the test is failed. | StatusCode:200:breakiffalse |

## Adding tests

* Add TestCollection file to /collections/
* Add test info to tests.json
* Add test to front end form.

## To-do
* Add FAQ to readme (tests only schema, no parameters.)
* Consider variables from configuration file or decide to ignore them at all.