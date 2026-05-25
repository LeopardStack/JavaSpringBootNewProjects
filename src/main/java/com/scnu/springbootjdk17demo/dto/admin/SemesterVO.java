package com.scnu.springbootjdk17demo.dto.admin;
import lombok.Data;
import java.time.LocalDate;
@Data
public class SemesterVO {
    private Long id;
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer isCurrent;
}