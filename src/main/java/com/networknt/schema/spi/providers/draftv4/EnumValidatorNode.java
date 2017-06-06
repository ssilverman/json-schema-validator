package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import com.networknt.schema.spi.BaseJsonValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EnumValidatorNode extends BaseJsonValidatorNode {

    public static final String PROPERTY_NAME_ENUM = "enum";
    private static final Logger logger = LoggerFactory.getLogger(EnumValidatorNode.class);

    private final List<JsonNode> nodes;
    private final String error;

    private EnumValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parentSchema, ValidatorNode rootSchema) {
        super(ValidatorTypeCode.ENUM, schemaPath, jsonNode, parentSchema, rootSchema);
        nodes = new ArrayList<>();

        final StringBuilder sb = new StringBuilder();
        if (jsonNode != null && jsonNode.isArray()) {
            sb.append('[');
            int i = 0;
            for (JsonNode n : jsonNode) {
                nodes.add(n);

                String v = n.asText();
                sb.append(i == 0 ? "" : ", ").append(v);
                i++;

            }
            sb.append(']');
            error = sb.toString();

        } else {
            error = "[none]";
        }
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new LinkedList<>();
        if (!nodes.contains(node)) {
            errors.add(buildValidationMessage(at, error));
        }

        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<EnumValidatorNode> {
        @Override
        public EnumValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                                             ValidatorNode root) {
            return new EnumValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
