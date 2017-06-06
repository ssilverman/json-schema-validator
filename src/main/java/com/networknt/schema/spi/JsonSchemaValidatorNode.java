package com.networknt.schema.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonSchemaValidatorNode extends BaseJsonValidatorNode {

    private static final Pattern intPattern = Pattern.compile("^[0-9]+$");
    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaValidatorNode.class);

    public static final String SCHEMA_PATH_THIS = "#";
    public static final ValidatorNode PARENT_VALIDATOR_NONE = null;
    public static final ValidatorNode ROOT_VALIDATOR_NONE = null;

    private final ConcurrentMap<String, ValidatorNodeFactory<? extends ValidatorNode>> validatorMap;

    protected JsonSchemaValidatorNode(ValidatorTypeCode validatorTypeCode, String schemaPath, JsonNode jsonNode,
                                      ValidatorNode parent, ValidatorNode root, ValidatorNodeMap validators) {
        super(validatorTypeCode, schemaPath, jsonNode, parent, root);
        validatorMap = new ConcurrentHashMap<>();
        validatorMap.putAll(validators);
        parseDown(schemaPath, jsonNode, root);
    }

    private void parseDown(String schemaPath, JsonNode jsonNode, ValidatorNode root) {
        final Iterator<String> propertyNames = jsonNode.fieldNames();
        while (propertyNames.hasNext()) {
            final String childPropertyName = propertyNames.next();

            final ValidatorNodeFactory<? extends ValidatorNode> factory = validatorMap.get(childPropertyName);
            if (factory == null) {
                continue;
            }
            final String childSchemaPath = addSlash(schemaPath) + childPropertyName;
            final JsonNode childJsonNode = jsonNode.get(childPropertyName);
            final ValidatorNode childValidatorNode = factory.newInstance(childSchemaPath, childJsonNode, this,
                    getRoot());

            addChild(childValidatorNode);
        }
    }

    private static String addSlash(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    public JsonSchemaValidatorNode registerValidator(String elementName,
                ValidatorNodeFactory<? extends ValidatorNode> factory) {
        validatorMap.put(elementName, factory);
        return this;
    }

    public void registerValidators(ValidatorNodeMap factoryMap) {
        validatorMap.putAll(factoryMap);
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode jsonRoot, String at) {
        List<ValidationMessage> result = new LinkedList<>();
        for (ValidatorNode validatorNode : children) {
            result.addAll(validatorNode.validate(node, jsonRoot, at));
        }
        return result;
    }

    public static final class Factory {
        public JsonSchemaValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                                                   ValidatorNode root, ValidatorNodeMap validators) {
            return new JsonSchemaValidatorNode(null, schemaPath, jsonNode, parent, root, validators);
        }
    }

}
