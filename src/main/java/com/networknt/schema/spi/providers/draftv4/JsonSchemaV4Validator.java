package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.spi.JsonSchemaParser;
import com.networknt.schema.spi.providers.JsonSchemaValidator;
import com.networknt.schema.spi.providers.JsonSchemaValidatorFactory;

import java.util.List;

public class JsonSchemaV4Validator implements JsonSchemaValidator {


    private final JsonNode schemaTree;

    private JsonSchemaV4Validator(JsonNode schemaTree) {
        this.schemaTree = schemaTree;
        final JsonSchemaParser parser = new JsonSchemaParser()
                .registerValidator("items", ItemsValidator.Factory)
                // ... and so on and so forth, you create a schema by subscribing validators...
                ;
    }

    @Override
    public List<ValidationMessage> errors() {
        return null;
    }

    public static final class Factory implements JsonSchemaValidatorFactory<JsonSchemaV4Validator> {

        @Override
        public JsonSchemaValidator newInstance(JsonNode schemaTree) {
            return new JsonSchemaV4Validator(schemaTree);
        }
    }
}
