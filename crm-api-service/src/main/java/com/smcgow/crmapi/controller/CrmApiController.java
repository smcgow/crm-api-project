package com.smcgow.crmapi.controller;

import javax.xml.namespace.QName;
import javax.xml.soap.*;

import com.smcgow.crmapi.config.yaml.CrmProperties;
import com.smcgow.crmapi.config.yaml.Operation;
import com.smcgow.crmapi.service.CrmApiService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@RestController
@RequestMapping("/crm")
@Slf4j
public class CrmApiController {

    public static final String CONTENT_TYPE = "content-type";

    private CrmProperties crmProperties;

    private static final String DEFAULT_ERR = "{\"code\" : \"500\", \"message\" :  \"Internal server error\"}";

    @Autowired
    public CrmApiController(CrmProperties crmProperties) {
        this.crmProperties = crmProperties;
    }

    @Autowired
    private CrmApiService apiService;

    @PostMapping(value = "/api/{operationName}", consumes = {APPLICATION_JSON_VALUE,APPLICATION_XML_VALUE}, produces = APPLICATION_XML_VALUE)
    public ResponseEntity<String> postToCrmApi(@PathVariable(name = "operationName") String operationName, HttpServletRequest request) throws IOException {
        String json = null;
        try{
            json = apiService.invoke(operationName,request);
        } catch (Exception e) {
            log.error("Call to api service failed in the controller",e);
            return new ResponseEntity<>(DEFAULT_ERR,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        ResponseEntity<String> response = new ResponseEntity(json, HttpStatus.OK);
        return response;
    }

        @PostMapping(value = "/api2/{operationName}", consumes = {APPLICATION_JSON_VALUE,APPLICATION_XML_VALUE}, produces = APPLICATION_XML_VALUE)
    public String postToCrmApi2(@PathVariable(name = "operationName") String operationName, HttpServletRequest request) throws IOException {

        JSONObject jsonResponseObject = null;
        SOAPConnection connection = null;

        //Must have this or get an exception using Saaj.
        System.setProperty("javax.xml.soap.MetaFactory","com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl");

        //Default server error on exception.
        String jsonResponse =
                "{\"code\" : \"500\", \"message\" :  \"Internal server error\"}";

        //Default
        String soap = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" %s=\"%s\">"+
                  "<soapenv:Header/>\n" +
                "<soapenv:Body>%s</soapenv:Body>" +
                "</soapenv:Envelope>";

        //Find operation
        Optional<Operation> operationOpt = crmProperties.getOperations().stream().filter(operation -> operation.getName().equals(operationName)).findFirst();
        if(operationOpt.isEmpty()){
            log.error("Could not locate operation named " + operationName + " from application properties.");
            return jsonResponse;
        }
        Operation operation = operationOpt.get();
        String webServiceUrl = operation.getUrl();
        log.info("The operation is " + operation);

        try {
            //1. Take servlet random servlet json input, change to a string and make a Json object.
             //   - Add request araemeters to the body.
            //    - Use configuration to set the message name.
            String requestBodyJson = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            log.info("Greet incoming in json is : " + requestBodyJson);
            JSONObject json = new JSONObject(requestBodyJson);
            Enumeration<String> parameterNames = request.getParameterNames();
            if(parameterNames.hasMoreElements()){
                JSONObject parameters = new JSONObject();
                json.put("requestParameters", parameters);
                while(parameterNames.hasMoreElements()){
                    String parameterName = parameterNames.nextElement();
                    parameters.put(parameterName,request.getParameter(parameterName));
                }
            }

            //2. Convert to XML and wrap in SOAP envelope. Print results
            String xml = XML.toString(json,operation.getInputMessage());
            log.info("Greet incoming in xml is : "+ xml);
            soap = String.format(soap,operation.getNamespace().getName(), operation.getNamespace().getUrl(), xml);
            log.info("Greet incoming in soap is : "+ soap);
            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();

            //4. Convert the soap request string to a w3c document and append as a Soap Part to the message.
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(soap));
            Document document = db.parse(is);
            DOMSource domSource = new DOMSource(document);
            SOAPPart part = soapMessage.getSOAPPart();
            part.setContent(domSource);
            soapMessage.saveChanges();

            //5 Create the connection and make the call.
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            connection = soapConnectionFactory.createConnection();
            URL endpoint = new URL(webServiceUrl);
            SOAPMessage response = connection.call(soapMessage, endpoint);
            connection.close();

            //6. Get the reposnes and convert it back to a string,

            //responseBody.getChildNodes().item(0).getNodeName()
            //responseBody.getChildNodes().item(0).getFirstChild()
            SOAPBody responseBody = response.getSOAPBody();
            DOMSource source = new DOMSource(responseBody.getChildNodes().item(0).getFirstChild());
            StringWriter stringResult = new StringWriter();
            TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
            String xmlResponse = stringResult.toString();

            //7. Convert to a json response string.
            jsonResponseObject = XML.toJSONObject(xmlResponse);
            jsonResponse = jsonResponseObject.toString(4);

        } catch (SOAPException | ParserConfigurationException | SAXException | TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } finally {

        }
        String xmlResponse;


        return jsonResponse;
    }
}
