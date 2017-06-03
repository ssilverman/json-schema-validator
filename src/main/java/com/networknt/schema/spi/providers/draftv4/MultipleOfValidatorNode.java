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

import static com.networknt.schema.ValidatorTypeCode.MULTIPLE_OF;

public class MultipleOfValidatorNode extends JsonSchemaValidatorNode {

    public static final String PROPERTY_NAME_MULTIPLEOF = "multipleOf";

    private static final Logger logger = LoggerFactory.getLogger(MultipleOfValidatorNode.class);

    private final double divisor;

    private MultipleOfValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTY_NAME_MULTIPLEOF, MULTIPLE_OF, schemaPath, jsonNode, parent, root);
        divisor = jsonNode.isNumber() ? jsonNode.doubleValue() : 0;
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new LinkedList<>();

        if (node.isNumber()) {
            double nodeValue = node.doubleValue();
            if (divisor != 0) {
                long multiples = Math.round(nodeValue / divisor);
                if (Math.abs(multiples * divisor - nodeValue) > 1e-12) {
                    errors.add(buildValidationMessage(at, "" + divisor));
                }
            }
        }

        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<MultipleOfValidatorNode> {
        @Override
        public MultipleOfValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
            return new MultipleOfValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
