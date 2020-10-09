package com.bigguy.spring.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description:
 * @Author bigguy
 * @Date 2020/9/19
 **/
@Data
@NoArgsConstructor
public class User {

    private Long id;

    private String username;

    public User(String username) {
        this.username = username;
    }
}
