package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.PatternPropertiesValidator;
import com.networknt.schema.PropertiesValidator;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.spi.BaseJsonValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.networknt.schema.ValidatorTypeCode.ADDITIONAL_PROPERTIES;

public class AdditionalPropertiesValidatorNode extends BaseJsonValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(AdditionalPropertiesValidatorNode.class);
    public static final String PROPERTY_NAME_ADDITIONALPROPERTIES = "additionalProperties";

    private final boolean allowAdditionalProperties;
    private ValidatorNode additionalPropertiesSchema;
    private final List<String> allowedProperties;
    private final List<Pattern> patternProperties;

    private AdditionalPropertiesValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                                              ValidatorNode root) {
        super(ADDITIONAL_PROPERTIES, schemaPath, jsonNode, parent, root);

        if (jsonNode.isBoolean()) {
            allowAdditionalProperties = jsonNode.booleanValue();

        } else if (jsonNode.isObject()) {
            allowAdditionalProperties = true;
            final String childSchemaPath = schemaPath + "/" + PROPERTY_NAME_ADDITIONALPROPERTIES;
            additionalPropertiesSchema = new JsonSchemaV4Validator(validatorType, childSchemaPath, jsonNode, parent, root);

        } else {
            allowAdditionalProperties = false;
        }

        allowedProperties = new ArrayList<>();
        final JsonNode propertiesNode = parent.getJsonNode().get(PropertiesValidator.PROPERTY);
        if (propertiesNode != null) {
            for (Iterator<String> it = propertiesNode.fieldNames(); it.hasNext(); ) {
                allowedProperties.add(it.next());
            }
        }

        patternProperties = new ArrayList<>();
        final JsonNode patternPropertiesNode = parent.getJsonNode().get(PatternPropertiesValidator.PROPERTY);
        if (patternPropertiesNode != null) {
            for (Iterator<String> it = patternPropertiesNode.fieldNames(); it.hasNext(); ) {
                patternProperties.add(Pattern.compile(it.next()));
            }
        }
    }

    @Override
    public List<ValidationMessage> validate(JsonNode jsonNode, JsonNode jsonRoot, String at) {
        debug(logger, jsonNode, jsonRoot, at);

        List<ValidationMessage> errors = new LinkedList<>();
        if (!jsonNode.isObject()) {
            // ignore non-objects
            return errors;
        }

        for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext(); ) {
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
                        errors.addAll(additionalPropertiesSchema.validate(jsonNode.get(pname), jsonRoot, at + "." + pname));
                    }
                }
            }
        }
        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<AdditionalPropertiesValidatorNode> {
        @Override
        public AdditionalPropertiesValidatorNode newInstance(String schemaPath, JsonNode jsonNode,
                 ValidatorNode parent, ValidatorNode root) {
            return new AdditionalPropertiesValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }

}
