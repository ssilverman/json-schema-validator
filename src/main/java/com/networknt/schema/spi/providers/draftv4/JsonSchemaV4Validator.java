package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.spi.JsonSchemaParser;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.providers.JsonSchemaValidator;
import com.networknt.schema.spi.providers.JsonSchemaValidatorFactory;

import java.util.List;

public class JsonSchemaV4Validator implements JsonSchemaValidator {

    private final JsonNode schemaTree;
    private final JsonSchemaParser parser;
    private final ValidatorNode validatorTreeRoot;

    private JsonSchemaV4Validator(JsonNode schemaTree) {
        this.schemaTree = schemaTree;
        this.parser = new JsonSchemaParser()
                .registerValidator("items", new AdditionalPropertiesValidatorNode.Factory())
                // ... and so on and so forth, you create a schema by subscribing validators...
                ;
        this.validatorTreeRoot = parser.parse(schemaTree);
    }

    @Override
    public List<ValidationMessage> validate(JsonNode jsonData) {
        return validatorTreeRoot.validate(jsonData, null, "/");
    }

    public static final class Factory implements JsonSchemaValidatorFactory<JsonSchemaV4Validator> {

        @Override
        public JsonSchemaValidator newInstance(JsonNode schemaTree) {
            return new JsonSchemaV4Validator(schemaTree);
        }
    }
}
