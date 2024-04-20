package io.github.fabripat.jreflex.testdomain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public record RecordDto(Integer integerField,
                        Double doubleField,
                        LocalDate localDateField,
                        LocalDateTime localDateTimeField) {
}
