package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonType;
import com.networknt.schema.TypeFactory;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.networknt.schema.ValidatorTypeCode.FORMAT;

public class FormatValidatorNode extends JsonSchemaValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(FormatValidatorNode.class);
    public static final String PROPERTY_NAME_FORMAT = "format";

    private static final Map<String, Pattern> FORMATS = new HashMap<String, Pattern>();
    static {
        FORMATS.put("date-time", Pattern.compile(
                "^\\d{4}-(?:0[0-9]{1}|1[0-2]{1})-[0-9]{2}[tT ]\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([zZ]|[+-]\\d{2}:\\d{2})$"));
        FORMATS.put("date", Pattern.compile("^\\d{4}-(?:0[0-9]{1}|1[0-2]{1})-[0-9]{2}$"));
        FORMATS.put("time", Pattern.compile("^\\d{2}:\\d{2}:\\d{2}$"));
        FORMATS.put("email", Pattern.compile("^\\S+@\\S+$"));
        FORMATS.put("ip-address", Pattern.compile(
                "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"));
        FORMATS.put("ipv4", Pattern.compile(
                "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"));
        FORMATS.put("ipv6", Pattern.compile(
                "^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$"));
        FORMATS.put("uri", Pattern.compile("(^[a-zA-Z][a-zA-Z0-9+-.]*:[^\\s]*$)|(^//[^\\s]*$)"));
        FORMATS.put("color", Pattern.compile(
                "(#?([0-9A-Fa-f]{3,6})\\b)|(aqua)|(black)|(blue)|(fuchsia)|(gray)|(green)|(lime)|(maroon)|(navy)|(olive)|(orange)|(purple)|(red)|(silver)|(teal)|(white)|(yellow)|(rgb\\(\\s*\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b\\s*,\\s*\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b\\s*,\\s*\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b\\s*\\))|(rgb\\(\\s*(\\d?\\d%|100%)+\\s*,\\s*(\\d?\\d%|100%)+\\s*,\\s*(\\d?\\d%|100%)+\\s*\\))"));
        FORMATS.put("hostname", Pattern.compile(
                "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$"));
        FORMATS.put("alpha", Pattern.compile("^[a-zA-Z]+$"));
        FORMATS.put("alphanumeric", Pattern.compile("^[a-zA-Z0-9]+$"));
        FORMATS.put("phone", Pattern.compile("^\\+(?:[0-9] ?){6,14}[0-9]$"));
        FORMATS.put("utc-millisec", Pattern.compile("^[0-9]+(\\.?[0-9]+)?$"));
        FORMATS.put("style", Pattern.compile("\\s*(.+?):\\s*([^;]+);?"));
    }

    private final String format;
    private final Pattern pattern;

    private FormatValidatorNode(String schemaPath, JsonNode jsonNode, ValidatorNode parent, ValidatorNode root) {
        super(PROPERTY_NAME_FORMAT, FORMAT, schemaPath, jsonNode, parent, root);
        if (jsonNode != null && jsonNode.isTextual()) {
            format = jsonNode.textValue();
            pattern = FORMATS.get(format);
        } else {
            format = null;
            pattern = null;
        }

        parseErrorCode(validatorType.getErrorCodeKey());
    }

    @Override
    public List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        List<ValidationMessage> errors = new LinkedList<>();

        JsonType nodeType = TypeFactory.getValueNodeType(node);
        if (nodeType != JsonType.STRING) {
            return errors;
        }

        if (pattern != null) {
            try {
                Matcher m = pattern.matcher(node.textValue());
                if (!m.matches()) {
                    errors.add(buildValidationMessage(at, format, pattern.pattern()));
                }
            } catch (PatternSyntaxException pse) {
                // String is considered valid if pattern is invalid
                logger.error("Failed to apply pattern on " + at + ": Invalid RE syntax [" + format + "]", pse);
            }
        }

        return errors;
    }

    public static final class Factory implements ValidatorNodeFactory<FormatValidatorNode> {

        @Override
        public FormatValidatorNode newInstance(String schemaPath, JsonNode jsonNode, ValidatorNode parent,
                                               ValidatorNode root) {
            return new FormatValidatorNode(schemaPath, jsonNode, parent, root);
        }
    }
}
