package com.smcgow.crmapi;

import com.smcgow.crmapi.service.CrmApiService;
import org.json.JSONObject;
import org.json.XML;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class CheckMarshallingToAndFromJsonTest {

    @Test
    public void checkArrayTwoValues(){

        String test = "{\n" +
                "     \"sessionName\": \"An initro into java multi-threading\",\n" +
                "    \"sessionDescription\": \"for advanced java programmers\",\n" +
                "    \"sessionLength\": 60,\n" +
                "    \"speakers\": [\n" +
                "        {\n" +
                "            \"speakerId\": 44,\n" +
                "            \"firstName\": \"Stepohen\",\n" +
                "            \"lastName\": \"McGowan\",\n" +
                "            \"title\": \"Senior Developer\",\n" +
                "            \"company\": \"Vhi Ireland\",\n" +
                "            \"speakerBio\": \"Japa Course\",\n" +
                "            \"speakerPhoto\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"speakerId\": 44,\n" +
                "            \"firstName\": \"Aoife\",\n" +
                "            \"lastName\": \"Walsh\",\n" +
                "            \"title\": \"Senior Developer\",\n" +
                "            \"company\": \"Vhi Ireland\",\n" +
                "            \"speakerBio\": \"Japa Course\",\n" +
                "            \"speakerPhoto\": null\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        JSONObject testJson = new JSONObject(test);
        String xmlTest = XML.toString(testJson,"myoperation");
        System.out.println("::::TEST XML" + xmlTest);
        testJson = XML.toJSONObject(xmlTest);
        String testJsonString = testJson.toString(4);
        System.out.println(":::: TEST JSON " + testJsonString);

        Assertions.assertTrue(true);

    }
}
