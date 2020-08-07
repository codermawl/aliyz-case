package com.aliyz.fabric.sdk.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Created by mawl at 2020-08-06 18:53
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class CompareUtils {

    /**
     * 判断两个集合是否相等（彼此包含）: 允许重复元素有序的List、ArrayList, 不允许元素重复无序的Set
     *
     * @param ca
     * @param cb
     * @return
     */
    public static boolean isContainEachother(Set<String> ca, Set<String> cb) {
        if (ca != null && cb != null) {
            if (ca.size() == cb.size()) {
                Set setA = (Set) ca, setB = (Set) cb; // 去重复元素
                if (setA.size() == setB.size()) {
                    return setA.containsAll(setB);
                    // return setB.containsAll(setA); //setA与setB是包含关系
                    // 证明两个set是相等的
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if(ca==null && cb==null){
            return true;
        } else {
            return false;
        }
    }

    /**
     * 通过HashCode比较两个集合是否相等
     *
     * @param ca
     * @param cb
     * @return
     */
    public static boolean isEqualComparedByHashcode(Collection ca, Collection cb) {
        if (ca != null && cb != null) {
            if (ca.size() == cb.size()) {
                Set setA = (Set) ca, setB = (Set) cb; // 去重复元素
                if (setA.size() == setB.size()) {
                    int flag = 0;                     // 标记次数
                    List listA = (List) setA, listB = (List) setB; // 转成有序集合
                    Collections.sort(listA); // 为集合listA排序
                    Collections.sort(listB); // 为集合listB排序
                    for (int i = 0; i < listA.size(); i++) {
                        int hashCodeA = listA.get(i).hashCode(), hashCodeB = listB
                                .get(i).hashCode();
                        if (hashCodeA != hashCodeB)
                            flag++;
                    }
                    if (flag > 0)
                        return false;
                    else
                        return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }else if(ca==null&&cb==null){
            return true;
        }
        return false;
    }

}

