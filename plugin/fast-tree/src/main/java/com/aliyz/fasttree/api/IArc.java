package com.aliyz.fasttree.api;

import com.sun.istack.internal.NotNull;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * Interface for Sample tree arc，provide uniform standards for all arcs
 * @version 2019-11-28 11:07
 * @author: mawl
 * @Copyright: 2019 www.aliyz.com Inc. All rights reserved.
 *
 * Note: this content is limited to internal circulation of @me,
 * and is forbidden to be disclosed and used for other commercial purposes
 */
public interface IArc {

    /**
     * Returns the index of the start point of the edge, external programs are required to
     * construct by themselves
     * @return value for from-node index
     */
    @NotNull
    String getFromIndex();

    /**
     * Returns the index of the end point of the edge, external programs are required to
     * construct by themselves
     * @return value for to-node index
     */
    @NotNull
    String getToIndex();
}
