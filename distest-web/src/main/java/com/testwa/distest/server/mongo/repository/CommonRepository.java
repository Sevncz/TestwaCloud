package com.testwa.distest.server.mongo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 2016/11/5.
 */
@NoRepositoryBean
public interface CommonRepository<T, ID extends Serializable> extends MongoRepository<T, ID>{

    Page<T> find(Query query, Pageable p);

    Page<T> find(Criteria query, Pageable p);

    List<T> find(Query query);

    Long count(Query query);

    T findOne(Query limit);

    void updateMulti(Query query, Update update);

    void updateInser(Query query, Update update);
}
