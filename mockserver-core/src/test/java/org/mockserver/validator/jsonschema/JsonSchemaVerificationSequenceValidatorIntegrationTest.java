package org.mockserver.validator.jsonschema;

import org.junit.Test;
import org.mockserver.logging.MockServerLogger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockserver.character.Character.NEW_LINE;
import static org.mockserver.validator.jsonschema.JsonSchemaValidator.OPEN_API_SPECIFICATION_URL;

/**
 * @author jamesdbloom
 */
public class JsonSchemaVerificationSequenceValidatorIntegrationTest {

    private final JsonSchemaValidator jsonSchemaValidator = new JsonSchemaVerificationSequenceValidator(new MockServerLogger());

    @Test
    public void shouldValidateValidCompleteRequestWithStringBody() {
        // when

        assertThat(jsonSchemaValidator.isValid("{ \"httpRequests\": [" + NEW_LINE +
            "  {" + NEW_LINE +
            "    \"method\" : \"someMethod\"," + NEW_LINE +
            "    \"path\" : \"somePath\"," + NEW_LINE +
            "    \"queryStringParameters\" : [ {" + NEW_LINE +
            "      \"name\" : \"queryStringParameterNameOne\"," + NEW_LINE +
            "      \"values\" : [ \"queryStringParameterValueOne_One\", \"queryStringParameterValueOne_Two\" ]" + NEW_LINE +
            "    }, {" + NEW_LINE +
            "      \"name\" : \"queryStringParameterNameTwo\"," + NEW_LINE +
            "      \"values\" : [ \"queryStringParameterValueTwo_One\" ]" + NEW_LINE +
            "    } ]," + NEW_LINE +
            "    \"body\" : {" + NEW_LINE +
            "      \"type\" : \"STRING\"," + NEW_LINE +
            "      \"string\" : \"someBody\"" + NEW_LINE +
            "    }," + NEW_LINE +
            "    \"cookies\" : [ {" + NEW_LINE +
            "      \"name\" : \"someCookieName\"," + NEW_LINE +
            "      \"value\" : \"someCookieValue\"" + NEW_LINE +
            "    } ]," + NEW_LINE +
            "    \"headers\" : [ {" + NEW_LINE +
            "      \"name\" : \"someHeaderName\"," + NEW_LINE +
            "      \"values\" : [ \"someHeaderValue\" ]" + NEW_LINE +
            "    } ]" + NEW_LINE +
            "  }," +
            "  {" + NEW_LINE +
            "    \"method\" : \"someMethod\"," + NEW_LINE +
            "    \"path\" : \"somePath\"," + NEW_LINE +
            "    \"queryStringParameters\" : [ {" + NEW_LINE +
            "      \"name\" : \"queryStringParameterNameOne\"," + NEW_LINE +
            "      \"values\" : [ \"queryStringParameterValueOne_One\", \"queryStringParameterValueOne_Two\" ]" + NEW_LINE +
            "    }, {" + NEW_LINE +
            "      \"name\" : \"queryStringParameterNameTwo\"," + NEW_LINE +
            "      \"values\" : [ \"queryStringParameterValueTwo_One\" ]" + NEW_LINE +
            "    } ]," + NEW_LINE +
            "    \"body\" : {" + NEW_LINE +
            "      \"type\" : \"STRING\"," + NEW_LINE +
            "      \"string\" : \"someBody\"" + NEW_LINE +
            "    }," + NEW_LINE +
            "    \"cookies\" : [ {" + NEW_LINE +
            "      \"name\" : \"someCookieName\"," + NEW_LINE +
            "      \"value\" : \"someCookieValue\"" + NEW_LINE +
            "    } ]," + NEW_LINE +
            "    \"headers\" : [ {" + NEW_LINE +
            "      \"name\" : \"someHeaderName\"," + NEW_LINE +
            "      \"values\" : [ \"someHeaderValue\" ]" + NEW_LINE +
            "    } ]" + NEW_LINE +
            "  }" + NEW_LINE +
            "]}"), is(""));
    }

    @Test
    public void shouldValidateInvalidBodyType() {
        // when
        assertThat(jsonSchemaValidator.isValid("{ \"httpRequests\": [" + NEW_LINE +
                "  {" + NEW_LINE +
                "    \"body\" : 1" + NEW_LINE +
                "  }" + NEW_LINE +
                "]}"),
            is(
                "2 errors:" + NEW_LINE +
                    " - for field \"/httpRequests/0/body\" a plain string, JSON object or one of the following example bodies must be specified " + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"BINARY\"," + NEW_LINE +
                    "     \"base64Bytes\": \"\"," + NEW_LINE +
                    "     \"contentType\": \"\"" + NEW_LINE +
                    "   }, " + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"JSON\"," + NEW_LINE +
                    "     \"json\": \"\"," + NEW_LINE +
                    "     \"contentType\": \"\"," + NEW_LINE +
                    "     \"matchType\": \"ONLY_MATCHING_FIELDS\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"JSON_SCHEMA\"," + NEW_LINE +
                    "     \"jsonSchema\": \"\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"JSON_PATH\"," + NEW_LINE +
                    "     \"jsonPath\": \"\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"PARAMETERS\"," + NEW_LINE +
                    "     \"parameters\": {\"name\": \"value\"}" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"REGEX\"," + NEW_LINE +
                    "     \"regex\": \"\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"STRING\"," + NEW_LINE +
                    "     \"string\": \"\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"XML\"," + NEW_LINE +
                    "     \"xml\": \"\"," + NEW_LINE +
                    "     \"contentType\": \"\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"XML_SCHEMA\"," + NEW_LINE +
                    "     \"xmlSchema\": \"\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"XPATH\"," + NEW_LINE +
                    "     \"xpath\": \"\"" + NEW_LINE +
                    "   }" + NEW_LINE +
                    " - instance failed to match at least one required schema among 13 for field \"/httpRequests/0/body\"" + NEW_LINE +
                    NEW_LINE +
                    OPEN_API_SPECIFICATION_URL
            ));
    }

