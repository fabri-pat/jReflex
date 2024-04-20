package io.github.fabripat.jreflex.testdomain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor(force = true)
public class ImmutableDto {
    private final Integer integerField;
    private final Double doubleField;
    private final LocalDate localDateField;
    private final LocalDateTime localDateTimeField;
}
