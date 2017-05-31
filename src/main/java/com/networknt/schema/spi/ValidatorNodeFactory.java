package com.networknt.schema.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;

public interface ValidatorNodeFactory<T extends ValidatorNode> {

    T newInstance(String schemaPath, JsonNode schemaNode, ValidatorNode parent);

}
