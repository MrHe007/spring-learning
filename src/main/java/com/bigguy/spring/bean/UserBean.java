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
package com.bigguy.spring.bean;

import lombok.Data;
import org.springframework.context.annotation.Bean;

/**
 * @author ：hechen
 * @data ：2020/9/30
 * @description ：
 */
@Data
public class UserBean {

    private Long id;

    private String username;

    private String password;

}
