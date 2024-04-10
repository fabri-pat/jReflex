package io.github.fabripat.jreflex.entity;


import io.github.fabripat.jreflex.annotations.ExcludeBeanTesting;
import lombok.Data;

@ExcludeBeanTesting
@Data
public class EntityExcluded {

    private Integer integerField;
    private Double doubleField;
}
