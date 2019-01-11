package com.testwa.distest.server.startup;

import com.testwa.core.redis.RedisCacheManager;
import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.service.issue.service.IssueService;
import com.testwa.distest.server.service.project.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * issue seq在启动时初始化
 *
 * @author wen
 * @create 2019-01-11 15:20
 */
@Slf4j
@Component
public class IssueSeqInitRunner implements CommandLineRunner {
    @Autowired
    private IssueService issueService;
    @Autowired
    private RedisCacheManager redisCacheManager;
    @Autowired
    private ProjectService projectService;

    @Override
    public void run(String... strings) throws Exception {
        List<Project> projectList = projectService.list();
        if(projectList == null || projectList.isEmpty()) {
            return;
        }
        projectList.forEach( project -> {
            String issueSeqKey = issueService.getIssueSeqRedisKey(project.getId());
            Issue issue = issueService.getIssueMaxSeq(project.getId());
            if(issue == null) {
                redisCacheManager.putString(issueSeqKey, -1, "0");
            }else{
                redisCacheManager.putString(issueSeqKey, -1, issue.getIssueSeq().toString());
            }
        });
    }
}
