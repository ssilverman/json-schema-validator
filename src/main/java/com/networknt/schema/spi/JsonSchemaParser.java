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
 * into a tree of {@link ValidatorNode}s ready to start validating JSON data, with safe multithreaded calls.
 */
public final class JsonSchemaParser {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaParser.class);

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
        final ValidatorNode rootValidator = new RootValidatorNode.Factory()
                .newInstance("/", rootNode, null);
        parseDown(rootValidator, "");
        return rootValidator;
    }

    private void parseDown(ValidatorNode parentValidator, String propertyName) {
        // fixme: could this typecode thing be done differently?
        ValidatorTypeCode.fromValue(propertyName);

        Iterator<String> propertyNames = parentValidator.getSchemaNode().fieldNames();
        while (propertyNames.hasNext()) {
            final String thisPropertyName = propertyNames.next();
            final String thisSchemaPath = parentValidator.getSchemaPath() + "/" + thisPropertyName;
            final JsonNode thisNode = parentValidator.getSchemaNode().get(thisPropertyName);

            try {
                final ValidatorNodeFactory thisFactory = nodeFactoryMap.get(thisPropertyName);
                final ValidatorNode thisValidator = thisFactory
                        .newInstance(thisSchemaPath, thisNode, parentValidator);
                parseDown(thisValidator, thisPropertyName);
                parentValidator.addChild(thisValidator);

            } catch (Exception e) {
                logger.info("Could not load validator " + thisPropertyName, e);
            }
        }
    }

}
