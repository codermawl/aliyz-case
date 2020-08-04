package com.aliyz.fasttree;


import com.aliyz.plugin.tree.api.INode;
import com.aliyz.plugin.tree.api.ISort;
import com.sun.istack.internal.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class YzTree {

    private INode node;

    private List<YzTree> childrens;

    public YzTree(@NotNull INode node) {
        this.node = node;
    }

    boolean contains (YzTree t) {
        if (!Util.isEmpty(childrens)) {
            for (YzTree child : childrens) {
                if (child.getNode().getIndex().equals(t.getNode().getIndex())) {
                    return true;
                }
            }
        }
        return false;
    }

    void addChildren (YzTree child) {
        if (child != null) {
            if (this.childrens == null) {
                childrens = new ArrayList<YzTree>();
            }
            if (!contains(child)) {
                this.childrens.add(child);
            }
        }
    }

    public YzTree sort () {
        sortProcess(this.childrens);
        return this;
    }

    private void sortProcess (List<YzTree> trees) {
        if (Util.isEmpty(this.childrens)) {
            return;
        }

        Collections.sort(trees, new Comparator<YzTree>(){
            @Override
            public int compare(YzTree tree1, YzTree tree2) {
                ISort o1 = (ISort) tree1.getNode();
                ISort o2 = (ISort) tree2.getNode();
                return o1.compareTo(o2);
            }
        });

        trees.forEach(tree -> {
            sortProcess(tree.getChildrens());
        });
    }
}
