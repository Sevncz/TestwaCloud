package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import lombok.Data;

@Data
@TableName("dis_postman")
public class Postman extends ProjectBaseEntity{

    @Column(value = "collection_path")
    private String collectionPath;
    @Column(value = "environment_path")
    private String environmentPath;

}
