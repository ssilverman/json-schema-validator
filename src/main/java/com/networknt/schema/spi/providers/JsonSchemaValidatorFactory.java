package com.networknt.schema.spi.providers;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonSchemaValidatorFactory<T extends JsonSchemaValidator> {

    JsonSchemaValidator newInstance(JsonNode schemaTree);
}
