package com.networknt.schema.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseJsonValidatorNode implements ValidatorNode {
    private static final Pattern intPattern = Pattern.compile("^[0-9]+$");
    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaValidatorNode.class);

    public static final String SCHEMA_PATH_THIS = "#";
    public static final ValidatorNode PARENT_VALIDATOR_NONE = null;
    public static final ValidatorNode ROOT_VALIDATOR_NONE = null;

    protected final ValidatorNode parent;
    private final ValidatorNode root;
    protected final ValidatorTypeCode validatorType;
    protected final String errorCode;
    protected final List<ValidatorNode> children;
    protected final ObjectMapper mapper;

    private final String schemaPath;
    private final JsonNode jsonNode;

    protected BaseJsonValidatorNode(ValidatorTypeCode validatorTypeCode, String schemaPath,
                                      JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        this.schemaPath = schemaPath;
        this.jsonNode = jsonNode;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.root = root;
        this.mapper = new ObjectMapper();
        this.validatorType = validatorTypeCode;
        if (validatorType == null) {
            errorCode = null;
        } else {
            errorCode = parseErrorCode(validatorType.getErrorCodeKey());
        }
    }

    protected String parseErrorCode(String errorCodeKey) {
        if (parent == null) {
            return null;
        }
        JsonNode errorCodeNode = parent.getJsonNode().get(errorCodeKey);
        if (errorCodeNode != null && errorCodeNode.isTextual()) {
            return errorCodeNode.asText();
        }
        return null;
    }

    @Override
    public ValidatorNode getRoot() {
        return root == null ? this : root;
    }

    @Override
    public List<ValidatorNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void addChild(ValidatorNode validatorNode) {
        children.add(validatorNode);
    }

    @Override
    public String getSchemaPath() {
        return schemaPath;
    }

    @Override
    public JsonNode getJsonNode() {
        return jsonNode;
    }

    @Override
    public ValidatorNode getParent() {
        return parent;
    }

    @Override
    public final JsonNode findReference(String reference) {
        JsonNode node = root == null ? jsonNode : root.getJsonNode();

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

    protected boolean equals(double n1, double n2) {
        return Math.abs(n1 - n2) < 1e-12;
    }

    private boolean isUsingCustomErrorCode() {
        return StringUtils.isNotBlank(errorCode);
    }

    @Override
    public abstract List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at);

    protected final void debug(Logger logger, JsonNode node, JsonNode rootNode, String at) {
        logger.debug("validate( " + node + ", " + rootNode + ", " + at + ")");
    }

    @Override
    public String toString() {
        return "\"" + getSchemaPath() + "\" : " + jsonNode.toString();
    }

}
