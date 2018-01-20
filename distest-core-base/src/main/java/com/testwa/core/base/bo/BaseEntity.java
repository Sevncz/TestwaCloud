package com.testwa.core.base.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public abstract class BaseEntity implements Entity, Serializable {

    private Long id;

}