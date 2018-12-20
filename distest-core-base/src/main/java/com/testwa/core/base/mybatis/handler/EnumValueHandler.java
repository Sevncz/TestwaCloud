package com.testwa.core.base.mybatis.handler;

import com.testwa.core.base.enums.ValueEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.*;
import java.util.Optional;

@MappedTypes({ValueEnum.class})
public class EnumValueHandler<E extends Enum<?> & ValueEnum>  implements TypeHandler<E> {
    private Class<E> type;

    public EnumValueHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, E e, JdbcType jdbcType) throws SQLException {
        if (e == null) {
            preparedStatement.setNull(i, Types.TINYINT);
        } else {
            preparedStatement.setInt(i, e.getValue());
        }
    }

    @Override
    public E getResult(ResultSet resultSet, String columnName) throws SQLException {
        int columnValue = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : enumOf(columnValue);
    }

    @Override
    public E getResult(ResultSet resultSet, int columnIndex) throws SQLException {
        int columnValue = resultSet.getInt(columnIndex);
        return resultSet.wasNull() ? null : enumOf(columnValue);
    }

    @Override
    public E getResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        int columnValue = callableStatement.getInt(columnIndex);
        return callableStatement.wasNull() ? null : enumOf(columnValue);
    }

    private E enumOf(int code) {
        final Optional<E> codedEnumOpt = ValueEnum.valueOf(type, code);
        if (codedEnumOpt.isPresent()) {
            return codedEnumOpt.get();
        } else {
            throw new IllegalArgumentException("Cannot convert " + code + " to " + type.getSimpleName() + " by code value.");
        }
    }
}