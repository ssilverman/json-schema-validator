package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.spi.BaseJsonValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import com.networknt.schema.spi.ValidatorNodeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.networknt.schema.ValidatorTypeCode.NOT_ALLOWED;

public class NotAllowedValidatorNode extends BaseJsonValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(NotAllowedValidatorNode.class);

    public static final String PROPERTY_NAME_NOTALLOWED = "notAllowed";

    private final List<String> fieldNames = new ArrayList<>();

    private NotAllowedValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(NOT_ALLOWED, schemaPath, jsonNode, parent, root);
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

            if (propertyNode != null) {
                errors.add(buildValidationMessage(at, fieldName));
            }
        }
        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<NotAllowedValidatorNode> {
        @Override
        public NotAllowedValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                                                   ValidatorNode root) {
            return new NotAllowedValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
