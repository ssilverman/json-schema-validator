package com.networknt.schema.spi.providers;

import com.networknt.schema.ValidationMessage;

import java.util.List;

public interface JsonSchemaValidator {

    List<ValidationMessage> errors();
}
