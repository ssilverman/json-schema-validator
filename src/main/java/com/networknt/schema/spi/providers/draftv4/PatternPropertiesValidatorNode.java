package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.PatternPropertiesValidator;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternPropertiesValidatorNode extends JsonSchemaValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(PatternPropertiesValidator.class);

    public static final String PROPERTY_NAME_PATTERPROPERTIES = "patternProperties";

    private final Map<Pattern, ValidatorNode> schemas;

    private PatternPropertiesValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
            ValidatorNode root) {
        super(PROPERTY_NAME_PATTERPROPERTIES, ValidatorTypeCode.PATTERN_PROPERTIES, schemaPath, jsonNode, parent, root);
        if (!jsonNode.isObject()) {
            throw new JsonSchemaException("patternProperties must be an object node");
        }

        Map<Pattern, ValidatorNode> schemas = new HashMap<>();
        Iterator<String> names = jsonNode.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            schemas.put(Pattern.compile(name),
                    new JsonSchemaValidatorNode.Factory()
                            .newInstance(name, jsonNode.get(name), parent, root));
        }

        this.schemas = Collections.unmodifiableMap(schemas);
    }

    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new LinkedList<>();
        if (!node.isObject()) {
            return errors;
        }

        Iterator<String> names = node.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            JsonNode n = node.get(name);
            for (Pattern pattern : schemas.keySet()) {
                Matcher m = pattern.matcher(name);
                if (m.find()) {
                    errors.addAll(schemas.get(pattern).validate(n, rootNode, at + "." + name));
                }
            }
        }
        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<PatternPropertiesValidatorNode> {
        @Override
        public PatternPropertiesValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
            return new PatternPropertiesValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }

}
