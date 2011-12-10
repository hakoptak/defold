package com.dynamo.cr.sceneed.core;

import java.util.ArrayList;
import java.util.List;

public class NodeUtil {

    public static interface IdFetcher<T extends Node> {
        String getId(T node);
    }

    public static <T extends Node> String getUniqueId(List<T> nodes, String baseId, IdFetcher<T> idFetcher) {
        List<String> ids = new ArrayList<String>(nodes.size());
        for (T node: nodes) {
            ids.add(idFetcher.getId(node));
        }
        String id = baseId;
        String format = "%s%d";
        int i = 1;
        while (ids.contains(id)) {
            id = String.format(format, baseId, i);
            ++i;
        }
        return id;
    }

    public static String getUniqueId(Node parent, String baseId, IdFetcher<Node> idFetcher) {
        return getUniqueId(parent.getChildren(), baseId, idFetcher);
    }

    /**
     * Returns the node to be selected if the supplied node is removed.
     * @param node Node to be removed
     * @return Node to be selected, or null
     */
    public static Node getSelectionReplacement(Node node) {
        Node parent = node.getParent();
        if (parent != null) {
            List<Node> children = parent.getChildren();
            int index = children.indexOf(node);
            if (index + 1 < children.size()) {
                ++index;
            } else {
                --index;
            }
            Node selected = null;
            if (index >= 0) {
                selected = children.get(index);
            } else {
                selected = parent;
            }
            return selected;
        }
        return null;
    }
}
