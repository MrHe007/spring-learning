package com.bigguy.spring.config;

import com.bigguy.spring.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @Author bigguy
 * @Date 2020/9/19
 **/
@Configuration
// 扫描包下的 compent
@ComponentScan(basePackages = "com.bigguy.spring")
public class SpringContext {

    @Bean
    public User user(){

        return new User("jeck");
    }

}
