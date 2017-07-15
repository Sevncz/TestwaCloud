package com.testwa.distest.server.mvc.model.params;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 07/01/2017.
 */
public class QueryFilters {

    private List<SubQueryFilter> andFilter = new ArrayList<>();
    private List<SubQueryFilter>  orFilter = new ArrayList<>();

    public QueryFilters(List<Map<String, Object>> and, List<Map<String, Object>> or) {
        if(and != null){
            for(Map<String, Object> k : and){
                if(k != null && k.size() == 3){
                    this.andFilter.add(new SubQueryFilter(k));
                }

            }
        }
        if(or != null){
            for(Map<String, Object> k : or){
                if(k != null && k.size() == 3){
                    this.orFilter.add(new SubQueryFilter(k));
                }
            }

        }
    }

    public List<SubQueryFilter> getAndFilter() {
        return andFilter;
    }

    public void setAndFilter(List<SubQueryFilter> andFilter) {
        this.andFilter = andFilter;
    }

    public List<SubQueryFilter> getOrFilter() {
        return orFilter;
    }

    public void setOrFilter(List<SubQueryFilter> orFilter) {
        this.orFilter = orFilter;
    }
}
