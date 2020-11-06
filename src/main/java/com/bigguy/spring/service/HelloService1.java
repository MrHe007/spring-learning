/*
Copyright (C) 2011-$today.year. ShenZhen IBOXCHAIN Information Technology Co.,Ltd.

All right reserved.

This software is the confidential and proprietary
information of IBOXCHAIN Company of China.
("Confidential Information"). You shall not disclose
such Confidential Information and shall use it only
in accordance with the terms of the contract agreement
you entered into with IBOXCHAIN inc.

*/
package com.bigguy.spring.service;

import org.springframework.stereotype.Service;

/**
 * @author ：hechen
 * @data ：2020/11/4
 * @description ：
 */
@Service
public class HelloService1 implements IHelloService {
    @Override
    public void sayHello() {
        System.out.println("HelloService  -- 111111");
    }
}
