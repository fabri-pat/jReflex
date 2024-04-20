package io.github.fabripat.jreflex.testdomain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DtoWithBuilder {
    private Integer integerField;
    private Double doubleField;
    private LocalDate localDateField;
    private LocalDateTime localDateTimeField;
}
