package com.testwa.core.base.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.annotation.Column;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public abstract class BaseEntity implements Entity, Serializable {

    @Column(value = "id")
    private Long id;

    @JsonIgnore
    @Column(value = "enabled")
    private Boolean enabled;

}