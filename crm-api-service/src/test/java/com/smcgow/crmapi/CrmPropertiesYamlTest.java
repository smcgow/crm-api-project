package com.smcgow.crmapi;

import com.smcgow.crmapi.config.yaml.CrmProperties;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = CrmProperties.class)
public class CrmPropertiesYamlTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private CrmProperties crmProperties;

    @Test
    public void verifyCorrectNumberOperations(){
        assertNotNull(crmProperties.getOperations(), "The operaions configuration came back undefined.");
        Integer correctNumber = 2;
        assertTrue(crmProperties.getOperations().size() == correctNumber,"Expected " + correctNumber + " operations but found more or less");
    }

    @Test
    public void checkPersonAndCountryOperation(){
        assertNotNull(crmProperties.getOperations(), "The operaions configuration came back undefined.");
        assertTrue(crmProperties.getOperations().get(0).getName().equals("country"),"Expected country operation name but did not find it at element 2");
        assertTrue(crmProperties.getOperations().get(1).getName().equals("person"),"Expected person operation name but did not find it at element 2");
    }
}
