package com.testwa.distest.server.mvc.service;

import com.testwa.distest.server.mvc.beans.QueryFilters;
import com.testwa.distest.server.mvc.beans.SubQueryFilter;
import com.testwa.distest.server.mvc.repository.CommonRepository;
import com.testwa.distest.server.mvc.beans.QueryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 2016/11/12.
 */
public class BaseService {
    private static final Logger log = LoggerFactory.getLogger(BaseService.class);

    protected Query buildQuery(List<Map<String, String>> filters) {
        Query query = new Query();
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(Criteria.where("disable").is(true));
        if(filters != null){
            for(Map params : filters){
                if(params.size() == 3){
                    String matchMode = (String) params.getOrDefault("matchMode", "contains");
                    String filtername = (String) params.getOrDefault("name", "");
                    Object value = params.getOrDefault("value", "");
                    log.info("matchMode: {}", matchMode);
                    log.info("filtername: {}", filtername);
                    log.info("value: {}", value);
                    addCriteria(andCriteria, matchMode, filtername, value);

                }
            }
        }
        Criteria criteria = new Criteria();
        Criteria[] criteriaArr = new Criteria[andCriteria.size()];
        criteriaArr = andCriteria.toArray(criteriaArr);
        criteria.andOperator(criteriaArr);
        query.addCriteria(criteria);
        return query;
    }


    protected Query buildQuery(QueryFilters filters) {
        Query query = new Query();
        List<Criteria> wheres = new ArrayList<>();
        Criteria where = new Criteria();
        List<Criteria> andCriterias = new ArrayList<>();
        andCriterias.add(Criteria.where("disable").is(true));
        List<SubQueryFilter> andFilters = filters.getAndFilter();
        if(andFilters != null){
            for(SubQueryFilter params : andFilters){
                String matchMode = params.getMatchMode();
                String filtername = params.getName();
                Object value = params.getValue();
                log.info("matchMode: {}", matchMode);
                log.info("filtername: {}", filtername);
                log.info("value: {}", value);
                addCriteria(andCriterias, matchMode, filtername, value);
            }
        }
//        Criteria criteria = new Criteria();
//        criteria.andOperator(criteriaArr);
//        query.addCriteria(criteria);
        if (andCriterias.size() > 0) {
            where.andOperator(andCriterias.toArray(new Criteria[0]));
        }

        List<Criteria> orCriterias = new ArrayList<>();
        List<SubQueryFilter> orFilters = filters.getOrFilter();
        if(orFilters != null){
            for(SubQueryFilter params : orFilters){
                String matchMode = params.getMatchMode();
                String filtername = params.getName();
                Object value = params.getValue();
                log.info("matchMode: {}", matchMode);
                log.info("filtername: {}", filtername);
                log.info("value: {}", value);
                addCriteria(orCriterias, matchMode, filtername, value);
            }
        }
        if (orCriterias.size() > 0) {
            where.orOperator(orCriterias.toArray(new Criteria[0]));
        }
        wheres.add(where);
        if (wheres.size() > 0) {
            query.addCriteria(new Criteria().andOperator(wheres.toArray(new Criteria[0])));
        }

        return query;
    }

    private void addCriteria(List<Criteria> andCriteria, String matchMode, String filtername, Object value) {
        switch (QueryOperator.valueOf(matchMode.trim().toLowerCase())){
            case contains:
                andCriteria.add(Criteria.where(filtername).regex((String) value));
                break;
            case in:
                try {
                    if(value != null){
                        List tempValue = (List)value;
                        Object[] objArr = new Object[tempValue.size()];
                        andCriteria.add(Criteria.where(filtername).in(tempValue.toArray(objArr)));
                    }
                }catch (Exception e){
                    log.error("Params error, maybe, value type is not list", e);
                }
                break;
            case is:
                if(value != null){
                    andCriteria.add(Criteria.where(filtername).is(value));
                }
                break;
            case startwith:
                break;
            case endwith:
                break;
        }
    }

    protected void disableById(String id, CommonRepository repository){
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));

        Update update = new Update();
        update.set("disable", false);
        update.set("modifyDate", new Date());

        repository.updateMulti(query, update);
    }
}
