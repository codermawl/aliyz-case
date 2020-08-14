package com.aliyz.demoweb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Created by aliyz at 2020-08-03 14:26
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = {"com.tusdao.**.mapper"})
public class DemoApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /**
     * 添加tomcat服务器中运行支持
     * visit http://localhost:8080/demo-web/api/monitor/alive
     * @param builder
     * @return
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(DemoApplication.class);
    }
}
