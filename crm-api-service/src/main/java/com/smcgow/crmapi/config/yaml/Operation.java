package com.smcgow.crmapi.config.yaml;

import lombok.Data;

@Data
public class Operation {

    String name;
    String inputMessage;
    String outputMessage;
    String url;
    Namespace namespace;

}
