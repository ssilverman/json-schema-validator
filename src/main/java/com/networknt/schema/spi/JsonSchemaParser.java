package com.networknt.schema.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidatorTypeCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class parses a JSON tree in Jackson {@link com.fasterxml.jackson.databind.JsonNode JsonNode}s
 * into a tree of {@link ValidatorNode}s ready to start validating JSON data in a thread-safe manner.
 */
public final class JsonSchemaParser {

    @SuppressWarnings("WeakerAccess")
    public static final String PROPERTY_NAME_EMPTY = "";

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaParser.class);
    private static final String SCHEMA_PATH_ROOT = "/";
    private static final ValidatorNode PARENT_VALIDATOR_NONE = null;
    private static final ValidatorNode ROOT_VALIDATOR_NONE = null;

    private final ConcurrentMap<String, ValidatorNodeFactory> nodeFactoryMap;

    public JsonSchemaParser() {
        this.nodeFactoryMap = new ConcurrentHashMap<>();
    }

    public JsonSchemaParser registerValidator(String elementName, ValidatorNodeFactory factory) {
        nodeFactoryMap.put(elementName, factory);
        return this;
    }

    public void registerValidators(Map<String, ValidatorNodeFactory> factoryMap) {
        nodeFactoryMap.putAll(factoryMap);
    }

    public ValidatorNode parse(JsonNode rootNode) {
        final ValidatorNode rootValidator = new JsonSchemaValidatorNode.Factory()
                .newInstance(SCHEMA_PATH_ROOT, rootNode, PARENT_VALIDATOR_NONE, ROOT_VALIDATOR_NONE);
        parseDown(rootValidator, rootValidator, PROPERTY_NAME_EMPTY);
        return rootValidator;
    }

    private void parseDown(ValidatorNode parentValidator, ValidatorNode rootValidator, String propertyName) {
        // fixme: could this typecode thing be done differently?
        ValidatorTypeCode.fromValue(propertyName);

        Iterator<String> propertyNames = parentValidator.getJsonNode().fieldNames();
        while (propertyNames.hasNext()) {
            final String thisPropertyName = propertyNames.next();
            final String thisSchemaPath = parentValidator.getSchemaPath() + "/" + thisPropertyName;
            final JsonNode thisNode = parentValidator.getJsonNode().get(thisPropertyName);

            try {
                final ValidatorNodeFactory thisFactory = nodeFactoryMap.get(thisPropertyName);
                final ValidatorNode thisValidator = thisFactory
                        .newInstance(thisSchemaPath, thisNode, parentValidator, rootValidator);
                parseDown(thisValidator, rootValidator, thisPropertyName);
                parentValidator.addChild(thisValidator);

            } catch (Exception e) {
                logger.info("Could not load validator " + thisPropertyName, e);
            }
        }
    }

}
