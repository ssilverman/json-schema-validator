package com.networknt.schema.spi;

import com.fasterxml.jackson.databind.JsonNode;

public interface ValidatorNodeFactory<T extends ValidatorNode> {

    T newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root);

}
