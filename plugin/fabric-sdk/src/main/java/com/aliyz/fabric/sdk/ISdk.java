package com.aliyz.fabric.sdk;

import static java.lang.String.format;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p>
 *     1、通道
 *      1.1、创建通道
 *      1.2、停止通道
 *      1.3、更新通道
 *      1.4、Peer加入通道
 *      1.5、Peer退出通道
 *
 *     2、链码
 *      2.1、打包链码
 *      2.2、安装链码
 *      2.3、查询链码安装结果
 *      2.4、审批链码
 *      2.5、查询链码审批结果
 *      2.6、提交链码安装
 *      2.7、查询提交链码安装结果
 *      2.8、链码初始化
 *      2.9、链码查询
 *      2.10、链码调用
 *      2.11、链码更新
 *      2.12、链码下链
 * </p>
 * Create by aliyz at 2020-08-04 10:57
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public abstract class ISdk {

    protected static void out(String format, Object... args) {

        System.err.flush();
        System.out.flush();

        System.out.println(format(format, args));
        System.err.flush();
        System.out.flush();

    }

}
