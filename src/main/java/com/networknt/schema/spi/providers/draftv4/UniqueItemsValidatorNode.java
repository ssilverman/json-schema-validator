package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import com.networknt.schema.spi.BaseJsonValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class UniqueItemsValidatorNode extends BaseJsonValidatorNode {

    public static final String PROPERTY_NAME_UNIQUEITEMS = "uniqueItems";

    private static final Logger logger = LoggerFactory.getLogger(UniqueItemsValidatorNode.class);

    private final boolean unique;

    private UniqueItemsValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(ValidatorTypeCode.UNIQUE_ITEMS, schemaPath, jsonNode, parent, root);
        unique = jsonNode.isBoolean() && jsonNode.booleanValue();
    }

    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new LinkedList<>();

        if (unique) {
            Set<JsonNode> set = new HashSet<JsonNode>();
            for (JsonNode n : node) {
                set.add(n);
            }

            if (set.size() < node.size()) {
                errors.add(buildValidationMessage(at));
            }
        }

        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<UniqueItemsValidatorNode> {
        @Override
        public UniqueItemsValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
            return new UniqueItemsValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }

}
