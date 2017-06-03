package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ItemsValidator;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ItemsValidatorNode extends JsonSchemaValidatorNode {

    public static final String PROPERTY_NAME_ITEMS = "items";
    private static final String PROPERTY_ADDITIONAL_ITEMS = "additionalItems";
    private static final Logger logger = LoggerFactory.getLogger(ItemsValidator.class);

    private final ValidatorNode schema;
    private final List<ValidatorNode> tupleSchema;
    private final boolean additionalItems;
    private final ValidatorNode additionalSchema;

    private ItemsValidatorNode(String schemaPath,
                JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTY_NAME_ITEMS, ValidatorTypeCode.ITEMS, schemaPath, jsonNode, parent, root);

        ValidatorNode schema = null;
        List<ValidatorNode> tupleSchema = null;
        boolean additionalItems = true;
        ValidatorNode additionalSchema = null;

        if (jsonNode.isObject()) {
            schema = new JsonSchemaValidatorNode.Factory()
                    .newInstance(schemaPath, jsonNode, this, root);
            additionalSchema = null;
            additionalItems = false;

        } else {
            tupleSchema = new ArrayList<>();
            for (JsonNode s : jsonNode) {
                tupleSchema.add(new JsonSchemaValidatorNode.Factory()
                        .newInstance(schemaPath, s, this, root));
            }

            final JsonNode addItemNode = getParentSchema().getJsonNode().get(PROPERTY_ADDITIONAL_ITEMS);
            if (addItemNode != null) {
                if (addItemNode.isBoolean()) {
                    additionalItems = addItemNode.asBoolean();
                } else if (addItemNode.isObject()) {
                    additionalSchema = new JsonSchemaValidatorNode.Factory()
                            .newInstance("#", addItemNode, this, root);
                    additionalItems = true;
                }
            }
        }

        this.schema = schema;
        this.tupleSchema = tupleSchema;
        this.additionalItems = additionalItems;
        this.additionalSchema = additionalSchema;

        parseErrorCode(validatorType.getErrorCodeKey());
    }

    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new LinkedList<>();

        if (!node.isArray()) {
            // ignores non-arrays
            return errors;
        }

        int i = 0;
        for (JsonNode n : node) {
            if (schema != null) {
                // validate with item schema (the whole array has the same item
                // schema)
                errors.addAll(schema.validate(n, rootNode, at + "[" + i + "]"));
            }

            if (tupleSchema != null) {
                if (i < tupleSchema.size()) {
                    // validate against tuple schema
                    errors.addAll(tupleSchema.get(i).validate(n, rootNode, at + "[" + i + "]"));
                } else {
                    if (additionalSchema != null) {
                        // validate against additional item schema
                        errors.addAll(additionalSchema.validate(n, rootNode, at + "[" + i + "]"));
                    } else if (!additionalItems) {
                        // no additional item allowed, return error
                        errors.add(buildValidationMessage(at, "" + i));
                    }
                }
            }

            i++;
        }
        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<ItemsValidatorNode> {

        @Override
        public ItemsValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                                              ValidatorNode root) {
            return new ItemsValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
