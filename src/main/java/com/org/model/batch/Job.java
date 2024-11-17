package com.org.model.batch;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Job implements IBatch {
    private String name;
    private List<Step> steps = new ArrayList<>();
    public void addStep(Step step){
        steps.add(step);
    }

    public String withClassName(){
        return StringUtils.capitalize(name)+"Config";
    }

    public String withMethodParams(){
        return steps.stream()
                .map(step -> "Step " + step.getName())
                .collect(Collectors.joining(", "));
    }

}