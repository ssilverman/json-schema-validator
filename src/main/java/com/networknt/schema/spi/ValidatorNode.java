package com.networknt.schema.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

import java.util.List;

/**
 * <p>The ValidatorNode Service interface.</p>
 *
 * <p>As it parses the JSON Schema tree, </p>
 */
public interface ValidatorNode {

    String AT_ROOT = "$";

    /**
     * Validate the given root JsonNode, starting at the root of the data path.
     *
     * @param rootNode JsonNode
     * @return A list of ValidationMessage if there is any validation error, or an empty
     * list if there is no error.
     */
    List<ValidationMessage> validate(JsonNode rootNode);

    /**
     * Validate the given JsonNode, the given node is the child node of the root node at given
     * data path.
     *
     * @param node     JsonNode
     * @param rootNode JsonNode
     * @param at       String
     * @return A list of ValidationMessage if there is any validation error, or an empty
     * list if there is no error.
     */
    List<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at);

    /**
     * Tells you the "property name" that a JSON Schema tree uses to refer to this validator.
     * For example, if this were the {@link ItemsValidator}, the property name would be
     * "items", because in JSON Schema you specify valid items and their schemas
     * by saying something like {@code {items: [item1, item2, item3]}.}
     * @return
     */
    String getPropertyName();

    /**
     * Gives you the list of children of this validator node.
     * Each of them will have a list of its own children, so this is effectively a tree.
     *
     * @return a list containing all the children of this node
     */
    List<ValidatorNode> getChildren();

    void addChild(ValidatorNode validatorNode);

    String getSchemaPath();

    JsonNode getSchemaNode();

    ValidatorNode getParentSchema();

    ValidatorNode getRootSchema();

}
