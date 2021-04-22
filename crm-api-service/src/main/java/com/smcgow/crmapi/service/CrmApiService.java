package com.smcgow.crmapi.service;

import com.smcgow.crmapi.config.yaml.CrmProperties;
import com.smcgow.crmapi.config.yaml.Operation;
import com.smcgow.crmapi.exceptions.AppExceptions;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class CrmApiService {

    public static final String AUTHORIZATION = "Authorization";
    private String soapMessageTemplate =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" %s=\"%s\">"+
            "<soapenv:Header/>\n" +
            "<soapenv:Body>%s</soapenv:Body>" +
            "</soapenv:Envelope>";
    public static final String CONTENT_TYPE = "content-type";

    private CrmProperties crmProperties;

    @Autowired
    public CrmApiService(CrmProperties crmProperties) {
        //Must have this or get an exception using Saaj.
        System.setProperty("javax.xml.soap.MetaFactory","com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl");
        this.crmProperties = crmProperties;
    }

    /**
     * Search the yaml config for the operation configuration specified and sets that as details to use.
     * @param operationName
     * @return
     */
    private Operation getOperation(String operationName){
        Optional<Operation> operationOpt = crmProperties.getOperations().stream().filter(operation -> operation.getName().equals(operationName)).findFirst();
        if(operationOpt.isEmpty()) {
            AppExceptions.throwCrmApplicationConfigurationException("Could not locate operation named " + operationName + " from application properties.");
        }
        return operationOpt.get();
    }

    /**
     * This extracts the json from the request and sets it as a json object for use further down.
     * @param request
     * @return
     */
    private JSONObject extractRequestToJson(HttpServletRequest request){

        String requestBodyJson = null;
        try {
            requestBodyJson = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            AppExceptions.throwJsonMessageParsingException("Could not parse the input stream.",e);
        }
        JSONObject json = new JSONObject(requestBodyJson);
        json = this.addRequestParameters(request,json);
        return json;
    }

    /**
     * This takes the http request paraemeters and places them into the body of the json so they can
     * be picked up by the crm service it is being proxied to.
     * @param request
     * @return
     */
    private JSONObject addRequestParameters(HttpServletRequest request, JSONObject json){

        Enumeration<String> parameterNames = request.getParameterNames();
        if(parameterNames.hasMoreElements()){
            JSONObject parameters = new JSONObject();
            json.put("requestParameters", parameters);
            while(parameterNames.hasMoreElements()){
                String parameterName = parameterNames.nextElement();
                parameters.put(parameterName,request.getParameter(parameterName));
            }
        }
        return json;
    }

    /**
     * Convert the incoming json object to and xml representation using the yaml operation config
     * to define the message. Then, using the template soap message create the soap message as
     * a string using the yaml operation configuration to define the namespace. Once done convert
     * the string to a w3c document and append to the SOAP Part of the soap message.
     * @return
     */
    private SOAPMessage buildSoapRequestMessage(JSONObject json, Operation operation){
        SOAPMessage request = null;
        try {
            String xml = XML.toString(json,operation.getInputMessage());
            String soap = String.format(soapMessageTemplate,operation.getNamespace().getName(), operation.getNamespace().getUrl(), xml);
            request = MessageFactory.newInstance().createMessage();

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(soap));
            Document document = db.parse(is);
            DOMSource domSource = new DOMSource(document);
            SOAPPart part = request.getSOAPPart();
            part.setContent(domSource);
            request.saveChanges();

        } catch (Exception e) {
            AppExceptions.throwJsonMessageParsingException("Exception occured building the soap message fron the original soap request.",e);
        }
        return request;
    }

    /**
     * Create the connection factory and make the call on the a cretaed connection
     * @return
     * @param operation,
     */
    private SOAPMessage makeSoapCall(Operation operation, SOAPMessage request){
        SOAPMessage response = null;
        try {
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection connection = soapConnectionFactory.createConnection();
            URL endpoint = new URL(operation.getUrl());
            response = connection.call(request, endpoint);
            connection.close();

        } catch (Exception e) {
            AppExceptions.throwInvocationOfServiceFailedException("Exception occurred making the service call",e);
        }
        return response;
    }

    /**
     * Takes the response in soap, navigates to the element node that is below the yaml configured
     * response messsage name configuration and converts that element to a json object and string
     * for returning to the callee of the service.
     * @return
     */
    private Optional<JSONObject> handleSoapCallResponse(Operation operation,SOAPMessage response){
        Optional<JSONObject> jsonResponseObjOpt = null;
        try {

            SOAPBody responseBody = response.getSOAPBody();
            Node responseNode = getResponseChildElement(operation,responseBody.getChildNodes());
            DOMSource source = new DOMSource(responseNode);
            StringWriter stringResult = new StringWriter();
            TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
            String xmlResponse = stringResult.toString();

            //7. Convert to a json response string.
            jsonResponseObjOpt = Optional.ofNullable(XML.toJSONObject(xmlResponse));

        } catch (Exception e) {
            AppExceptions.throwJsonMessageParsingException("Exception occured getting the message from the soap response",e);
        }
        return jsonResponseObjOpt;
    }

    /**
     * Recursice method to obtain the tag below the output message.
     * @param childList
     * @return
     */
    private Node getResponseChildElement(Operation operation, NodeList childList) {
        for (int i = 0; i < childList.getLength(); i++) {
            Node item = childList.item(i);
            if(item.getNodeName().equals(operation.getOutputMessage())){
                return item.getFirstChild();
            }else{
                item = getResponseChildElement(operation,item.getChildNodes());
                if(item != null){
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Takes the response in soap, navigates to the element node that is below the yaml configured
     * response messsage name configuration and converts that element to a json object and string
     * for returning to the callee of the service.
     * @return
     */
    public String invoke(String operationName, HttpServletRequest servletRequest){

        Operation operation = this.getOperation(operationName);
        this.authenticateRequest(operation,servletRequest);
        JSONObject requestJson = this.extractRequestToJson(servletRequest);
        SOAPMessage requestSoap = this.buildSoapRequestMessage(requestJson, operation);
        SOAPMessage responseSoap = this.makeSoapCall(operation, requestSoap);
        Optional<JSONObject> jsonObject = this.handleSoapCallResponse(operation, responseSoap);
        if(!jsonObject.isPresent()){
            AppExceptions.throwJsonResponseAbsentException("The resulting json response whas undefined");
        }
        return jsonObject.get().toString(4);
    }

    /**
     * This method will validate the basic authentication header token to ensure the person
     * can access the operation.
     * @param servletRequest
     * @return
     */
    private void authenticateRequest(Operation operation, HttpServletRequest servletRequest) {

        String headerValue = servletRequest.getHeader(AUTHORIZATION);
        if(Objects.isNull(headerValue)){
            AppExceptions.throwInvalidBasicAuthCredentialsException("Could not find authorization header");
        }
        String[] headerTokens = headerValue.split("Basic ");
        if(headerTokens.length != 2){
            AppExceptions.throwInvalidBasicAuthCredentialsException("Could not find Basic auth credentials");
        }

        String originalInput = "test input";
        String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);

        String creds = new String(Base64.getDecoder().decode((String)headerTokens[1]));
        String[] uPassArr = creds.split(":");
        if(uPassArr.length != 2){
            AppExceptions.throwInvalidBasicAuthCredentialsException("Invalid credentials in basic auth request");
        }
        this.checkLoginAndPassword(operation,uPassArr[0],uPassArr[1]);
    }

    /**
     * Checks the configuration for the operation to see if that username/pair has access.
     * @param username
     * @param password
     */
    private void checkLoginAndPassword(Operation operation,String username, String password) {
        Boolean found = operation.getAuthorizations().stream().anyMatch(authorization ->
                authorization.getUsername().equals(username) && authorization.getPassword().equals(password));
        if(!found){
            AppExceptions.throwInvalidBasicAuthCredentialsException("Could not match the basic username and password with the config for the operation " + operation.getName());
        }
    }


}
