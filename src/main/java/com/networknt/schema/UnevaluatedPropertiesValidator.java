/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnevaluatedPropertiesValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UnevaluatedPropertiesValidator.class);

    private final boolean allowUnevaluatedProperties;
    private final JsonSchema unevaluatedPropertiesSchema;
    private final Set<String> allowedProperties = new HashSet<String>();
    private final List<Pattern> patternProperties = new ArrayList<Pattern>();
    private final JsonSchema additionalPropertiesSchema;

    // TODO: $ref targets

    // In-place applicators
    private final JsonSchema allOfSchema;
    private final JsonSchema anyOfSchema;
    private final JsonSchema oneOfSchema;
    private final JsonSchema notSchema;

    public UnevaluatedPropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                                          ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.UNEVALUATED_PROPERTIES, validationContext);
        if (schemaNode.isBoolean()) {
            allowUnevaluatedProperties = schemaNode.booleanValue();
            unevaluatedPropertiesSchema = null;
        } else if (schemaNode.isObject()) {
            allowUnevaluatedProperties = true;
            unevaluatedPropertiesSchema = new JsonSchema(validationContext, getValidatorType().getValue(), parentSchema.getCurrentUri(), schemaNode, parentSchema)
                .initialize();
        } else {
            allowUnevaluatedProperties = false;
            unevaluatedPropertiesSchema = null;
        }

        JsonNode propertiesNode = parentSchema.getSchemaNode().get(PropertiesValidator.PROPERTY);
        if (propertiesNode != null) {
            for (Iterator<String> it = propertiesNode.fieldNames(); it.hasNext(); ) {
                allowedProperties.add(it.next());
            }
        }

        JsonNode patternPropertiesNode = parentSchema.getSchemaNode().get(PatternPropertiesValidator.PROPERTY);
        if (patternPropertiesNode != null) {
            for (Iterator<String> it = patternPropertiesNode.fieldNames(); it.hasNext(); ) {
                patternProperties.add(Pattern.compile(it.next()));
            }
        }

        JsonNode additionalPropertiesNode = parentSchema.getSchemaNode().get(AdditionalPropertiesValidator.PROPERTY);
        if (additionalPropertiesNode != null) {
            additionalPropertiesSchema = new JsonSchema(validationContext, ValidatorTypeCode.ADDITIONAL_PROPERTIES.getValue(), parentSchema.getCurrentUri(), additionalPropertiesNode, parentSchema);
        } else {
            additionalPropertiesSchema = null;
        }

        // In-place applicator schemas
        JsonNode applicatorNode = parentSchema.getSchemaNode().get(AllOfValidator.PROPERTY);
        if (applicatorNode != null) {
            allOfSchema = new JsonSchema(validationContext, ValidatorTypeCode.ALL_OF.getValue(), parentSchema.getCurrentUri(), applicatorNode, parentSchema);
        } else {
            allOfSchema = null;
        }
        applicatorNode = parentSchema.getSchemaNode().get(AnyOfValidator.PROPERTY);
        if (applicatorNode != null) {
            anyOfSchema = new JsonSchema(validationContext, ValidatorTypeCode.ANY_OF.getValue(), parentSchema.getCurrentUri(), applicatorNode, parentSchema);
        } else {
            anyOfSchema = null;
        }
        applicatorNode = parentSchema.getSchemaNode().get(OneOfValidator.PROPERTY);
        if (applicatorNode != null) {
            oneOfSchema = new JsonSchema(validationContext, ValidatorTypeCode.ONE_OF.getValue(), parentSchema.getCurrentUri(), applicatorNode, parentSchema);
        } else {
            oneOfSchema = null;
        }
        applicatorNode = parentSchema.getSchemaNode().get(NotValidator.PROPERTY);
        if (applicatorNode != null) {
            notSchema = new JsonSchema(validationContext, ValidatorTypeCode.NOT.getValue(), parentSchema.getCurrentUri(), applicatorNode, parentSchema);
        } else {
            notSchema = null;
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        if (logger.isDebugEnabled()) debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();
        if (!node.isObject()) {
            // ignore no object
            return errors;
        }

        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            // skip the context items
            if (pname.startsWith("#")) {
                continue;
            }

            boolean handled = false;

            for (Pattern pattern : patternProperties) {
                Matcher m = pattern.matcher(pname);
                if (m.find()) {
                    handled = true;
                    break;
                }
            }

            if (!handled) {
                Set<ValidationMessage> msgs = additionalPropertiesSchema.validate(node.get(pname), rootNode, at + "." + pname);
                if (msgs.size() > 0) {
                    errors.addAll(msgs);
                } else {
                    handled = true;
                }
            }

            if (!allowedProperties.contains(pname) && !handled) {
                if (!allowUnevaluatedProperties) {
                    errors.add(buildValidationMessage(at, pname));
                } else {
                    if (unevaluatedPropertiesSchema != null) {
                        errors.addAll(unevaluatedPropertiesSchema.validate(node.get(pname), rootNode, at + "." + pname));
                    }
                }
            }
        }
        return Collections.unmodifiableSet(errors);
    }

}
