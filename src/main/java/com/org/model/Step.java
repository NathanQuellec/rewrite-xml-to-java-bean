package com.org.model;

import lombok.Getter;
import lombok.Setter;
import org.openrewrite.jgit.annotations.Nullable;

@Getter
@Setter
public class Step{
    private String name;
    @Nullable
    private String reader;
    @Nullable
    private String processor;
    @Nullable
    private String writer;
    @Nullable
    private String commitInterval;
}
