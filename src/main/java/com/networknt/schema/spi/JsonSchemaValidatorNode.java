package com.networknt.schema.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.networknt.schema.spi.JsonSchemaParser.PROPERTY_NAME_EMPTY;

public class JsonSchemaValidatorNode implements ValidatorNode {

    private static final Pattern intPattern = Pattern.compile("^[0-9]+$");

    private final String propertyName;
    private final String schemaPath;
    private final JsonNode jsonNode;
    private final ValidatorNode parent;
    private final ValidatorNode root;
    @SuppressWarnings("WeakerAccess")
    protected final List<ValidatorNode> children;

    protected final ValidatorTypeCode validatorType;

    private String errorCode;

    protected JsonSchemaValidatorNode(String propertyName, ValidatorTypeCode validatorTypeCode, String schemaPath,
                                      JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        this.propertyName = propertyName;
        this.schemaPath = schemaPath;
        this.jsonNode = jsonNode;
        this.parent = parent;
        this.root = root;

        this.children = new ArrayList<>();
        this.validatorType = validatorTypeCode;
        if (validatorType != null) {
            parseErrorCode(validatorType.getErrorCodeKey());
        }
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode jsonRoot, String at) {
        List<ValidationMessage> result = new LinkedList<>();
        for (ValidatorNode validatorNode : children) {
            result.addAll(validatorNode.validate(node, jsonRoot, at));
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
        JsonNode errorCodeNode = getParent().getJsonNode().get(errorCodeKey);
        if (errorCodeNode != null && errorCodeNode.isTextual()) {
            errorCode = errorCodeNode.asText();
        }
    }

    protected boolean equals(double n1, double n2) {
        return Math.abs(n1 - n2) < 1e-12;
    }

    @Override
    public final String getPropertyName() {
        return propertyName;
    }

    @Override
    public final ValidatorNode getRoot() {
        return root;
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
        return jsonNode;
    }

    @Override
    public final ValidatorNode getParent() {
        return parent;
    }

    @Override
    public final JsonNode findReference(String reference) {
        ValidatorNode schema = root;
        JsonNode node = schema.getJsonNode();

        if (reference.startsWith("#/")) {
            // handle local ref
            String[] keys = reference.substring(2).split("/");
            for (String key : keys) {
                try {
                    key = URLDecoder.decode(key, "utf-8");
                } catch (UnsupportedEncodingException e) {
                }
                Matcher matcher = intPattern.matcher(key);
                if (matcher.matches()) {
                    node = node.get(Integer.parseInt(key));
                } else {
                    node = node.get(key);
                }
                if (node == null){
                    break;
                }
            }
        }
        return node;
    }

    public static final class Factory implements ValidatorNodeFactory<JsonSchemaValidatorNode> {
        private static final ValidatorTypeCode NO_VALIDATOR = null;

        @Override
        public JsonSchemaValidatorNode newInstance(String schemaPath, JsonNode jsonNode,
                                                   ValidatorNode parent, ValidatorNode root) {
            return new JsonSchemaValidatorNode(PROPERTY_NAME_EMPTY, NO_VALIDATOR, schemaPath, jsonNode, parent, root);
        }
    }

}
