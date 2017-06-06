package com.networknt.schema.spi.providers.draftv4;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import com.networknt.schema.spi.JsonSchemaValidatorNode;
import com.networknt.schema.spi.ValidatorNode;
import com.networknt.schema.spi.ValidatorNodeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

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
import static com.networknt.schema.spi.providers.draftv4.RequiredValidatorNode.PROPERTY_NAME_REQUIRED;
import static com.networknt.schema.spi.providers.draftv4.TypeValidatorNode.PROPERTY_NAME_TYPE;
import static com.networknt.schema.spi.providers.draftv4.UniqueItemsValidatorNode.PROPERTY_NAME_UNIQUEITEMS;

public class JsonSchemaV4Validator extends JsonSchemaValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaV4Validator.class);

    public JsonSchemaV4Validator(JsonNode schemaTree) {
        super(null, SCHEMA_PATH_THIS, schemaTree, null, null, registerValidators());
        registerValidators();
    }

    public JsonSchemaV4Validator(ValidatorTypeCode validatorTypeCode, String schemaPath, JsonNode jsonNode,
                ValidatorNode parent, ValidatorNode root) {
        super(validatorTypeCode, schemaPath, jsonNode, parent, root, registerValidators());
        registerValidators();
    }

    private static ValidatorNodeMap registerValidators() {
        ValidatorNodeMap result = new ValidatorNodeMap();
        result.put(PROPERTY_NAME_ITEMS, new ItemsValidatorNode.Factory());
        result.put(PROPERTY_NAME_ADDITIONALPROPERTIES, new AdditionalPropertiesValidatorNode.Factory());
        result.put(PROPERTY_NAME_ALLOF, new AllOfValidatorNode.Factory());
        result.put(PROPERTY_NAME_ANYOF, new AnyOfValidatorNode.Factory());
        result.put(PROPERTY_NAME_DEPENDENCIES, new DependenciesValidatorNode.Factory());
        result.put(PROPERTY_NAME_ENUM, new EnumValidatorNode.Factory());
        result.put(PROPERTY_NAME_FORMAT, new FormatValidatorNode.Factory());
        result.put(PROPERTY_NAME_MAXIMUM, new MaximumValidatorNode.Factory());
        result.put(PROPERTY_NAME_MAXITEMS, new MaxItemsValidatorNode.Factory());
        result.put(PROPERTY_NAME_MAXLENGTH, new MaxLengthValidatorNode.Factory());
        result.put(PROPERTY_NAME_MAXPROPERTIES, new MaxPropertiesValidatorNode.Factory());
        result.put(PROPERTY_NAME_MINIMUM, new MinimumValidatorNode.Factory());
        result.put(PROPERTY_NAME_MINITEMS, new MinItemsValidatorNode.Factory());
        result.put(PROPERTY_NAME_MINLENGTH, new MinLengthValidatorNode.Factory());
        result.put(PROPERTY_NAME_MINPROPERTIES, new MinPropertiesValidatorNode.Factory());
        result.put(PROPERTY_NAME_MULTIPLEOF, new MultipleOfValidatorNode.Factory());
        result.put(PROPERTY_NAME_NOTALLOWED, new NotAllowedValidatorNode.Factory());
        result.put(PROPERTY_NAME_NOT, new NotValidatorNode.Factory());
        result.put(PROPERTY_NAME_ONEOF, new OneOfValidatorNode.Factory());
        result.put(PROPERTY_NAME_PATTERNPROPERTIES, new PatternPropertiesValidatorNode.Factory());
        result.put(PROPERTY_NAME_PATTERN, new PatternValidatorNode.Factory());
        result.put(PROPERTY_NAME_PROPERTIES, new PropertiesValidatorNode.Factory());
        result.put(PROPERTY_NAME_REF, new RefValidatorNode.Factory());
        result.put(PROPERTY_NAME_REQUIRED, new RequiredValidatorNode.Factory());
        result.put(PROPERTY_NAME_TYPE, new TypeValidatorNode.Factory());
        result.put(PROPERTY_NAME_UNIQUEITEMS, new UniqueItemsValidatorNode.Factory());

        return result;
    }

    public List<ValidationMessage> validate(JsonNode jsonNode) {
        return validate(jsonNode, jsonNode, "#");
    }

    protected ValidatorNode buildValidatorTree(String schema) {
        return buildValidatorTree(() -> mapper.readTree(schema));
    }

    protected ValidatorNode buildValidatorTree(InputStream schemaStream) {
        return buildValidatorTree(() -> mapper.readTree(schemaStream));
    }

    protected ValidatorNode buildValidatorTree(URL url) {
        return buildValidatorTree(() -> mapper.readTree(url.openStream()));
    }

    private ValidatorNode buildValidatorTree(CheckedNodeSupplier supplier) {
        try {
            JsonNode jsonNode = supplier.supply();
            return new JsonSchemaV4Validator(validatorType, SCHEMA_PATH_THIS, jsonNode, PARENT_VALIDATOR_NONE, getRoot());
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new JsonSchemaException(ioe);
        }
    }

    @FunctionalInterface
    private interface CheckedNodeSupplier {
        JsonNode supply() throws IOException;
    }


}
