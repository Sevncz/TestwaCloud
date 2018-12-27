package com.testwa.distest.server.service.apitest.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.core.utils.Identities;
import com.testwa.distest.config.DisFileProperties;
import com.testwa.distest.postman.PostmanReader;
import com.testwa.distest.postman.model.PostmanEnvironment;
import com.testwa.distest.server.entity.Api;
import com.testwa.distest.server.entity.Postman;
import com.testwa.distest.server.entity.PostmanEnv;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.PostmanEnvMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;

/**
 * postman的service类
 * @author wen
 * @create 2018-12-04 17:28
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class PostmanEnvService extends BaseService<Api, Long> {

    @Autowired
    private PostmanEnvMapper postmanEnvMapper;
    @Autowired
    private User currentUser;
    @Autowired
    private DisFileProperties disFileProperties;

    @Transactional(propagation = Propagation.REQUIRED)
    public PostmanEnv save(long projectId, String environmentPath, String envId) {
        PostmanEnv postmanEnv = new PostmanEnv();
        postmanEnv.setEnvId(envId);
        postmanEnv.setEnvironmentPath(environmentPath);
        postmanEnv.setProjectId(projectId);
        postmanEnv.setCreateBy(currentUser.getId());
        postmanEnv.setCreateTime(new Date());
        postmanEnv.setEnabled(true);
        postmanEnvMapper.insert(postmanEnv);
        return postmanEnv;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public long upload(Long projectId, MultipartFile environmentFile) throws Exception {

        Path relativeDir = Paths.get(Postman.class.getSimpleName(), projectId.toString(), Identities.uuid2());
        Path absoluteDir = Paths.get(disFileProperties.getDist(), relativeDir.toString());
        if (!Files.exists(absoluteDir)) {
            Files.createDirectories(absoluteDir);
        }

        Path absoluteEnvironment = Paths.get(absoluteDir.toString(), environmentFile.getOriginalFilename());
        // 保存文件
        Files.copy(environmentFile.getInputStream(), absoluteEnvironment, StandardCopyOption.REPLACE_EXISTING);
        // 读取文件
        PostmanReader reader = new PostmanReader();
        PostmanEnvironment environment = reader.readEnvironmentFile(absoluteEnvironment.toString());
        //要保存到数据库，这里把原始id放到envId字段
//        environment.setEnvId(environment.getId());
//        environment.setId(null);

        // 检查PostmanEnv文件是否存在
//        String envId = environment.getEnvId();
//        PostmanEnvironment environmentOld = getPostmanEnvironment(envId);
//        if(environmentOld != null) {
//            // 如果存在，则删除，重新insert
//            postmanEnvironmentRepository.deleteByIds(envId);
//        }
        // 保存
//        PostmanEnvironment ee = postmanEnvironmentRepository.insert(environment);

        // 保存对象
//        Path relativeEnvironment = Paths.get(relativeDir.toString(), environmentFile.getOriginalFilename());

//        return save(projectId, relativeEnvironment.toString(), ee.getId());
        return 0L;
    }

    public PostmanEnvironment getPostmanEnvironment(String envId) {
//        return postmanEnvironmentRepository.get(envId);
        return null;
    }


}
