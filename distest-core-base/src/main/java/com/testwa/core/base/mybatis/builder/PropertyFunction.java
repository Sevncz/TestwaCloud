package com.testwa.core.base.mybatis.builder;

import java.io.Serializable;
import java.util.function.Function;

public interface PropertyFunction<T, R> extends Function<T, R>, Serializable {

}