package com.aliyz.plugin.tree.api;

import com.sun.istack.internal.NotNull;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * Interface for Sample tree node，provide uniform standards for all nodes
 * @version 2019-11-28 11:07
 * @author: mawl
 * @Copyright: 2019 www.aliyz.com Inc. All rights reserved.
 *
 * Note: this content is limited to internal circulation of 北京启迪区块链科技发展有限公司,
 * and is forbidden to be disclosed and used for other commercial purposes
 */
public interface INode {

    /**
     * Returned the node unique index.
     * @return value for node index
     */
    @NotNull
    String getIndex();
}
