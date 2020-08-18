package com.aliyz.javabasic.dyproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Created by aliyz at 2020-07-04 15:10
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class SubjectProxy implements InvocationHandler{

    private Subject subject;

    public SubjectProxy(Subject subject) {
        this.subject = subject;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("--------------before-------------");
        Object invoke = method.invoke(subject, args);
        System.out.println("--------------after-------------");
        return invoke;
    }

    /**
     * @Description:  通过反射得到一个代理类的实例
     **/
    public Subject getProxyInstance() {
        return (Subject) Proxy.newProxyInstance(subject.getClass().getClassLoader(),
                subject.getClass().getInterfaces(), this);
    }
}
