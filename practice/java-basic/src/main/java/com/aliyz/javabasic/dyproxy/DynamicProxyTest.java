package com.aliyz.javabasic.dyproxy;

import org.junit.Test;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Created by aliyz at 2020-07-04 15:12
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class DynamicProxyTest {

    @Test
    public void subjectProxy() throws Exception {

        // 1. 获取目标对象，这里有两种法：
        // 1-1. new 关键字直接创建
        Subject subject = new SubjectImpl();
        // 1-2. 反射实例化对象
        Subject rf_subject = (Subject) Class.forName("com.aliyz.javabasic.dyproxy.SubjectImpl").newInstance();

        // 2. 创建代理对象
        SubjectProxy subjectProxy = new SubjectProxy(rf_subject);

        // 3. 实例化被代理的目标对象
        Subject proxyInstance = subjectProxy.getProxyInstance();

        // 4. 调用目标对象的方法
        proxyInstance.sayHello("world");
    }

    @Test
    public void cgSubjectProxy() throws Exception {
        // 1. 获取目标对象
        CGSubject subject = new CGSubject();

        // 2. 创建代理对象
        CGSubjectProxy subjectProxy = new CGSubjectProxy(subject);

        // 3. 通过字节码技术动态创建子类实例
        CGSubject cGsubject = subjectProxy.getProxyInstance();

        // 4. 通过代理类调用父类中的方法
        cGsubject.sayHello("world");
    }
}
