package com.aliyz.javabasic.dyproxy;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Created by aliyz at 2020-07-04 15:08
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class SubjectImpl implements Subject {

    public void sayHello(String param) {
        System.out.println(String.format("Hello, %s!", param));
    }
}
