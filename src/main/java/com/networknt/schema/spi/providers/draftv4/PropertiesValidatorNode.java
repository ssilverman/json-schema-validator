package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.spi.BaseJsonValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.networknt.schema.ValidatorTypeCode.PROPERTIES;

public class PropertiesValidatorNode extends BaseJsonValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesValidatorNode.class);

    public static final String PROPERTY_NAME_PROPERTIES = "properties";

    private final Map<String, ValidatorNode> schemas;

    private PropertiesValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTIES, schemaPath, jsonNode, parent, root);
        schemas = new HashMap<>();
        for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext(); ) {
            String childPropertyName = it.next();
            final String childSchemaPath = schemaPath + "/" + childPropertyName;
            schemas.put(childPropertyName,
                    new JsonSchemaV4Validator(validatorType, childSchemaPath, jsonNode.get(childPropertyName), parent, root));
        }
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new LinkedList<>();

        for (String key : schemas.keySet()) {
            ValidatorNode propertySchema = schemas.get(key);
            JsonNode propertyNode = node.get(key);

            if (propertyNode != null) {
                errors.addAll(propertySchema.validate(propertyNode, rootNode, at + "." + key));
            }
        }

        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<PropertiesValidatorNode> {
        @Override
        public PropertiesValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                ValidatorNode root) {
            return new PropertiesValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
