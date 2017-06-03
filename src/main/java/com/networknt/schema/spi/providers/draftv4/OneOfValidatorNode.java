package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.RequiredValidator;
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
import java.util.stream.Collectors;

public class OneOfValidatorNode extends JsonSchemaValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(RequiredValidator.class);

    public static final String PROPERTY_NAME_ONEOF = "oneOf";

    private final List<JsonSchemaValidatorNode> schemas = new ArrayList<>();

    private OneOfValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTY_NAME_ONEOF, ValidatorTypeCode.ONE_OF, schemaPath, jsonNode, parent, root);

        int size = jsonNode.size();
        for (int i = 0; i < size; i++) {
            schemas.add(new JsonSchemaValidatorNode.Factory()
                    .newInstance(validatorType.getValue(), jsonNode.get(i), parent, root));
        }
        parseErrorCode(validatorType.getErrorCodeKey());
    }

    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        int numberOfValidSchema = 0;
        List<ValidationMessage> errors = new LinkedList<>();

        for (JsonSchemaValidatorNode schema : schemas) {
            List<ValidationMessage> schemaErrors = schema.validate(node, rootNode, at);
            if (schemaErrors.isEmpty()) {
                numberOfValidSchema++;
                errors = new LinkedList<>();
            }
            if(numberOfValidSchema == 0){
                errors.addAll(schemaErrors);
            }
            if (numberOfValidSchema > 1) {
                break;
            }
        }

        if (numberOfValidSchema == 0) {
            errors = errors.stream()
                    .filter(msg -> !ValidatorTypeCode.ADDITIONAL_PROPERTIES
                            .equals(ValidatorTypeCode.fromValue(msg.getType())))
                    .collect(Collectors.toList());
        }
        if (numberOfValidSchema > 1) {
            errors = new LinkedList<>();
            errors.add(buildValidationMessage(at, ""));
        }

        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<OneOfValidatorNode> {
        @Override
        public OneOfValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                                              ValidatorNode root) {
            return new OneOfValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
