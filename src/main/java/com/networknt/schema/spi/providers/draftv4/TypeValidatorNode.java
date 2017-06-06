package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonType;
import com.networknt.schema.TypeFactory;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import com.networknt.schema.spi.BaseJsonValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class TypeValidatorNode extends BaseJsonValidatorNode {

    public static final String PROPERTY_NAME_TYPE = "type";

    private static final Logger logger = LoggerFactory.getLogger(TypeValidatorNode.class);

    private JsonType schemaType;
    private UnionTypeValidatorNode unionTypeValidator;

    protected TypeValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(ValidatorTypeCode.TYPE, schemaPath, jsonNode, parent, root);

        schemaType = TypeFactory.getSchemaNodeType(jsonNode);
        if (schemaType == JsonType.UNION) {
            unionTypeValidator = new UnionTypeValidatorNode.Factory().newInstance(schemaPath, jsonNode, parent, root);
        }
    }

    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new LinkedList<>();

        if (schemaType == JsonType.UNION) {
            errors.addAll(unionTypeValidator.validate(node, rootNode, at));
            return errors;
        }

        JsonType nodeType = TypeFactory.getValueNodeType(node);
        if (nodeType != schemaType) {
            if (schemaType == JsonType.ANY) {
                return errors;
            }

            if (schemaType == JsonType.NUMBER && nodeType == JsonType.INTEGER) {
                return errors;
            }

            errors.add(buildValidationMessage(at, nodeType.toString(), schemaType.toString()));
        }

        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<TypeValidatorNode> {
        @Override
        public TypeValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                ValidatorNode root) {
            return new TypeValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }

}
