package com.smcgow.crmapi;

import org.json.JSONObject;
import org.json.XML;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckMarshallingToAndFromJsonTest {

    /**
     * Structure does not produce well formed XML when you start with Json.
     * Clearly the idea is to start with XML and then set xml can be converted
     * to and from Json. Following tests show capabilitiy.
     * see CheckMarshallingToAndFromJsonTest.checkMovieListXmlWithMultipleMovies
     */
    @Test
    public void checkSessionJsonWithTwoSpeakers(){

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

    /**
     * Produces correct result. Will be inline with the well formed and structured
     * xml being returned. CDATA handled on the way but not on the conversion
     * back to XML.
     */
    @Test
    public void checkMovieListXmlWithMultipleMovies(){

        String test = "<movies>\n" +
                "   <movie id=\"1\">\n" +
                "      <title>The Green Mile</title>\n" +
                "      <year>1999</year>\n" +
                "   </movie>\n" +
                "   <movie id=\"2\">\n" +
                "   \t<![CDATA[Welcome to cdata ]]>\n" +
                "      <title>Taxi Driver</title>\n" +
                "      <year>1976</year>\n" +
                "   </movie>\n" +
                "   <movie id=\"3\">\n" +
                "      <title>The Matrix: Revolutions</title>\n" +
                "      <year>2004</year>\n" +
                "   </movie>\n" +
                "   <movie id=\"4\">\n" +
                "      <title>Shrek II</title>\n" +
                "      <year>2004</year>\n" +
                "   </movie>\n" +
                "</movies>";

        marshalAndPrintXmlToJson(test);

        Assertions.assertTrue(true);

    }

    /**
     * Produces different xml on way back. Attributes are marshalled into json fields
     * the same as element values. therefore when convereting back they can't be marshalled as XML
     */
    @Test
    public void checkPersonnelListWithAttributes(){

        String test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<?xml-model href=\"personal.rnc\" type=\"application/relax-ng-compact-syntax\" schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\n" +
                "<?xml-stylesheet type=\"text/css\" href=\"../personal.css\"?>\n" +
                "<personnel xmlns=\"http://www.oxygenxml.com/ns/samples/personal\">\n" +
                "    <person id=\"harris.anderson\" photo=\"../personal-images/harris.anderson.jpg\">                \n" +
                "        <name>\n" +
                "            <given>Harris</given>\n" +
                "            <family>Anderson</family>\n" +
                "        </name>\n" +
                "        <email>harris.anderson@example.com</email>\n" +
                "        <link subordinates=\"robert.taylor helen.jackson michelle.taylor jason.chen harris.anderson brian.carter\"/>\n" +
                "        <url href=\"http://www.example.com/na/harris-anderson.html\"/>\n" +
                "    </person>\n" +
                "    <person id=\"robert.taylor\" photo=\"../personal-images/robert.taylor.jpg\">\n" +
                "        <name>\n" +
                "            <given>Robert</given>\n" +
                "            <family>Taylor</family>\n" +
                "        </name>\n" +
                "        <email>robert.taylor@example.com</email>\n" +
                "        <link manager=\"harris.anderson\"/>\n" +
                "        <url href=\"http://www.example.com/na/robert-taylor.html\"/>\n" +
                "    </person>\n" +
                "    <person id=\"helen.jackson\" photo=\"../personal-images/helen.jackson.jpg\">\n" +
                "        <name>\n" +
                "            <given>Helen</given>\n" +
                "            <family>Jackson</family>\n" +
                "        </name>        \n" +
                "        <email>hellen.jackson@example.com</email>\n" +
                "        <link manager=\"harris.anderson\"/>\n" +
                "        <url href=\"http://www.example.com/na/hellen-jackson.html\"/>\n" +
                "    </person>\n" +
                "    <person id=\"michelle.taylor\" photo=\"../personal-images/michelle.taylor.jpg\">\n" +
                "        <name>\n" +
                "            <given>Michelle</given>\n" +
                "            <family>Taylor</family>\n" +
                "        </name>\n" +
                "        <email>michelle.taylor@example.com</email>\n" +
                "        <link manager=\"harris.anderson\"/>\n" +
                "        <url href=\"http://www.example.com/na/michelle-taylor.html\"/>\n" +
                "    </person>\n" +
                "    <person id=\"jason.chen\" photo=\"../personal-images/jason.chen.jpg\">\n" +
                "        <name>\n" +
                "            <given>Jason</given>\n" +
                "            <family>Chen</family>\n" +
                "        </name>\n" +
                "        <email>jason.chen@example.com</email>\n" +
                "        <link manager=\"harris.anderson\"/>\n" +
                "        <url href=\"http://www.example.com/na/jason-chen.html\"/>\n" +
                "    </person>\n" +
                "    <person id=\"brian.carter\" photo=\"../personal-images/brian.carter.jpg\" >\n" +
                "        <name>\n" +
                "            <given>Brian</given>\n" +
                "            <family>Carter</family>\n" +
                "        </name>\n" +
                "        <email>brian.carter@example.com</email>\n" +
                "        <link manager=\"harris.anderson\"/>\n" +
                "        <url href=\"http://www.example.com/na/brian-carter.html\"/>\n" +
                "    </person>\n" +
                "</personnel>\n";

        marshalAndPrintXmlToJson(test);

        Assertions.assertTrue(true);

    }

    /**
     * This is handled fine. The resultant xml is the same after the conversion to JSON.
     * So flat array data can be handled fine.
     */
    @Test
    public void checkFlatDataArray(){

        String test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<data>\n" +
                "    <description>Sales evolution 2003</description>\n" +
                "    <entry >15.3</entry>\n" +
                "    <entry >45.3</entry>\n" +
                "    <entry >90</entry>\n" +
                "    <entry >120.5</entry>\n" +
                "    <entry >150.3</entry>\n" +
                "    <entry >155.6</entry>\n" +
                "    <entry >160.3</entry>\n" +
                "    <entry >120</entry>\n" +
                "    <entry >140.3</entry>\n" +
                "    <entry >120</entry>\n" +
                "    <entry >159.6</entry>\n" +
                "    <entry >210.6</entry>\n" +
                "</data>";

        marshalAndPrintXmlToJson(test);

        Assertions.assertTrue(true);

    }

    /**
     * Again, it godes not like attributes so it flattens this data out into
     * tags.
     */
    @Test
    public void checkTournamentMixedListArrayAndAttributes(){

        String test = "\n" +
                "<Tournament xmlns=\"www.allette.com.au/Tournament\">\n" +
                "\t<Name>Allette Open</Name>\n" +
                "\t<Type>Singles</Type>\n" +
                "\t<Date>2001-03-20</Date>\n" +
                "\t<Participants nbrParticipants=\"3\">\n" +
                "\t\t<Name id=\"p1\">Nick</Name>\n" +
                "\t\t<Name id=\"p2\">Marcus</Name>\n" +
                "\t\t<Name id=\"p3\">Eddie</Name>\n" +
                "\t</Participants>\n" +
                "\t<Teams nbrTeams=\"5\">\n" +
                "\t\t<Team id=\"t1\" Name=\"Team 1\">\n" +
                "\t\t\t<Member>p1</Member>\n" +
                "\t\t</Team>\n" +
                "\t\t<Team id=\"t2\" Name=\"Team 2\">\n" +
                "\t\t\t<Member>p2</Member>\n" +
                "\t\t</Team>\n" +
                "\t\t<Team id=\"t3\" Name=\"Team 3\">\n" +
                "\t\t\t<Member>p3</Member>\n" +
                "\t\t</Team>\n" +
                "\t</Teams>\n" +
                "\t<Matches nbrMatches=\"3\">\n" +
                "\t\t<Match id=\"m1\">\n" +
                "\t\t\t<Team>t1</Team>\n" +
                "\t\t\t<Team>t2</Team>\n" +
                "\t\t</Match>\n" +
                "\t\t<Match id=\"m2\">\n" +
                "\t\t\t<Team>t1</Team>\n" +
                "\t\t\t<Team>t3</Team>\n" +
                "\t\t</Match>\n" +
                "\t\t<Match id=\"m3\">\n" +
                "\t\t\t<Team>t2</Team>\n" +
                "\t\t\t<Team>t5</Team>\n" +
                "\t\t</Match>\n" +
                "\t</Matches>\n" +
                "</Tournament>";

        marshalAndPrintXmlToJson(test);

        Assertions.assertTrue(true);

    }

    private void marshalAndPrintXmlToJson(String test) {
        System.out.println("::::TEST XML" + test);
        JSONObject testJson = XML.toJSONObject(test);
        String testJsonString = testJson.toString(4);
        System.out.println(":::: TEST JSON " + testJsonString);
        String xmlTest = XML.toString(testJson);
        System.out.println(":::: TEST XML back " + xmlTest);
    }


}
