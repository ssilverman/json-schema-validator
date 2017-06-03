package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.networknt.schema.ValidatorTypeCode.PROPERTIES;

public class PropertiesValidatorNode extends JsonSchemaValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesValidatorNode.class);

    public static final String PROPERTY_NAME_PROPERTIES = "properties";

    private final Map<String, ValidatorNode> schemas;

    private PropertiesValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTY_NAME_PROPERTIES, PROPERTIES, schemaPath, jsonNode, parent, root);
        schemas = new HashMap<>();
        for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext(); ) {
            String childPropertyName = it.next();
            final String childSchemaPath = schemaPath + "/" + childPropertyName;
            schemas.put(childPropertyName,
                    new JsonSchemaValidatorNode.Factory()
                            .newInstance(childSchemaPath, jsonNode.get(childPropertyName), parent, root));
        }
    }

    public static final class Factory implements ValidatorNodeFactory<PropertiesValidatorNode> {
        @Override
        public PropertiesValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                ValidatorNode root) {
            return new PropertiesValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
