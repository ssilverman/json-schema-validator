package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.RefValidator;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.spi.JsonSchemaParser;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static com.networknt.schema.ValidatorTypeCode.REF;

public class RefValidatorNode extends JsonSchemaValidatorNode {

    public static final String PROPERTY_NAME_REF = "$ref";
    private static final Logger logger = LoggerFactory.getLogger(RefValidator.class);

    private final ValidatorNode schema;

    protected RefValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTY_NAME_REF, REF, schemaPath, jsonNode, parent, root);
        
        ValidatorNode schema = null;
        String refValue = jsonNode.asText();
        if (refValue.startsWith("#")) {
            // handle local $ref
            if (refValue.equals("#")) {
                schema = parent.getRoot();

            } else {
                JsonNode node = parent.findReference(refValue);
                if (node != null) {
                    schema = new JsonSchemaValidatorNode.Factory().newInstance(refValue, node, parent, root);
                }
            }

        } else {
            // handle remote ref
            int index = refValue.indexOf("#");
            String schemaUrl = refValue;
            if (index > 0) {
                schemaUrl = schemaUrl.substring(0, index);
            }

            try {
                URL url = new URL(schemaUrl);
                parent = JsonSchemaParser.getInstance().buildValidatorTree(url);

            } catch (MalformedURLException e) {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(schemaUrl);
                parent = JsonSchemaParser.getInstance().buildValidatorTree(is);
            }

            if (index < 0) {
                schema = parent.getRoot();

            } else {
                refValue = refValue.substring(index);
                if (refValue.equals("#")) {
                    schema = parent.getRoot();

                } else {
                    JsonNode node = parent.findReference(refValue);
                    if (node != null) {
                        schema = new JsonSchemaValidatorNode.Factory().newInstance(refValue, node, parent, root);
                    }
                }
            }
        }

        this.schema = schema;
    }

    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        if (schema != null) {
            return schema.validate(node, rootNode, at);
        } else {
            return new LinkedList<>();
        }
    }

    public static final class Factory implements ValidatorNodeFactory<RefValidatorNode> {
        @Override
        public RefValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
            return new RefValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
