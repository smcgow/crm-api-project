
### About

This is supposed to be a light microservice that accepts, json, converts it to a soap messsage
and then proxies th soap messsage onto a third party after which the reponse is converted
back to json and returned back to the client application. 
To be used with the kana web services to provide a proxy to json for existing soap web services.

NOTE::This relies on the request and response being formatted without namespace prefixes. It needs 
more work to enable this.

### Running the code

 1. Run gradlew build to build both services.
 1. Open command line and run gradlew country-web-service:bootRun - This runs the SOAP service from the Spring getting started guide on 8081
 2. In a new command line run gradlew crm-api-service:bootRun - This runs the api service on 8080.
 3. Send the following request to receive the following response.

You should be able to use this service for any SOAP service operation. See the application.yml for configuration of various operations. The configuraion
shows the message typss which is needed for the SOAP call but not needed in the resultant JSON.

#### Request

{
   "name": "Spain"
}

#### Response

{"country": {
    "xmlns": "http://spring.io/guides/gs-producing-web-service",
    "capital": "Madrid",
    "name": "Spain",
    "currency": "EUR",
    "population": 46704314
}}

NOTE::Any namespace references in the response will be present when returned. Need additional work to filter this.

### Tomcat

The applicationm is currrently configured as a multi-project gradle project and all artifacts can be deployed to tomcat through
the run configuration. If deploying to tomcat, you will need to update application.yml for the crm-api-service to point to the
correct context that is used on tomcat and possibly you need to update the port too..

End points:
	1. http://[server]:[port]/[crm_api_deployment_name]/v1/country  - The endpoint for the JSON service.
	2. http://[server]:[port]/[country_service_deployment_name]/ws  - The endpoint for the demo SOAP country service.