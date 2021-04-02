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
@RequestMapping("/")
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

    @PostMapping(value = "/v1/{operationName}", consumes = {APPLICATION_JSON_VALUE,APPLICATION_XML_VALUE}, produces = APPLICATION_XML_VALUE)
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


}
