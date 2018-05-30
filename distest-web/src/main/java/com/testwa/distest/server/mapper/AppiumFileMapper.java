package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.AppiumFile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AppiumFileMapper extends BaseMapper<AppiumFile, Long> {


    AppiumFile findOne(@Param("taskCode") Long taskCode, @Param("deviceId") String deviceId);

    List<AppiumFile> fildAll(AppiumFile query);

    int removeFromTask(@Param("taskCode") Long taskCode);

}