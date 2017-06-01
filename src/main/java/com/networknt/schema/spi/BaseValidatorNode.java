package com.networknt.schema.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public abstract class BaseValidatorNode implements ValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchema.class);
    private static final Pattern intPattern = Pattern.compile("^[0-9]+$");

    private final String propertyName;
    private final String schemaPath;
    private final JsonNode schemaNode;
    private final ValidatorNode parentSchema;
    private final ValidatorNode rootSchema;
    protected final List<ValidatorNode> children;


    protected final ValidatorTypeCode validatorType;
    private String errorCode;

    protected BaseValidatorNode(String propertyName, ValidatorTypeCode validatorTypeCode, String schemaPath,
                                JsonNode schemaNode, ValidatorNode parentSchema, ValidatorNode rootSchema) {
        this.propertyName = propertyName;
        this.schemaPath = schemaPath;
        this.schemaNode = schemaNode;
        this.parentSchema = parentSchema;
        this.rootSchema = rootSchema;
        this.children = new ArrayList<>();
        validatorType = validatorTypeCode;
    }

    private boolean isUsingCustomErrorCode() {
        return StringUtils.isNotBlank(errorCode);
    }

    protected ValidationMessage buildValidationMessage(String at, String... arguments) {
        ValidationMessage.Builder builder = new ValidationMessage.Builder();
        if (isUsingCustomErrorCode()) {
            builder.code(errorCode).path(at).arguments(arguments).type(validatorType.getValue());
        } else {
            builder.code(validatorType.getErrorCode()).path(at).arguments(arguments)
                    .format(validatorType.getMessageFormat()).type(validatorType.getValue());
        }
        return builder.build();
    }

    protected void debug(Logger logger, JsonNode node, JsonNode rootNode, String at) {
        logger.debug("validate( " + node + ", " + rootNode + ", " + at + ")");
    }

    protected void parseErrorCode(String errorCodeKey) {
        JsonNode errorCodeNode = getParentSchema().getSchemaNode().get(errorCodeKey);
        if (errorCodeNode != null && errorCodeNode.isTextual()) {
            errorCode = errorCodeNode.asText();
        }
    }

    @Override
    public List<ValidationMessage> validate(JsonNode rootNode) {
        return validate(rootNode, null, "/");
    }

    @Override
    public String getPropertyName() {
        return propertyName;
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
    public final JsonNode getSchemaNode() {
        return schemaNode;
    }

    @Override
    public final ValidatorNode getParentSchema() {
        return parentSchema;
    }

    @Override
    public final ValidatorNode getRootSchema() {
        return rootSchema;
    }

}
