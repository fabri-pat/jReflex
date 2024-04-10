package io.github.fabripat.jreflex.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Dto {
    private Integer integerField;
    private Double doubleField;
    private LocalDate localDateField;
    private LocalDateTime localDateTimeField;
}