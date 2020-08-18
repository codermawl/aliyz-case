package com.aliyz.javabasic.dyproxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Created by aliyz at 2020-07-04 16:47
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class CGSubjectProxy implements MethodInterceptor {

    private CGSubject subject;

    public CGSubjectProxy(CGSubject subject) {
        this.subject = subject;
    }

    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("--------------CG_before-------------");
        // 这里不使用 invoke方法，容易 OOM
        Object o1 = methodProxy.invokeSuper(o, objects);
        System.out.println("--------------CG_after-------------");
        return o1;
    }

    /**
     * @Description:  通过反射得到一个代理类的实例
     **/
    public CGSubject getProxyInstance() {
        Enhancer enhancer = new Enhancer();

        // 1. 设置需要创建子类的类
        enhancer.setSuperclass(subject.getClass());

        // 2. 设置方法拦截器代理
        enhancer.setCallback(this);

        // 3. 通过字节码技术动态创建子类实例
        return  (CGSubject) enhancer.create();
    }
}
