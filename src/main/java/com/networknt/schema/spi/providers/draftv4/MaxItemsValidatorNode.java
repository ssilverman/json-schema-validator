package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class MaxItemsValidatorNode extends JsonSchemaValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(MaxItemsValidatorNode.class);
    public static final String PROPERTY_NAME_MAXITEMS = "maxItems";

    private final int max;

    protected MaxItemsValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTY_NAME_MAXITEMS, ValidatorTypeCode.MAX_ITEMS, schemaPath, jsonNode, parent, root);
        max = jsonNode.isIntegralNumber() ? jsonNode.intValue() : 0;
        parseErrorCode(validatorType.getErrorCodeKey());
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode jsonRoot, String at) {
        debug(logger, node, jsonRoot, at);

        List<ValidationMessage> errors = new LinkedList<>();
        if (node.isArray()) {
            if (node.size() > max) {
                errors.add(buildValidationMessage(at, "" + max));
            }
        }

        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<MaxItemsValidatorNode> {
        @Override
        public MaxItemsValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                                                 ValidatorNode root) {
            return new MaxItemsValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }


}
