package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.spi.BaseJsonValidatorNode;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static com.networknt.schema.ValidatorTypeCode.MAXIMUM;

public class MaximumValidatorNode extends BaseJsonValidatorNode {

    public static final String PROPERTY_NAME_MAXIMUM = "maximum";

    private static final String PROPERTY_NAME_EXCLUSIVEMAXIMUM = "exclusiveMaximum";
    private static final Logger logger = LoggerFactory.getLogger(MaximumValidatorNode.class);

    private final double maximum;
    private final boolean excludeEqual;

    private MaximumValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(MAXIMUM, schemaPath, jsonNode, parent, root);
        if (jsonNode.isNumber()) {
            maximum = jsonNode.doubleValue();
        } else {
            throw new JsonSchemaException("maximum value is not a number");
        }

        JsonNode exclusiveMaximumNode = parent.getJsonNode().get(PROPERTY_NAME_EXCLUSIVEMAXIMUM);
        excludeEqual = exclusiveHasBooleanValue(exclusiveMaximumNode) && exclusiveMaximumNode.booleanValue();
    }

    private static boolean exclusiveHasBooleanValue(JsonNode exclusiveMaximumNode) {
        return exclusiveMaximumNode != null && exclusiveMaximumNode.isBoolean();
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode jsonRoot, String at) {
        debug(logger, node, jsonRoot, at);

        List<ValidationMessage> errors = new LinkedList<>();
        if (!node.isNumber()) {
            // maximum only applies to numbers
            return errors;
        }

        double value = node.doubleValue();
        if (greaterThan(value, maximum) || (excludeEqual && equals(value, maximum))) {
            errors.add(buildValidationMessage(at, "" + maximum));
        }
        return errors;
    }

    private static boolean greaterThan(double n1, double n2) {
        return n1 - n2 > 1e-12;
    }

    public static final class Factory implements ValidatorNodeFactory<MaximumValidatorNode> {
        @Override
        public MaximumValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                                                ValidatorNode root) {
            return new MaximumValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
