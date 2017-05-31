package com.networknt.schema.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class RootValidatorNode implements ValidatorNode {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchema.class);
    private static final Pattern intPattern = Pattern.compile("^[0-9]+$");

    private final String schemaPath;
    private final JsonNode schemaNode;
    private final ValidatorNode parentSchema;
    protected final List<ValidatorNode> children;

    private RootValidatorNode(String schemaPath, JsonNode schemaNode, ValidatorNode parentSchema) {
        this.schemaPath = schemaPath;
        this.schemaNode = schemaNode;
        this.parentSchema = parentSchema;
        this.children = new ArrayList<>();
    }

    // fixme: should I create an abstract superclass for all of this?? - yes, yes you absolutely should
    @Override
    public Set<ValidationMessage> validate(JsonNode rootNode) {
        return null;
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        return null;
    }

    @Override
    public String getPropertyName() {
        return "";
    }

    @Override
    public List<ValidatorNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void addChild(ValidatorNode validatorNode) {
    }

    @Override
    public String getSchemaPath() {
        return schemaPath;
    }

    @Override
    public JsonNode getSchemaNode() {
        return schemaNode;
    }

    @Override
    public ValidatorNode getParentSchema() {
        return parentSchema;
    }

    // fixme: should I create an abstract superclass for little bullshit like this??

    public static class Factory implements ValidatorNodeFactory {
        /**
         * Gives you a <b>new</b> instance of your implementation class.
         * You should consider keeping your constructors private and only returning instances
         * of your implementations through this method.
         *
         * @param schemaPath a JSON Pointer expression indicating the path from the root of the tree
         *                  this node belongs to
         * @param schemaNode
         * @param parent
         * @return
         */
        @Override
        public ValidatorNode newInstance(String schemaPath, JsonNode schemaNode, ValidatorNode parent) {
            return new RootValidatorNode(schemaPath, schemaNode, parent);
        }
    }

}
