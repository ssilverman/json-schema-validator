package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.spi.JsonSchemaParser;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.providers.JsonSchemaValidator;
import com.networknt.schema.spi.providers.JsonSchemaValidatorFactory;

import java.util.List;

import static com.networknt.schema.spi.ValidatorNode.AT_ROOT;
import static com.networknt.schema.spi.ValidatorNode.NO_ROOT;
import static com.networknt.schema.spi.providers.draftv4.AdditionalPropertiesValidatorNode.PROPERTY_NAME_ADDITIONALPROPERTIES;
import static com.networknt.schema.spi.providers.draftv4.AllOfValidatorNode.PROPERTY_NAME_ALLOF;
import static com.networknt.schema.spi.providers.draftv4.AnyOfValidatorNode.PROPERTY_NAME_ANYOF;
import static com.networknt.schema.spi.providers.draftv4.DependenciesValidatorNode.PROPERTY_NAME_DEPENDENCIES;
import static com.networknt.schema.spi.providers.draftv4.EnumValidatorNode.PROPERTY_NAME_ENUM;
import static com.networknt.schema.spi.providers.draftv4.FormatValidatorNode.PROPERTY_NAME_FORMAT;
import static com.networknt.schema.spi.providers.draftv4.ItemsValidatorNode.PROPERTY_NAME_ITEMS;
import static com.networknt.schema.spi.providers.draftv4.MaxItemsValidatorNode.PROPERTY_NAME_MAXITEMS;
import static com.networknt.schema.spi.providers.draftv4.MaxLengthValidatorNode.PROPERTY_NAME_MAXLENGTH;
import static com.networknt.schema.spi.providers.draftv4.MaxPropertiesValidatorNode.PROPERTY_NAME_MAXPROPERTIES;
import static com.networknt.schema.spi.providers.draftv4.MaximumValidatorNode.PROPERTY_NAME_MAXIMUM;
import static com.networknt.schema.spi.providers.draftv4.MinItemsValidatorNode.PROPERTY_NAME_MINITEMS;
import static com.networknt.schema.spi.providers.draftv4.MinLengthValidatorNode.PROPERTY_NAME_MINLENGTH;
import static com.networknt.schema.spi.providers.draftv4.MinPropertiesValidatorNode.PROPERTY_NAME_MINPROPERTIES;
import static com.networknt.schema.spi.providers.draftv4.MinimumValidatorNode.PROPERTY_NAME_MINIMUM;
import static com.networknt.schema.spi.providers.draftv4.MultipleOfValidatorNode.PROPERTY_NAME_MULTIPLEOF;
import static com.networknt.schema.spi.providers.draftv4.NotAllowedValidatorNode.PROPERTY_NAME_NOTALLOWED;
import static com.networknt.schema.spi.providers.draftv4.NotValidatorNode.PROPERTY_NAME_NOT;
import static com.networknt.schema.spi.providers.draftv4.OneOfValidatorNode.PROPERTY_NAME_ONEOF;
import static com.networknt.schema.spi.providers.draftv4.PatternPropertiesValidatorNode.PROPERTY_NAME_PATTERNPROPERTIES;
import static com.networknt.schema.spi.providers.draftv4.PatternValidatorNode.PROPERTY_NAME_PATTERN;
import static com.networknt.schema.spi.providers.draftv4.PropertiesValidatorNode.PROPERTY_NAME_PROPERTIES;
import static com.networknt.schema.spi.providers.draftv4.RefValidatorNode.PROPERTY_NAME_REF;

public class JsonSchemaV4Validator implements JsonSchemaValidator {

    private final JsonNode schemaTree;
    private final JsonSchemaParser parser;
    private final ValidatorNode validatorTreeRoot;

    private JsonSchemaV4Validator(JsonNode schemaTree) {
        this.schemaTree = schemaTree;
        this.parser = JsonSchemaParser.getInstance()
                .registerValidator(PROPERTY_NAME_ITEMS, new ItemsValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_ADDITIONALPROPERTIES, new AdditionalPropertiesValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_ALLOF, new AllOfValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_ANYOF, new AnyOfValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_DEPENDENCIES, new DependenciesValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_ENUM, new EnumValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_FORMAT, new FormatValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_MAXIMUM, new MaximumValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_MAXITEMS, new MaxItemsValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_MAXLENGTH, new MaxLengthValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_MAXPROPERTIES, new MaxPropertiesValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_MINIMUM, new MinimumValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_MINITEMS, new MinItemsValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_MINLENGTH, new MinLengthValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_MINPROPERTIES, new MinPropertiesValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_MULTIPLEOF, new MultipleOfValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_NOTALLOWED, new NotAllowedValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_NOT, new NotValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_ONEOF, new OneOfValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_PATTERNPROPERTIES, new PatternPropertiesValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_PATTERN, new PatternValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_PROPERTIES, new PropertiesValidatorNode.Factory())
                .registerValidator(PROPERTY_NAME_REF, new RefValidatorNode.Factory())
                // ... and so on and so forth, you create a schema by subscribing validators...
                ;
        this.validatorTreeRoot = parser.parse(schemaTree);
    }

    @Override
    public List<ValidationMessage> validate(JsonNode jsonData) {
        return validatorTreeRoot.validate(jsonData, NO_ROOT, AT_ROOT);
    }

    public static final class Factory implements JsonSchemaValidatorFactory<JsonSchemaV4Validator> {
        @Override
        public JsonSchemaValidator newInstance(JsonNode schemaTree) {
            return new JsonSchemaV4Validator(schemaTree);
        }
    }
}
