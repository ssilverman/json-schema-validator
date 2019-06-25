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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EnumValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(EnumValidator.class);

    private final Set<JsonNode> nodes;
    private final String error;

    public EnumValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
            super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ENUM, validationContext);

        if (schemaNode != null && schemaNode.isArray()) {
            nodes = new HashSet<JsonNode>();
            StringBuilder sb = new StringBuilder();

            sb.append('[');
            String separator = "";

            for (JsonNode n : schemaNode) {
                nodes.add(n);

                sb.append(separator);
                sb.append(n.asText());
                separator = ", ";
            }

            sb.append(']');

            error = sb.toString();
        } else {
            nodes = Collections.emptySet();
            error = "[none]";
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<>();
        boolean hasError = false;
        JsonNode enumItem = null;
        //first find if the node exists in enum without matching cases.
        Optional<JsonNode> result = nodes.stream()
                .filter(enumNode -> enumNode.asText().equalsIgnoreCase(node.asText()))
                .findAny();
        if (result.isPresent()) {
            //if node exists and type loose, try to check if the node is string type. (convert from string)
            boolean typeLooseValidCondition = config.isTypeLoose() && TypeFactory.getValueNodeType(node).equals(JsonType.STRING);
            //if node exists and not type loose, try to compare if the types of two nodes are the same.
            boolean notTypeLooseValidCondition = !config.isTypeLoose() && TypeFactory.getValueNodeType(node).equals(TypeFactory.getSchemaNodeType(enumItem));
            if(!typeLooseValidCondition || !notTypeLooseValidCondition) {
                hasError = true;
            }
            //if node exists, and set case sensitive, try to compare again without ignore case.
            if(config.isEnumCaseSensitive() && !enumItem.asText().equals(node.asText())) {
                hasError = true;
            }
        //if cannot find at at all, validation error
        } else {
            hasError = true;
        }

        if (hasError) {
            errors.add(buildValidationMessage(at, error));
        }

        return Collections.unmodifiableSet(errors);
    }
}
