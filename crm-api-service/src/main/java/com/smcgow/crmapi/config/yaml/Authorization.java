package com.smcgow.crmapi.config.yaml;

import lombok.Data;

@Data
public class Authorization {

    private String username, password, grants;
}
