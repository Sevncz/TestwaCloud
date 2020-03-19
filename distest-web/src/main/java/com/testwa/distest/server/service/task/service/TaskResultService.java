package com.testwa.distest.server.service.task.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.entity.TaskResult;
import com.testwa.distest.server.mapper.TaskResultMapper;
import com.testwa.distest.server.service.fdfs.service.FdfsStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by wen on 24/10/2017.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class TaskResultService extends BaseService<TaskResult, Long> {

    @Autowired
    private TaskResultMapper taskResultMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private FdfsStorageService fdfsStorageService;
    @Value("${base.report.dir}")
    private String reportDir;

    public List<TaskResult> listByCode(Long taskCode) {
        return taskResultMapper.selectListByProperty(TaskResult::getTaskCode, taskCode);
    }

    public List<TaskResult> listByProjectId(Long projectId) {
        return  taskResultMapper.selectListByProperty(TaskResult::getProjectId, projectId);
    }

    @Async
    public void generateProject(Long projectId) {
        // 获得所有任务
        List<Task> taskList = taskService.findFinishList(projectId);

        Path resultPath = Paths.get(reportDir, projectId.toString(), "result");
        Path reportPath = Paths.get(reportDir, projectId.toString(), "report");
        for (Task task : taskList) {
            List<TaskResult> results = listByCode(task.getTaskCode());
            List<CompletableFuture<Boolean>> responseFutures = results.stream().map(r -> fdfsStorageService.downloadResult(r, resultPath)).collect(Collectors.toList());
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(responseFutures.toArray(new CompletableFuture[responseFutures.size()]));
            CompletableFuture<List<Boolean>> allResponseFuture = allFutures.thenApply(v -> {
                return responseFutures.stream()
                        .map(future -> future.join())
                        .collect(Collectors.toList());
            });
            CompletableFuture<Long> countFuture = allResponseFuture.thenApply(responses -> {
                return responses.stream()
                        .filter(b -> b)
                        .count();
            });
            try {
                log.info("下载结果文件数量：{}", countFuture.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            String[] cmd = {"allure", "generate", resultPath.toString(), "-o", reportPath.toString(), "--clean"};
            try {
                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
