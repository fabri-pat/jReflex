package io.github.fabripat.jreflex.entity;


import io.github.fabripat.jreflex.annotations.ExcludeFieldBeanTesting;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
public class EntityWithFieldExcluded {

    @ExcludeFieldBeanTesting
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Integer integerField;
    private Double doubleField;
}