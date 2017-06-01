package com.networknt.schema.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public final class RootValidatorNode extends BaseValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchema.class);
    private static final Pattern intPattern = Pattern.compile("^[0-9]+$");

    private RootValidatorNode(String schemaPath, JsonNode schemaNode, ValidatorNode parentSchema) {
        super("", null, schemaPath, schemaNode, parentSchema);
    }

    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        List<ValidationMessage> result = new LinkedList<>();
        for (ValidatorNode validatorNode : children) {
            result.addAll(validatorNode.validate(node, rootNode, at));
        }
        return result;
    }

    public static class Factory implements ValidatorNodeFactory {
        /**
         * Gives you a <b>new</b> instance of your implementation class.
         * You should consider keeping your constructors private and only returning instances
         * of your implementations through this method.
         *
         * @param schemaPath a JSON Pointer expression indicating the path from the root of the tree
         *                  this node belongs to
         * @param schemaNode
         * @param parent
         * @return
         */
        @Override
        public ValidatorNode newInstance(String schemaPath, JsonNode schemaNode, ValidatorNode parent) {
            return new RootValidatorNode(schemaPath, schemaNode, parent);
        }
    }

}
