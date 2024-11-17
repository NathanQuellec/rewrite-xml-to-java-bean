package com.org.model.batch;

import com.org.model.Bean;
import lombok.Getter;
import lombok.Setter;
import org.openrewrite.jgit.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Map;

@Getter
@Setter
public class Step implements IBatch {
    private String name;
    @Nullable
    private AbstractMap.SimpleEntry<String, Bean> reader;
    @Nullable
    private AbstractMap.SimpleEntry<String,Bean> processor;
    @Nullable
    private AbstractMap.SimpleEntry<String,Bean> writer;
    @Nullable
    private String commitInterval;

    public void setBeanRef(String key, AbstractMap.SimpleEntry<String, Bean> beanRef) {
        switch(key){
            case "reader":
                reader = beanRef;
                break;
            case "processor":
                processor = beanRef;
                break;
            case "writer":
                writer = beanRef;
                break;
            default:
                break;
        }
    }

    public String withReaderImport(){
        return "import " + reader.getValue().getBeanClass() + ";";
    }
}
