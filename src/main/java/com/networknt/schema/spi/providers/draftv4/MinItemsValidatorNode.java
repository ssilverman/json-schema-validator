package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import com.networknt.schema.spi.BaseJsonValidatorNode;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class MinItemsValidatorNode extends BaseJsonValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(MinItemsValidatorNode.class);
    public static final String PROPERTY_NAME_MINITEMS = "minItems";

    private final int min;

    private MinItemsValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(ValidatorTypeCode.MIN_ITEMS, schemaPath, jsonNode, parent, root);
        min = jsonNode.isIntegralNumber() ? jsonNode.intValue() : 0;
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new LinkedList<>();
        if (node.isArray()) {
            if (node.size() < min) {
                errors.add(buildValidationMessage(at, "" + min));
            }
        }
        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<MinItemsValidatorNode> {
        @Override
        public MinItemsValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
            return new MinItemsValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
