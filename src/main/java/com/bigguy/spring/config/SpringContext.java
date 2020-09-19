package com.bigguy.spring.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Author bigguy
 * @Date 2020/9/19
 **/
@Configuration
// 扫描包下的 compent
@ComponentScan(basePackages = "com.bigguy.spring.service")
public class SpringContext {

}
