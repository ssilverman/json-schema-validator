package com.networknt.schema.spi.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

import java.util.List;

public interface JsonSchemaValidator {

    List<ValidationMessage> validate(JsonNode jsonData);

}
