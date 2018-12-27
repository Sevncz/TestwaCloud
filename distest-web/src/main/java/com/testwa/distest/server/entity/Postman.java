package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import lombok.Data;

@Data
@Table(name="dis_postman")
public class Postman extends ProjectBase{

    @Column(name = "postman_id")
    private String postmanId;
    @Column(name = "collection_path")
    private String collectionPath;

    @JsonIgnore
    @Column(name = "lock_version")
    private Long lockVersion;
}
