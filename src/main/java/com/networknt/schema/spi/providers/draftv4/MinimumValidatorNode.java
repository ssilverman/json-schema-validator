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

import static com.networknt.schema.ValidatorTypeCode.MINIMUM;

public class MinimumValidatorNode extends BaseJsonValidatorNode {

    public static final String PROPERTY_NAME_MINIMUM = "minimum";
    private static final String PROPERTY_NAME_EXCLUSIVEMINIMUM = "exclusiveMinimum";
    private static final Logger logger = LoggerFactory.getLogger(MinimumValidatorNode.class);

    private final double minimum;
    private final boolean excluded;

    private MinimumValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(MINIMUM, schemaPath, jsonNode, parent, root);

        if (jsonNode.isNumber()) {
            minimum = jsonNode.doubleValue();
        } else {
            throw new JsonSchemaException("minimum value is not a number");
        }

        JsonNode exclusiveMinimumNode = parent.getJsonNode().get(PROPERTY_NAME_EXCLUSIVEMINIMUM);
        excluded = hasBooleanValue(exclusiveMinimumNode) && exclusiveMinimumNode.booleanValue();
    }

    private static boolean hasBooleanValue(JsonNode exclusiveMinimumNode) {
        return exclusiveMinimumNode != null && exclusiveMinimumNode.isBoolean();
    }

    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new LinkedList<>();

        if (!node.isNumber()) {
            // minimum only applies to numbers
            return errors;
        }

        double value = node.doubleValue();
        if (lessThan(value, minimum) || (excluded && equals(value, minimum))) {
            errors.add(buildValidationMessage(at, "" + minimum));
        }
        return errors;
    }

    private static boolean lessThan(double n1, double n2) {
        return n1 - n2 < -1e-12;
    }

    public static final class Factory implements ValidatorNodeFactory<MinimumValidatorNode> {
        @Override
        public MinimumValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                ValidatorNode root) {
            return new MinimumValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
