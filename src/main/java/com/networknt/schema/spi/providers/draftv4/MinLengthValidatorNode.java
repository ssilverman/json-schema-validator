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

public class MinLengthValidatorNode extends JsonSchemaValidatorNode {

    public static final String PROPERTY_NAME_MINLENGTH = "minLength";

    private static final Logger logger = LoggerFactory.getLogger(MinLengthValidatorNode.class);

    private final int minLength;

    private MinLengthValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTY_NAME_MINLENGTH, ValidatorTypeCode.MAX_LENGTH, schemaPath, jsonNode, parent, root);
        minLength = hasIntegerValue(jsonNode) ? jsonNode.intValue() : Integer.MIN_VALUE;
    }

    private boolean hasIntegerValue(JsonNode jsonNode) {
        return jsonNode != null && jsonNode.isIntegralNumber();
    }

    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        JsonType nodeType = TypeFactory.getValueNodeType(node);
        List<ValidationMessage> errors = new LinkedList<>();
        if (nodeType != JsonType.STRING) {
            // ignore non-string types
            return errors;
        }
        if (node.textValue().codePointCount(0, node.textValue().length()) < minLength) {
            errors.add(buildValidationMessage(at, "" + minLength));
        }
        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<MinLengthValidatorNode> {

        @Override
        public MinLengthValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                ValidatorNode root) {
            return new MinLengthValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
