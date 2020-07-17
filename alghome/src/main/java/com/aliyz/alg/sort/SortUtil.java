package com.aliyz.alg.sort;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p>排序工具类</p>
 * Created by aliyz at 2020-06-24 16:48
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
class SortUtil {

    static String toPrint(int[] arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i=0; i<arr.length; i++) {
            if (i == arr.length - 1) {
                sb.append(arr[i]);
            } else {
                sb.append(arr[i]).append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
