package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static com.networknt.schema.ValidatorTypeCode.NOT;

public class NotValidatorNode extends JsonSchemaValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(NotValidatorNode.class);

    public static final String PROPERTY_NAME_NOT = "not";

    private final JsonSchemaValidatorNode schema;

    private NotValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTY_NAME_NOT, NOT, schemaPath, jsonNode, parent, root);
        schema = new JsonSchemaValidatorNode.Factory().newInstance(validatorType.getValue(), jsonNode, parent, root);
        parseErrorCode(validatorType.getErrorCodeKey());
    }

    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> notValidationError = new LinkedList<>();
        List<ValidationMessage> errors = schema.validate(node, rootNode, at);
        if (errors.isEmpty()) {
            notValidationError.add(buildValidationMessage(at, schema.toString()));
        }
        return notValidationError;
    }


    public static final class Factory implements ValidatorNodeFactory<NotValidatorNode> {
        @Override
        public NotValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
            return new NotValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
