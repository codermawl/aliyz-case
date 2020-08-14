package com.aliyz.fabric.sdk.utils;

import java.util.Collection;
import java.util.Set;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Created by aliyz at 2020-08-06 15:19
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class EmptyUtils {

    public static boolean isEmpty (Collection c) {
        return c == null || c.size() == 0;
    }

    public static boolean isEmpty (Object ... array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty (String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isBlack (String s) {
        return s == null || s.trim().length() == 0;
    }

}
