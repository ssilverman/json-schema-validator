package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;
import com.networknt.schema.spi.BaseValidatorNode;
import com.networknt.schema.spi.RootValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdditionalPropertiesValidator extends BaseValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(AdditionalPropertiesValidator.class);
    private static final String PROPERTY_NAME = "additionalProperties";

    private boolean allowAdditionalProperties;
    private ValidatorNode additionalPropertiesSchema;
    private final List<String> allowedProperties;
    private final List<Pattern> patternProperties;

    private AdditionalPropertiesValidator(String schemaPath, JsonNode schemaNode, ValidatorNode parentSchema,
                                          ValidatorNode rootSchema) {
        super(PROPERTY_NAME, ValidatorTypeCode.ADDITIONAL_PROPERTIES, schemaPath, schemaNode, parentSchema, rootSchema);

        allowAdditionalProperties = false;
        if (schemaNode.isBoolean()) {
            allowAdditionalProperties = schemaNode.booleanValue();
        }
        if (schemaNode.isObject()) {
            allowAdditionalProperties = true;
            additionalPropertiesSchema = new RootValidatorNode(schemaPath + "/additionalProperties", schemaNode, parentSchema);
        }

        allowedProperties = new ArrayList<>();
        JsonNode propertiesNode = parentSchema.getSchemaNode().get(PropertiesValidator.PROPERTY);
        if (propertiesNode != null) {
            for (Iterator<String> it = propertiesNode.fieldNames(); it.hasNext(); ) {
                allowedProperties.add(it.next());
            }
        }

        patternProperties = new ArrayList<>();
        JsonNode patternPropertiesNode = parentSchema.getSchemaNode().get(PatternPropertiesValidator.PROPERTY);
        if (patternPropertiesNode != null) {
            for (Iterator<String> it = patternPropertiesNode.fieldNames(); it.hasNext(); ) {
                patternProperties.add(Pattern.compile(it.next()));
            }
        }

        parseErrorCode(validatorType.getErrorCodeKey());
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new LinkedList<>();
        if (!node.isObject()) {
            // ignore non-objects
            return errors;
        }

        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            // skip context items
            if (pname.startsWith("#")) {
                continue;
            }
            boolean handledByPatternProperties = false;
            for (Pattern pattern : patternProperties) {
                Matcher m = pattern.matcher(pname);
                if (m.find()) {
                    handledByPatternProperties = true;
                    break;
                }
            }

            if (!allowedProperties.contains(pname) && !handledByPatternProperties) {
                if (!allowAdditionalProperties) {
                    errors.add(buildValidationMessage(at, pname));
                } else {
                    if (additionalPropertiesSchema != null) {
                        errors.addAll(additionalPropertiesSchema.validate(node.get(pname), rootNode, at + "." + pname));
                    }
                }
            }
        }
        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<AdditionalPropertiesValidator> {
        @Override
        public AdditionalPropertiesValidator newInstance(String schemaPath, JsonNode schemaNode, ValidatorNode parent) {
            return new AdditionalPropertiesValidator(schemaPath, schemaNode, parent);
        }
    }

}
