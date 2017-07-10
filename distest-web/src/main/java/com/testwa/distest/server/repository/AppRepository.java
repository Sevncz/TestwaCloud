package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.App;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
public interface AppRepository extends CommonRepository<App, Serializable> {

    List<App> findByUserId(String userId);

}
