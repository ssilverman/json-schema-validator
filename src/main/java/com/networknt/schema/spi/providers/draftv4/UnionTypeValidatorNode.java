package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.networknt.schema.spi.JsonSchemaParser.PROPERTY_NAME_EMPTY;

public class UnionTypeValidatorNode extends JsonSchemaValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(UnionTypeValidatorNode.class);

    private final List<ValidatorNode> schemas;
    private final String error;

    private UnionTypeValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTY_NAME_EMPTY, ValidatorTypeCode.UNION_TYPE, schemaPath, jsonNode, parent, root);
        // fixme: the original version of this did NOT call parseErrorCode, remember this in case some weird error occurs
        schemas = new ArrayList<>();
        String sep = "";

        final StringBuilder sb = new StringBuilder();
        sb.append("[");

        if (!jsonNode.isArray()) {
            throw new JsonSchemaException("Expected array for type property on Union Type Definition.");
        }

        int i = 0;
        for (JsonNode childNode : jsonNode) {
            JsonType t = TypeFactory.getSchemaNodeType(childNode);
            sb.append(sep).append(t);
            sep = ", ";

            if (childNode.isObject()) {
                schemas.add(new JsonSchemaValidatorNode.Factory()
                        .newInstance(ValidatorTypeCode.TYPE.getValue(), childNode, parent, root));
            } else {
                final String childPath = schemaPath + "/" + i;
                schemas.add(new TypeValidatorNode.Factory().newInstance(childPath, childNode, parent, root));
            }

            i++;
        }

        error = sb.toString();
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        JsonType nodeType = TypeFactory.getValueNodeType(node);

        List<ValidationMessage> result = new LinkedList<>();
        boolean valid = false;

        for (ValidatorNode schema : schemas) {
            List<ValidationMessage> errors = schema.validate(node, rootNode, at);
            if (errors == null || errors.size() == 0) {
                valid = true;
                break;
            }
        }

        if (!valid) {
            result.add(buildValidationMessage(at, nodeType.toString(), error));
        }

        return result;
    }

    public static final class Factory implements ValidatorNodeFactory<UnionTypeValidatorNode> {
        @Override
        public UnionTypeValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
            return new UnionTypeValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
