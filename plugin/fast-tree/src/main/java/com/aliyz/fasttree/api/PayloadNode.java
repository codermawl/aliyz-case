package com.aliyz.fasttree.api;

import com.sun.istack.internal.NotNull;
import lombok.Data;

@Data
public abstract class PayloadNode<T> implements INode {

    private T payload;

    public PayloadNode(@NotNull T t) {
        this.payload = t;
    }
}
