package com.testwa.distest.server.web.project.vo;

import lombok.Data;
import lombok.ToString;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ToString
public class ProjectStatisElapsedTimeLineVO {

    private List<Long> time;
    private List<String> date;

    public void add(Long second, Date day) {
        if(this.time == null) {
            this.time = new ArrayList<>();
        }
        if(this.date == null) {
            this.date = new ArrayList<>();
        }
        this.time.add(second);
        this.date.add(new DateTime(day).toString("yyyy-MM-dd"));
    }
}
