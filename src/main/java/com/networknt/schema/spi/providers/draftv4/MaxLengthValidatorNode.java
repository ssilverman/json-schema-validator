package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonType;
import com.networknt.schema.TypeFactory;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class MaxLengthValidatorNode extends JsonSchemaValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(MaxLengthValidatorNode.class);
    public static final String PROPERTY_NAME_MAXLENGTH = "maxLength";

    private final int maxLength;

    private MaxLengthValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTY_NAME_MAXLENGTH, ValidatorTypeCode.MAX_LENGTH, schemaPath, jsonNode, parent, root);
        maxLength = hasIntegerValue(jsonNode) ? jsonNode.intValue() : Integer.MAX_VALUE;
    }

    private static boolean hasIntegerValue(JsonNode jsonNode) {
        return jsonNode != null && jsonNode.isIntegralNumber();
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        JsonType nodeType = TypeFactory.getValueNodeType(node);
        List<ValidationMessage> errors = new LinkedList<>();
        if (nodeType != JsonType.STRING) {
            // ignore no-string types
            return errors;
        }
        if (node.textValue().codePointCount(0, node.textValue().length()) > maxLength) {
            errors.add(buildValidationMessage(at, "" + maxLength));
        }
        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<MaxLengthValidatorNode> {
        @Override
        public MaxLengthValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                ValidatorNode root) {
            return new MaxLengthValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
