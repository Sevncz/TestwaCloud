package com.testwa.distest.server.service.apitest.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.core.utils.Identities;
import com.testwa.distest.config.DisFileProperties;
import com.testwa.distest.postman.PostmanReader;
import com.testwa.distest.postman.model.PostmanCollection;
import com.testwa.distest.server.entity.Api;
import com.testwa.distest.server.entity.Postman;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.PostmanMapper;
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
public class PostmanService extends BaseService<Postman, Long> {

    @Autowired
    private PostmanMapper postmanMapper;
    @Autowired
    private User currentUser;
    @Autowired
    private DisFileProperties disFileProperties;

    @Transactional(propagation = Propagation.REQUIRED)
    public long save(long projectId, String collectionPath, String postmanId) {
        Postman postman = new Postman();
        postman.setPostmanId(postmanId);
        postman.setCollectionPath(collectionPath);
        postman.setProjectId(projectId);
        postman.setCreateBy(currentUser.getId());
        postman.setCreateTime(new Date());
        postman.setEnabled(true);
        return postmanMapper.insert(postman);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public long upload(Long projectId, MultipartFile collectionFile) throws Exception {

        Path relativeDir = Paths.get(Postman.class.getSimpleName(), projectId.toString(), Identities.uuid2());
        Path absoluteDir = Paths.get(disFileProperties.getDist(), relativeDir.toString());
        if (!Files.exists(absoluteDir)) {
            Files.createDirectories(absoluteDir);
        }

        Path absoluteCollection = Paths.get(absoluteDir.toString(), collectionFile.getOriginalFilename());
        // 保存文件
        Files.copy(collectionFile.getInputStream(), absoluteCollection, StandardCopyOption.REPLACE_EXISTING);
        // 读取文件
        PostmanReader reader = new PostmanReader();
        PostmanCollection collection = reader.readCollectionFile(absoluteCollection.toString());

        // 检查Postman文件是否存在
//        String postmanId = collection.getPostmanId();
//        PostmanCollection collectionOld = getPostmanCollection(postmanId);
//        if(collectionOld != null) {
//            throw new BusinessException(ResultCode.ILLEGAL_OP, "Postman文件已存在");
//        }

        // 保存collection
//        PostmanCollection cc = postmanCollectionRepository.insert(collection);

        // 保存对象
//        Path relativeCollection = Paths.get(relativeDir.toString(), collectionFile.getOriginalFilename());

//        return save(projectId, relativeCollection.toString(), cc.getId());

        return 1L;
    }

    public PostmanCollection getPostmanCollection(String postmanId) {
//        return postmanCollectionRepository.get(postmanId);
        return null;
    }

}
