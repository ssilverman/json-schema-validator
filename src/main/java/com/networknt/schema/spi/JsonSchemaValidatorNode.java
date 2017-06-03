package com.networknt.schema.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.networknt.schema.spi.JsonSchemaParser.PROPERTY_NAME_EMPTY;

public class JsonSchemaValidatorNode implements ValidatorNode {

    private final String propertyName;
    private final String schemaPath;
    private final JsonNode schemaNode;
    private final ValidatorNode parentSchema;
    private final ValidatorNode rootSchema;
    @SuppressWarnings("WeakerAccess")
    protected final List<ValidatorNode> children;

    protected final ValidatorTypeCode validatorType;
    private String errorCode;


    protected JsonSchemaValidatorNode(String propertyName, ValidatorTypeCode validatorTypeCode, String schemaPath,
                                      JsonNode schemaNode, ValidatorNode parentSchema, ValidatorNode rootSchema) {
        this.propertyName = propertyName;
        this.schemaPath = schemaPath;
        this.schemaNode = schemaNode;
        this.parentSchema = parentSchema;
        this.rootSchema = rootSchema;

        this.children = new ArrayList<>();
        this.validatorType = validatorTypeCode;
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        List<ValidationMessage> result = new LinkedList<>();
        for (ValidatorNode validatorNode : children) {
            result.addAll(validatorNode.validate(node, rootNode, at));
        }
        return result;
    }

    protected final ValidationMessage buildValidationMessage(String at, String... arguments) {
        ValidationMessage.Builder builder = new ValidationMessage.Builder();
        if (isUsingCustomErrorCode()) {
            builder.code(errorCode).path(at).arguments(arguments).type(validatorType.getValue());
        } else {
            builder.code(validatorType.getErrorCode()).path(at).arguments(arguments)
                    .format(validatorType.getMessageFormat()).type(validatorType.getValue());
        }
        return builder.build();
    }

    private boolean isUsingCustomErrorCode() {
        return StringUtils.isNotBlank(errorCode);
    }

    protected final void debug(Logger logger, JsonNode node, JsonNode rootNode, String at) {
        logger.debug("validate( " + node + ", " + rootNode + ", " + at + ")");
    }

    protected final void parseErrorCode(String errorCodeKey) {
        JsonNode errorCodeNode = getParentSchema().getJsonNode().get(errorCodeKey);
        if (errorCodeNode != null && errorCodeNode.isTextual()) {
            errorCode = errorCodeNode.asText();
        }
    }

    @Override
    public final String getPropertyName() {
        return propertyName;
    }

    @Override
    public final ValidatorNode getRoot() {
        return rootSchema;
    }

    @Override
    public final List<ValidatorNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public final void addChild(ValidatorNode validatorNode) {
        children.add(validatorNode);
    }

    @Override
    public final String getSchemaPath() {
        return schemaPath;
    }

    @Override
    public final JsonNode getJsonNode() {
        return schemaNode;
    }

    @Override
    public final ValidatorNode getParentSchema() {
        return parentSchema;
    }

    public static final class Factory implements ValidatorNodeFactory<JsonSchemaValidatorNode> {
        private static final ValidatorTypeCode NO_VALIDATOR = null;

        @Override
        public JsonSchemaValidatorNode newInstance(String schemaPath, JsonNode schemaNode,
                                                   ValidatorNode parent, ValidatorNode root) {
            return new JsonSchemaValidatorNode(PROPERTY_NAME_EMPTY, NO_VALIDATOR, schemaPath, schemaNode, parent, root);
        }
    }

}
