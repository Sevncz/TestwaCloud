package com.testwa.core.base.mybatis;import java.io.Serializable;import java.lang.reflect.Field;import java.util.ArrayList;import java.util.Collection;import java.util.List;import java.util.Map;import com.google.common.base.Joiner;import com.testwa.core.base.annotation.Column;import com.testwa.core.base.annotation.TableName;import org.apache.commons.lang3.ArrayUtils;import org.apache.ibatis.jdbc.SQL;import org.springframework.util.StringUtils;public class SqlProvider {    public String insert(Map<String, Object> params) {        String tableName = (String) params.get("__tableName__");        Map<String, Object> data = (Map<String, Object>) params.get("data");        return new SQL() {            {                INSERT_INTO(tableName);                data.forEach((k,v) -> {                    VALUES(k, "#{data."+k+"}");                });            }        }.toString();    }    public String update(Map<String, Object> params) {        String tableName = (String) params.get("__tableName__");        Map<String, Object> data = (Map<String, Object>) params.get("data");        return new SQL() {            {                UPDATE(tableName);                data.forEach((k, v) -> {                    SET(k + " = #{data."+k+"}");                });                WHERE("id = #{key}");            }        }.toString();    }    public String delete(Map<String, Object> params) {        String tableName = (String) params.get("__tableName__");        return new SQL() {            {                DELETE_FROM(tableName);                WHERE("id = #{key}");            }        }.toString();    }    public String deleteAll(Map<String, Object> params) {        String tableName = (String) params.get("__tableName__");        List keys = (List) params.get("keys");        return new SQL() {            {                DELETE_FROM(tableName);                WHERE("id in ("+ Joiner.on(" , ").join(keys) +")");            }        }.toString();    }    public String findList(Map<String, Object> params) {        String tableName = (String) params.get("__tableName__");        List keys = (List) params.get("keys");        return new SQL() {            {                SELECT("*");                FROM(tableName);                if(keys != null){                    WHERE("id in ("+ Joiner.on(" , ").join(keys) +")");                }            }        }.toString();    }    public String findBy(Map<String, Object> params) {        String tableName = (String) params.get("__tableName__");        Map<String, Object> query = (Map<String, Object>) params.get("query");        return new SQL() {            {                SELECT("*");                FROM(tableName);                if(query != null){                    query.forEach((k, v) -> {                        WHERE(k + " = #{query."+k+"}");                    });                }            }        }.toString();    }    public String findOne(Map<String, Object> params) {        String tableName = (String) params.get("__tableName__");        Serializable key = (Serializable) params.get("key");        return new SQL() {            {                SELECT("*");                FROM(tableName);                WHERE("id = "+ key);            }        }.toString();    }    public String count(Map<String, Object> params) {        String tableName = (String) params.get("__tableName__");        return new SQL() {            {                SELECT("count(1) c");                FROM(tableName);            }        }.toString();    }}