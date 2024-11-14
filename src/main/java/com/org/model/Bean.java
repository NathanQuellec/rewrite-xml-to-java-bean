package com.org.model;

import lombok.Getter;
import lombok.Setter;
import org.openrewrite.jgit.annotations.Nullable;

@Getter
@Setter
public class Bean{
    private String name;
    @Nullable
    private String beanClass;
}