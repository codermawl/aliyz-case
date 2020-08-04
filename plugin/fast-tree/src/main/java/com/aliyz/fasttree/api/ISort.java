package com.aliyz.fasttree.api;

import com.sun.istack.internal.NotNull;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * Interface for Tree node, It supports comparison with other INode.
 * @version 2019-11-28 11:07
 * @author: mawl
 * @Copyright: 2019 www.aliyz.com Inc. All rights reserved.
 *
 * Note: this content is limited to internal circulation of 北京启迪区块链科技发展有限公司,
 * and is forbidden to be disclosed and used for other commercial purposes
 */
public interface ISort {

    /**
     * Returns the sort comparison results with this object, in the
     * given target INode.
     *
     * @param o INode to be compared
     * @return the remaining delay; zero or negative values indicate
     * that the delay has already elapsed
     */
    @NotNull
    int compareTo(ISort o);
}
