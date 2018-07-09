package com.testwa.core.base.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public abstract class BaseEntity implements Entity, Serializable {

    private Long id;

    @JsonIgnore
    private Boolean enabled;

}