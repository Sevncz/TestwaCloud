package com.testwa.distest.server.mongo.service;

import com.testwa.distest.server.mongo.repository.CommonRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wen on 2016/11/12.
 */
public class BaseService {
    private static final Logger log = LoggerFactory.getLogger(BaseService.class);

    protected Query buildQueryByCriteria(List<Criteria> andCriteria, List<Criteria> orCriteria) {
        Query query = new Query();
        Criteria criteria = new Criteria();

        if(andCriteria != null && !andCriteria.isEmpty()){
            Criteria[] criteriaArr = new Criteria[andCriteria.size()];
            criteriaArr = andCriteria.toArray(criteriaArr);
            criteria.andOperator(criteriaArr);
        }

        if(orCriteria != null && !orCriteria.isEmpty()){
            Criteria[] criteriaArr = new Criteria[orCriteria.size()];
            criteriaArr = orCriteria.toArray(criteriaArr);
            criteria.orOperator(criteriaArr);
        }

        query.addCriteria(criteria);
        return query;
    }


    protected Query buildQuery(List<String> projectIds, String name) {
        Query query = new Query();
        List<Criteria> andCriteria = new ArrayList<>();
        if(StringUtils.isNotEmpty(name)){
            andCriteria.add(Criteria.where("name").regex(name));
        }
        andCriteria.add(Criteria.where("projectId").in(projectIds));
        andCriteria.add(Criteria.where("disableAll").is(false));

        Criteria criteria = new Criteria();
        Criteria[] criteriaArr = new Criteria[andCriteria.size()];
        criteriaArr = andCriteria.toArray(criteriaArr);
        criteria.andOperator(criteriaArr);
        query.addCriteria(criteria);
        return query;
    }

    protected void disableById(String id, CommonRepository repository){
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));

        Update update = new Update();
        update.set("disableAll", true);
        update.set("modifyDate", new Date());

        repository.updateMulti(query, update);
    }


}
