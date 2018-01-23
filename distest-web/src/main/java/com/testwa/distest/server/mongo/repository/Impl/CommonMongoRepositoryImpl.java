package com.testwa.distest.server.mongo.repository.Impl;

import com.testwa.distest.server.mongo.repository.CommonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 2016/11/5.
 */
public class CommonMongoRepositoryImpl<T, ID extends Serializable> extends SimpleMongoRepository<T,ID> implements CommonRepository<T,ID> {
    protected final MongoOperations mongoTemplate;

    protected final MongoEntityInformation<T, ID> entityInformation;

    public CommonMongoRepositoryImpl(MongoEntityInformation<T, ID> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.mongoTemplate=mongoOperations;
        this.entityInformation = metadata;
    }

    protected Class<T> getEntityClass(){
        return entityInformation.getJavaType();
    }

    @Override
    public Page<T> find(Query query, Pageable p) {
        long total=mongoTemplate.count(query, getEntityClass());
        List<T> list=mongoTemplate.find(query.with(p), getEntityClass());
        return new PageImpl<T>(list, p, total);
    }

    @Override
    public Page<T> find(Criteria criteria, Pageable p) {
        return find(new Query(criteria), p);
    }

    @Override
    public List<T> find(Query query) {
        return mongoTemplate.find(query, getEntityClass());
    }

    @Override
    public Long count(Query query) {
        return mongoTemplate.count(query, getEntityClass());
    }

    @Override
    public T findOne(Query query) {
        return mongoTemplate.findOne(query.limit(1), getEntityClass());
    }

    /**
     * 更新满足条件的所有记录
     * @param query
     * @param update
     */
    @Override
    public void updateMulti(Query query, Update update){
        mongoTemplate.updateMulti(query, update, getEntityClass());
    }

    /**
     * 查找更新,如果没有找到符合的记录,则将更新的记录插入库中
     * @param query
     * @param update
     */
    @Override
    public void updateInser(Query query, Update update){
        mongoTemplate.upsert(query, update, getEntityClass());
    }

}
