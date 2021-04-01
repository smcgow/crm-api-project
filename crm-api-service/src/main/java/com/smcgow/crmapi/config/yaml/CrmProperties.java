package com.smcgow.crmapi.config.yaml;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "crm")
public class CrmProperties {

    List<Operation> operations;


}
