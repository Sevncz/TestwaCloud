package com.testwa.distest.server.mongo.service;

import com.testwa.distest.server.mongo.model.MethodRunningLog;
import com.testwa.distest.server.mongo.repository.MethodRunningLogRepository;
import io.rpc.testwa.task.ExecutorAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Created by wen on 16/9/7.
 */
@Service
public class MethodRunningLogService extends BaseService {
    private static final List<Integer> pointOrders = Arrays.asList(ExecutorAction.downloadApp_VALUE,
            ExecutorAction.downloadScript_VALUE,
            ExecutorAction.installApp_VALUE,
            ExecutorAction.launch_VALUE,
            ExecutorAction.run_VALUE,
            ExecutorAction.uninstallApp_VALUE,
            ExecutorAction.complete_VALUE
    );
    private static final Map<Integer, String> pointNames = new HashMap<Integer, String>(){{
        put(ExecutorAction.downloadApp_VALUE, "应用下载");
        put(ExecutorAction.downloadScript_VALUE, "脚本下载");
        put(ExecutorAction.installApp_VALUE, "应用安装");
        put(ExecutorAction.launch_VALUE, "应用启动");
        put(ExecutorAction.run_VALUE, "运行");
        put(ExecutorAction.uninstallApp_VALUE, "应用卸载");
        put(ExecutorAction.complete_VALUE, "完成");
        }
    };

    @Autowired
    private MethodRunningLogRepository methodRunningLogRepository;

    public void save(MethodRunningLog info){
        methodRunningLogRepository.save(info);
    }

    public List<MethodRunningLog> findBy(Long taskCode){
        return methodRunningLogRepository.findByTaskCodeOrderByTimestampAsc(taskCode);
    }

    public List<MethodRunningLog> findBy(Long taskCode, String deviceId) {
        return methodRunningLogRepository.findByTaskCodeAndDeviceIdOrderByMethodOrderAsc(taskCode, deviceId);
    }

    public List<MethodRunningLog> getLogCheckPointList(Long taskCode) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("taskCode").is(taskCode),
                Criteria.where("methodOrder").in(pointOrders),
                Criteria.where("flag").is("start")
        );
        Sort sort = new Sort(Sort.Direction.ASC, "methodOrder","timestamp");
        Query query = new Query();
        query.addCriteria(criatira);
        query.with(sort);
        return methodRunningLogRepository.find(query);
    }

    public List<ExecutorAction> getCheckPoint() {
        return pointOrders.stream().map(ExecutorAction::forNumber).collect(Collectors.toList());
    }

    public String getCheckPointName(Integer pointValue) {
        return pointNames.get(pointValue);
    }

    /**
     *@Description: 方法run的执行时间
     *@Param: [taskCode]
     *@Return: java.util.List<com.testwa.distest.server.mongo.model.MethodRunningLog>
     *@Author: wen
     *@Date: 2018/5/30
     */
    public List<MethodRunningLog> getRunningTime(Long taskCode) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("taskCode").is(taskCode),
                Criteria.where("methodOrder").is(ExecutorAction.run_VALUE)
        );
        Query query = new Query();
        query.addCriteria(criatira);
        return methodRunningLogRepository.find(query);
    }
}
