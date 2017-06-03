package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonType;
import com.networknt.schema.TypeFactory;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternValidatorNode extends JsonSchemaValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(PatternValidatorNode.class);

    public static final String PROPERTY_NAME_PATTERN = "pattern";

    private final String pattern;
    private final Pattern p;

    private PatternValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTY_NAME_PATTERN, ValidatorTypeCode.PATTERN, schemaPath, jsonNode, parent, root);

        if (jsonNode != null && jsonNode.isTextual()) {
            pattern = jsonNode.textValue();
            p = Pattern.compile(pattern);
        } else {
            pattern = "";
            p = null;
        }

        parseErrorCode(validatorType.getErrorCodeKey());
    }

    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new LinkedList<>();

        JsonType nodeType = TypeFactory.getValueNodeType(node);
        if (nodeType != JsonType.STRING && nodeType != JsonType.NUMBER && nodeType != JsonType.INTEGER) {
            return errors;
        }

        if (p != null) {
            try {
                Matcher m = p.matcher(node.asText());
                if (!m.find()) {
                    errors.add(buildValidationMessage(at, pattern));
                }
            } catch (PatternSyntaxException pse) {
                logger.error("Failed to apply pattern on " + at + ": Invalid syntax [" + pattern + "]", pse);
            }
        }

        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<PatternValidatorNode> {
        @Override
        public PatternValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
            return new PatternValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }

}
