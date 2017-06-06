package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.spi.BaseJsonValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.networknt.schema.ValidatorTypeCode.ALL_OF;

public class AnyOfValidatorNode extends BaseJsonValidatorNode {

    public static final String PROPERTY_NAME_ANYOF = "AnyOf";
    private static final Logger logger = LoggerFactory.getLogger(AnyOfValidatorNode.class);

    private List<ValidatorNode> schemas = new ArrayList<>();

    private AnyOfValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                               ValidatorNode root) {
        super(ALL_OF, schemaPath, jsonNode, parent, root);
        int size = jsonNode.size();
        for (int i = 0; i < size; i++) {
            schemas.add(new JsonSchemaV4Validator(validatorType, schemaPath, jsonNode.get(i), parent, root));
        }
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode jsonRoot, String at) {
        debug(logger, node, jsonRoot, at);

        final List<ValidationMessage> allErrors = new LinkedList<>();
        for (ValidatorNode schema : schemas) {
            List<ValidationMessage> errors = schema.validate(node, jsonRoot, at);
            if (errors.isEmpty()) {
                return errors;
            }
            allErrors.addAll(errors);
        }

        return allErrors;
    }

    public static final class Factory implements ValidatorNodeFactory<AnyOfValidatorNode> {
        @Override
        public AnyOfValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                                              ValidatorNode root) {
            return new AnyOfValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
