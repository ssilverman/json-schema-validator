package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.DependenciesValidator;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.networknt.schema.ValidatorTypeCode.DEPENDENCIES;

public class DependenciesValidatorNode extends JsonSchemaValidatorNode {

    public static final String PROPERTY_NAME_DEPENDENCIES = "dependencies";
    private static final Logger logger = LoggerFactory.getLogger(DependenciesValidator.class);

    private final Map<String, List<String>> propertyDeps = new HashMap<String, List<String>>();
    private ConcurrentMap<String, ValidatorNode> schemaDeps = new ConcurrentHashMap<>();

    private DependenciesValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parentSchema,
                                      ValidatorNode root) {
        super(PROPERTY_NAME_DEPENDENCIES, DEPENDENCIES, schemaPath, jsonNode, parentSchema, root);
        for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext();) {
            final String propertyName = it.next();
            final JsonNode propertyValue = jsonNode.get(propertyName);
            if (propertyValue.isArray()) {
                List<String> depsProps = propertyDeps.computeIfAbsent(propertyName, k -> new ArrayList<>());
                for (int i = 0; i < propertyValue.size(); i++) {
                    depsProps.add(propertyValue.get(i).asText());
                }

            } else if (propertyValue.isObject()) {
                schemaDeps.put(propertyName, new JsonSchemaValidatorNode.Factory()
                        .newInstance(schemaPath, propertyValue, parentSchema, root));
            }
        }

        parseErrorCode(validatorType.getErrorCodeKey());
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new ArrayList<>();

        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String proeprtyName = it.next();
            List<String> deps = propertyDeps.get(proeprtyName);
            if (deps != null && !deps.isEmpty()) {
                for (String field : deps) {
                    if (node.get(field) == null) {
                        errors.add(buildValidationMessage(at, propertyDeps.toString()));
                    }
                }
            }
            ValidatorNode schema = schemaDeps.get(proeprtyName);
            if (schema != null) {
                errors.addAll(schema.validate(node, rootNode, at));
            }
        }

        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<DependenciesValidatorNode> {
        @Override
        public DependenciesValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                ValidatorNode root) {
            return new DependenciesValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
