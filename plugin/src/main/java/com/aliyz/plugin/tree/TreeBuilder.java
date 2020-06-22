package com.aliyz.plugin.tree;

import com.aliyz.plugin.tree.api.IArc;
import com.aliyz.plugin.tree.api.IJoinNode;
import com.aliyz.plugin.tree.api.INode;
import com.sun.istack.internal.NotNull;

import java.util.*;

public class TreeBuilder {

    private Map<String, INode> node_store = new HashMap<>();

    public Map<String, YzTree> build(@NotNull List<INode> nodes, @NotNull List<IArc> arcs) {
        storeNodes(nodes);
        valid(arcs);

        Map<String, YzTree> forest = new HashMap<>();
        while (!Util.isEmpty(arcs)) {
            String rootIndex = seekRoot(arcs, null);
            YzTree rootYzTree = new YzTree(node_store.get(rootIndex));
            process(rootYzTree, arcs);
            forest.put(rootIndex, rootYzTree);
        }

        return forest;
    }

    public Map<String, YzTree> build(@NotNull List<IJoinNode> jnodes) {
        List<INode> nodes = new ArrayList<>();
        List<IArc> arcs = new ArrayList<>();
        jnodes.forEach(jnode -> {
            nodes.add(jnode);
            arcs.add(new IArc() {
                @Override
                public String getFromIndex() {
                   return jnode.getFromIndex();
                }

                @Override
                public String getToIndex() {
                    return jnode.getToIndex();
                }
            });
        });

        return build(nodes, arcs);
    }

    private void storeNodes(List<INode> nodes) {
        if (Util.isEmpty(nodes)) {
            throw new RuntimeException(String.format("Node list can not be empty."));
        }

        nodes.forEach(iNode -> {
            node_store.put(iNode.getIndex(), iNode);
        });
    }

    private void valid (List<IArc> arcs) {
        if (Util.isEmpty(arcs)) {
            throw new RuntimeException(String.format("Arc list can not be empty."));
        }

        Set<String> set = new HashSet<>();
        arcs.forEach(arc -> {
            if (!set.add(String.format("%s:%s", arc.getFromIndex(), arc.getToIndex()))) {
                throw new RuntimeException(String.format("Illegal duplicate Arc data: %s.", arc));
            }
            if (!set.add(String.format("%s:%s", arc.getToIndex(), arc.getFromIndex()))) {
                throw new RuntimeException(String.format("Illegal ring Arc data: %s.", arc));
            }
        });
    }

    private String seekRoot(List<IArc> arcs, String hit) {
        if (hit == null) {
            return seekRoot(arcs, arcs.get(0).getFromIndex());
        }

        for (IArc arc : arcs) {
            if (arc.getToIndex().equals(hit)) {
                return seekRoot(arcs, arc.getFromIndex());
            }
        }
        return hit;
    }

    private void process (YzTree rootTree, List<IArc> arcs) {
        int i = 0;
        while (i < arcs.size()) {
            INode rootNode = rootTree.getNode();
            IArc arc = arcs.get(i);
            if (rootNode.getIndex().equals(arc.getFromIndex())) {
                YzTree child = new YzTree(getAndCreateNode(arc.getToIndex()));
                rootTree.addChildren(child);
                arcs.remove(i);
                process(child, arcs);
                i = 0;
            } else {
                i++;
            }
        }
    }

    private INode getAndCreateNode(String index) {
        INode node = node_store.get(index);
        if (node == null) {
            node = new INode() {
                @Override
                public String getIndex() {
                    return index;
                }
            };
        }
        return node;
    }
}
