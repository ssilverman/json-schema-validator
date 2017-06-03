package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RequiredValidatorNode extends JsonSchemaValidatorNode {

    public static final String PROPERTY_NAME_REQUIRED = "required";

    private static final Logger logger = LoggerFactory.getLogger(RequiredValidatorNode.class);

    private final List<String> fieldNames = new ArrayList<String>();

    private RequiredValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTY_NAME_REQUIRED, ValidatorTypeCode.REQUIRED, schemaPath, jsonNode, parent, root);
        if (jsonNode.isArray()) {
            int size = jsonNode.size();
            for (int i = 0; i < size; i++) {
                fieldNames.add(jsonNode.get(i).asText());
            }
        }
    }

    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new LinkedList<>();
        for (String fieldName : fieldNames) {
            JsonNode propertyNode = node.get(fieldName);

            if (propertyNode == null) {
                errors.add(buildValidationMessage(at, fieldName));
            }
        }

        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<RequiredValidatorNode> {

        @Override
        public RequiredValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                ValidatorNode root) {
            return new RequiredValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }

}
