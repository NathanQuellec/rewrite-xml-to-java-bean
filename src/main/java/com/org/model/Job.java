package com.org.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Job{
    private String name;
    private List<Step> steps = new ArrayList<>();
    private List<Bean> beans = new ArrayList<>();

    public void addStep(Step step){
        steps.add(step);
    }
}