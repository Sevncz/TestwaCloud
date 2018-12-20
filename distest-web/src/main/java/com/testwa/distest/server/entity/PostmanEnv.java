package com.testwa.distest.server.entity;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import lombok.Data;

@Data
@Table(name="dis_postman_env")
public class PostmanEnv extends ProjectBase{

    @Column(name = "env_id")
    private String envId;
    @Column(name = "environment_path")
    private String environmentPath;

}
