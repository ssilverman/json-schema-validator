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

    ValidatorNode getRoot();

    /**
     * Gives you the list of children of this validator node.
     * Each of them will have a list of its own children, so this is effectively a tree.
     *
     * @return a list containing all the children of this node
     */
    List<ValidatorNode> getChildren();

    void addChild(ValidatorNode validatorNode);

    String getSchemaPath();

    JsonNode getJsonNode();

    ValidatorNode getParent();

    JsonNode findReference(String reference);

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

}
