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

import com.bigguy.spring.annotation.RountingInjected;
import org.springframework.stereotype.Service;

/**
 * @author ：hechen
 * @data ：2020/11/4
 * @description ：
 */
@Service
public class HelloServiceTest {

    @RountingInjected(value = "helloService2")
//    @Autowired
    private IHelloService helloService;

    public void testSayHello() {
        helloService.sayHello();
    }

}
