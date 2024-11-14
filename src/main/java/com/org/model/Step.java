package com.org.model;

import lombok.Getter;
import lombok.Setter;
import org.openrewrite.jgit.annotations.Nullable;

import java.util.Map;

@Getter
@Setter
public class Step{
    private String name;
    @Nullable
    private Map<String,Bean> reader;
    @Nullable
    private Map<String,Bean> processor;
    @Nullable
    private Map<String,Bean> writer;
    @Nullable
    private String commitInterval;
}
