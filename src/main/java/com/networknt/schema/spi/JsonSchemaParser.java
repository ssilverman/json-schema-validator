package com.networknt.schema.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaException;
import com.sun.istack.internal.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class parses a JSON tree in Jackson {@link com.fasterxml.jackson.databind.JsonNode JsonNode}s
 * into a tree of {@link ValidatorNode}s ready to start validating JSON data in a thread-safe manner.
 */
public final class JsonSchemaParser {

    public static final String PROPERTY_NAME_EMPTY = "";

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaParser.class);
    private static final String SCHEMA_PATH_THIS = "#";
    private static final ValidatorNode PARENT_VALIDATOR_NONE = null;
    private static final ValidatorNode ROOT_VALIDATOR_NONE = null;

    private static JsonSchemaParser instance;

    private final ConcurrentMap<String, ValidatorNodeFactory> nodeFactoryMap;
    private final ObjectMapper mapper;

    private JsonSchemaParser() {
        this.nodeFactoryMap = new ConcurrentHashMap<>();
        this.mapper = new ObjectMapper();
    }

    public static JsonSchemaParser getInstance() {
        if (instance == null) {
            instance = new JsonSchemaParser();
        }
        return instance;
    }

    public JsonSchemaParser registerValidator(String elementName, ValidatorNodeFactory factory) {
        nodeFactoryMap.put(elementName, factory);
        return this;
    }

    public void registerValidators(Map<String, ValidatorNodeFactory> factoryMap) {
        nodeFactoryMap.putAll(factoryMap);
    }

    public ValidatorNode parse(String propertyName, JsonNode rootNode) {
        return parseDown(propertyName, SCHEMA_PATH_THIS, rootNode, null, null);
    }

    private ValidatorNode parseDown(String propertyName, String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                ValidatorNode root) {
        final ValidatorNode currentValidator = buildCurrentNode(propertyName, schemaPath, jsonNode, parent, root);
        if (currentValidator == null) {
            throw new JsonSchemaException("validator not found: " + propertyName);
//            return null;
        }
        root = root == null ? currentValidator : root;

        final Iterator<String> propertyNames = currentValidator.getJsonNode().fieldNames();
        while (propertyNames.hasNext()) {
            final String childPropertyName = propertyNames.next();
            final String childSchemaPath = addSlash(schemaPath) + childPropertyName;
            final JsonNode child = jsonNode.get(childPropertyName);

            currentValidator.addChild(parseDown(childPropertyName, childSchemaPath, child, currentValidator, root));
        }
        return currentValidator;
    }

    private static String addSlash(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    private ValidatorNode buildCurrentNode(String propertyName, String schemaPath, @NotNull JsonNode jsonNode,
                ValidatorNode parent, ValidatorNode root) {
        if ("".equals(propertyName)) {
            return new JsonSchemaValidatorNode(propertyName, null, "#", jsonNode, parent,
                    root);
        }
        ValidatorNodeFactory factory = nodeFactoryMap.get(propertyName);
        if (factory == null) {
            return null;
        }
        return factory.newInstance(schemaPath, jsonNode, parent, root);
    }

    public ValidatorNode buildValidatorTree(String schema) {
        return buildValidatorTree(() -> mapper.readTree(schema));
    }

    public ValidatorNode buildValidatorTree(InputStream schemaStream) {
        return buildValidatorTree(() -> mapper.readTree(schemaStream));
    }

    public ValidatorNode buildValidatorTree(URL url) {
        return buildValidatorTree(() -> mapper.readTree(url.openStream()));
    }

    private static ValidatorNode buildValidatorTree(CheckedNodeSupplier supplier) {
        try {
            JsonNode jsonNode = supplier.supply();
            return new JsonSchemaValidatorNode.Factory()
                    .newInstance(SCHEMA_PATH_THIS, jsonNode, PARENT_VALIDATOR_NONE, ROOT_VALIDATOR_NONE);
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new JsonSchemaException(ioe);
        }
    }

    @FunctionalInterface
    private interface CheckedNodeSupplier {
        JsonNode supply() throws IOException;
    }

}
