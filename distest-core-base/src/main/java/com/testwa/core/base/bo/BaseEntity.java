package com.testwa.core.base.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public abstract class BaseEntity implements Entity, Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @JsonIgnore
    @Column(name = "enabled")
    private Boolean enabled;

}