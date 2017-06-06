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

public class AllOfValidatorNode extends BaseJsonValidatorNode {

    public static final String PROPERTY_NAME_ALLOF = "allOf";
    private static final Logger logger = LoggerFactory.getLogger(AllOfValidatorNode.class);

    private List<ValidatorNode> schemas = new ArrayList<>();

    private AllOfValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
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

        final List<ValidationMessage> errors = new LinkedList<>();
        for (ValidatorNode schema : schemas) {
            errors.addAll(schema.validate(node, jsonRoot, at));
        }

        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<AllOfValidatorNode> {
        @Override
        public AllOfValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                ValidatorNode root) {
            return new AllOfValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
