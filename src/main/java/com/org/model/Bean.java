package com.org.model;

import lombok.Getter;
import lombok.Setter;
import org.openrewrite.jgit.annotations.Nullable;

@Getter
public class Bean{
    @Setter
    private String name;
    @Nullable
    private String beanClassPath;
    @Nullable
    private String beanClassName;

    private void setBeanClassName(String beanClassPath){
        beanClassName = beanClassPath.substring(beanClassPath.lastIndexOf('.') + 1);
    }
    public void setBeanClassPath(String beanClassPath){
        this.beanClassPath = beanClassPath;
        setBeanClassName(beanClassPath);
    }
}