package io.github.l4rue.cime.internal.dom;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple {@link NodeList} implementation backed by an array list.
 *
 * @author dingyh
 */
public class StandardNodeList implements NodeList {

    private final List<Node> nodes = new ArrayList<Node>();

    /**
     * Adds a node to the end of this list.
     *
     * @param node node to append
     */
    public void add(Node node) {
        nodes.add(node);
    }

    /**
     * Returns the node at the requested index.
     *
     * @param index zero-based node index
     * @return matching node, or {@code null} when the index is outside the list bounds
     */
    public Node item(int index) {
        if (index < 0 || index >= nodes.size()) {
            return null;
        }
        return nodes.get(index);
    }

    /**
     * Returns the number of nodes stored in this list.
     *
     * @return node count
     */
    public int getLength() {
        return nodes.size();
    }
}
