package com.testwa.distest.common.serializer;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;

public class StringValueToLongSerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType,
                      int features) throws IOException {
        Long value = null;
        if(object != null){
            if(object instanceof String){
                String obj_str = (String) object;
                if(StringUtils.isNotEmpty(obj_str)){
                    value = Long.parseLong(obj_str);
                }
            }
            if(object instanceof Long){
                value = (Long) object;
            }
        }
        serializer.write(value);
    }
}
