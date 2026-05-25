package com.scnu.springbootjdk17demo.dto.admin;

import lombok.Data;

@Data
public class ScheduleItemDTO {
    private Integer weekday;
    private Integer periodStart;
    private Integer periodEnd;
    private Integer weekStart;
    private Integer weekEnd;
    private String location;
    private String roomUrl;
}