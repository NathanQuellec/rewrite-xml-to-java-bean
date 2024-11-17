package com.org.model.batch;

import com.org.model.Bean;
import lombok.Getter;
import lombok.Setter;
import org.openrewrite.jgit.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

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

    private String withGenericImport(AbstractMap.SimpleEntry<String,Bean> obj) {
        return Optional.ofNullable(obj)
                .map(o -> "import " + o.getValue().getBeanClass() + ";")
                .orElse(null);
    }

    public String withReaderImport(){
        return withGenericImport(reader);
    }

    public String withProcessorImport(){
        return withGenericImport(processor);
    }

    public String withWriterImport(){
        return withGenericImport(writer);
    }
}
