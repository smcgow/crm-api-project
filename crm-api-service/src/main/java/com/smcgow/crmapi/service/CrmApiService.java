package com.smcgow.crmapi.service;

import com.smcgow.crmapi.config.yaml.CrmProperties;
import com.smcgow.crmapi.config.yaml.Operation;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
import java.util.Enumeration;
import java.util.Optional;

@Service
@Slf4j
public class CrmApiService {

    private JSONObject jsonResponseObject = null;
    private SOAPConnection connection = null;
    private String jsonResponse = null;
    private String soapMessageTemplate =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" %s=\"%s\">"+
            "<soapenv:Header/>\n" +
            "<soapenv:Body>%s</soapenv:Body>" +
            "</soapenv:Envelope>";
    public static final String CONTENT_TYPE = "content-type";
    private String requestBodyJson = null;
    private JSONObject json = null;
    SOAPMessage response = null;
    SOAPMessage request = null;
    Operation operation = null;
    String webServiceUrl = null;

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
    private CrmApiService useOperation(String operationName){
        Optional<Operation> operationOpt = crmProperties.getOperations().stream().filter(operation -> operation.getName().equals(operationName)).findFirst();
        if(operationOpt.isEmpty()){
            handleServiceError("Could not locate operation named " + operationName + " from application properties.");
        }
        operation = operationOpt.get();
        webServiceUrl = operation.getUrl();
        return this;
    }

    /**
     * This extracts the json from the request and sets it as a json object for use further down.
     * @param request
     * @return
     */
    private CrmApiService extractRequestToJson(HttpServletRequest request){

        try {
            requestBodyJson = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        json = new JSONObject(requestBodyJson);
        return this;
    }

    /**
     * This takes the http request paraemeters and places them into the body of the json so they can
     * be picked up by the crm service it is being proxied to.
     * @param request
     * @return
     */
    private CrmApiService addRequestParameters(HttpServletRequest request){

        Enumeration<String> parameterNames = request.getParameterNames();
        if(parameterNames.hasMoreElements()){
            JSONObject parameters = new JSONObject();
            json.put("requestParameters", parameters);
            while(parameterNames.hasMoreElements()){
                String parameterName = parameterNames.nextElement();
                parameters.put(parameterName,request.getParameter(parameterName));
            }
        }
        return this;
    }

    /**
     * Convert the incoming json object to and xml representation using the yaml operation config
     * to define the message. Then, using the template soap message create the soap message as
     * a string using the yaml operation configuration to define the namespace. Once done convert
     * the string to a w3c document and append to the SOAP Part of the soap message.
     * @return
     */
    private CrmApiService buildSoapRequestMessage(){
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
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Create the connection factory and make the call on the a cretaed connection
     * @return
     */
    private CrmApiService makeSoapCall(){
        try {

            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            connection = soapConnectionFactory.createConnection();
            URL endpoint = new URL(webServiceUrl);
            response = connection.call(request, endpoint);
            connection.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Takes the response in soap, navigates to the element node that is below the yaml configured
     * response messsage name configuration and converts that element to a json object and string
     * for returning to the callee of the service.
     * @return
     */
    private CrmApiService handleSoapCallResponse(){
        try {

            SOAPBody responseBody = response.getSOAPBody();
            Node responseNode = getResponseChildElement(responseBody.getChildNodes());
            DOMSource source = new DOMSource(responseNode);
            StringWriter stringResult = new StringWriter();
            TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
            String xmlResponse = stringResult.toString();

            //7. Convert to a json response string.
            jsonResponseObject = XML.toJSONObject(xmlResponse);
            jsonResponse = jsonResponseObject.toString(4);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Recursice method to obtain the tag below the output message.
     * @param childList
     * @return
     */
    private Node getResponseChildElement(NodeList childList) {
        for (int i = 0; i < childList.getLength(); i++) {
            Node item = childList.item(i);
            if(item.getNodeName().equals(operation.getOutputMessage())){
                return item.getFirstChild();
            }else{
                item = getResponseChildElement(item.getChildNodes());
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
        try {

            this.useOperation(operationName)
                    .extractRequestToJson(servletRequest)
                    .addRequestParameters(servletRequest)
                    .buildSoapRequestMessage()
                    .makeSoapCall()
                    .handleSoapCallResponse();

            return this.jsonResponse;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }



    private void handleServiceError(String message) {
        log.error(message);
        throw new RuntimeException(message);
    }
}
