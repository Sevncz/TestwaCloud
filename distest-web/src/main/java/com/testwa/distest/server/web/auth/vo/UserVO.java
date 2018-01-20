package com.testwa.distest.server.web.auth.vo;

import lombok.Data;

/**
 * Created by wen on 20/10/2017.
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
}
