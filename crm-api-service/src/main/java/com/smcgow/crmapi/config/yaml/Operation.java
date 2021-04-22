package com.smcgow.crmapi.config.yaml;

import lombok.Data;

import java.util.List;

@Data
public class Operation {

    String name;
    String inputMessage;
    String outputMessage;
    String url;
    Namespace namespace;
    List<Authorization> authorizations;

}