    @Test
    public void shouldValidateInvalidExtraField() {
        // when
        assertThat(jsonSchemaValidator.isValid("{ \"httpRequests\": [" + NEW_LINE +
                "  {" + NEW_LINE +
                "    \"invalidField\" : {" + NEW_LINE +
                "      \"type\" : \"STRING\"," + NEW_LINE +
                "      \"value\" : \"someBody\"" + NEW_LINE +
                "    }" + NEW_LINE +
                "  }" + NEW_LINE +
                "]}"),
            is(
                "1 error:" + NEW_LINE +
                    " - object instance has properties which are not allowed by the schema: [\"invalidField\"] for field \"/httpRequests/0\"" + NEW_LINE +
                    NEW_LINE +
                    OPEN_API_SPECIFICATION_URL
            ));
    }

    @Test
    public void shouldValidateMultipleInvalidFieldTypes() {
        // when
        assertThat(jsonSchemaValidator.isValid("{ \"httpRequests\": [" + NEW_LINE +
                "  {" + NEW_LINE +
                "    \"method\" : 100," + NEW_LINE +
                "    \"path\" : false" + NEW_LINE +
                "  }" + NEW_LINE +
                "]}"),
            is(
                "2 errors:" + NEW_LINE +
                    " - instance type (integer) does not match any allowed primitive type (allowed: [\"string\"]) for field \"/httpRequests/0/method\"" + NEW_LINE +
                    " - instance type (boolean) does not match any allowed primitive type (allowed: [\"string\"]) for field \"/httpRequests/0/path\"" + NEW_LINE +
                    NEW_LINE +
                    OPEN_API_SPECIFICATION_URL
            ));
    }

    @Test
    public void shouldValidateInvalidListItemType() {
        // when
        assertThat(jsonSchemaValidator.isValid("{ \"httpRequests\": [" + NEW_LINE +
                "  {" + NEW_LINE +
                "    \"headers\" : [ \"invalidValueOne\", \"invalidValueTwo\" ]" + NEW_LINE +
                "  }" + NEW_LINE +
                "]}"),
            is(
                "5 errors:" + NEW_LINE +
                    " - for field \"/httpRequests/0/headers\" only one of the following example formats is allowed: " + NEW_LINE +
                    NEW_LINE +
                    "    {" + NEW_LINE +
                    "        \"exampleHeaderName\" : [ \"exampleHeaderValue\" ]" + NEW_LINE +
                    "        \"exampleMultiValuedHeaderName\" : [ \"exampleHeaderValueOne\", \"exampleHeaderValueTwo\" ]" + NEW_LINE +
                    "    }" + NEW_LINE +
                    NEW_LINE +
                    "   or:" + NEW_LINE +
                    NEW_LINE +
                    "    [" + NEW_LINE +
                    "        {" + NEW_LINE +
                    "            \"name\" : \"exampleHeaderName\"," + NEW_LINE +
                    "            \"values\" : [ \"exampleHeaderValue\" ]" + NEW_LINE +
                    "        }," + NEW_LINE +
                    "        {" + NEW_LINE +
                    "            \"name\" : \"exampleMultiValuedHeaderName\"," + NEW_LINE +
                    "            \"values\" : [ \"exampleHeaderValueOne\", \"exampleHeaderValueTwo\" ]" + NEW_LINE +
                    "        }" + NEW_LINE +
                    "    ]" + NEW_LINE +
                    " - instance type (string) does not match any allowed primitive type (allowed: [\"object\"]) for schema \"keyToMultiValue/oneOf/0\" for field \"/httpRequests/0/headers/0\"" + NEW_LINE +
                    " - instance type (string) does not match any allowed primitive type (allowed: [\"object\"]) for schema \"keyToMultiValue/oneOf/0\" for field \"/httpRequests/0/headers/1\"" + NEW_LINE +
                    " - instance type (array) does not match any allowed primitive type (allowed: [\"object\"]) for schema \"keyToMultiValue/oneOf/1\" for field \"/httpRequests/0/headers\"" + NEW_LINE +
                    " - instance failed to match exactly one schema (matched 0 out of 2) for field \"/httpRequests/0/headers\"" + NEW_LINE +
                    NEW_LINE +
                    OPEN_API_SPECIFICATION_URL
            ));
    }

}
