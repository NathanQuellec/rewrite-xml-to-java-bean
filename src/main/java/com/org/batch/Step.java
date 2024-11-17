package com.org.batch;

import com.org.enums.BatchType;
import com.org.model.Bean;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openrewrite.jgit.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class Step implements IBatch {
    private String name;

    private BatchType batchType;
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

    private String getBeanImport(AbstractMap.SimpleEntry<String,Bean> obj) {
        return Optional.ofNullable(obj)
                .map(o -> "import " + o.getValue().getBeanClassPath() + ";")
                .orElse(null);
    }

    public String withStepImports(){
        return Stream.of(reader, processor, writer)
                .filter(Objects::nonNull)
                .map(this::getBeanImport)
                .collect(Collectors.joining("\n"));
    }

    private String chunkLine(String batchProcessType, AbstractMap.SimpleEntry<String, Bean> batchProcess){
        return "." + batchProcessType + "(" +
                StringUtils.uncapitalize(batchProcess.getValue().getBeanClassName()) +
                ")\n";
    }

    private Map<AbstractMap.SimpleEntry<String,Bean>, String> buildBeanLabels(){
        return new HashMap<AbstractMap.SimpleEntry<String,Bean>, String>() {{
            put(reader, "reader");
            put(processor, "processor");
            put(writer, "writer");
        }};
    }

    private String chunkStepLines(){
        Map<AbstractMap.SimpleEntry<String,Bean>, String> beanLabels = buildBeanLabels();
        StringBuilder factoryLines = new StringBuilder();

        factoryLines.append("                ")
                .append(".<Person, Person>chunk(")
                .append(commitInterval)
                .append(")\n");
        Stream.of(reader, processor, writer)
                .filter(Objects::nonNull)
                .forEach(batchProcess -> factoryLines.append("                ")
                                .append(chunkLine(beanLabels.get(batchProcess), batchProcess))
                );

        return factoryLines.toString();
    }

    private String stepBuilderFactoryLines(){
        return batchType.equals(BatchType.CHUNK) ? chunkStepLines() : "";
    }

    private String methodParameters(){
        return Stream.of(reader, processor, writer)
                .filter(Objects::nonNull)
                .map(bean -> bean.getValue().getBeanClassName() + " " +
                        StringUtils.uncapitalize(bean.getValue().getBeanClassName()))
                .collect(Collectors.joining(", "));
    }

    public String withMethod(){
        return  "    @Bean\n" +
                "    public Step "+name+"("+methodParameters()+") {\n" +
                "        return stepBuilderFactory.get(\"personStep\")\n" +
                                stepBuilderFactoryLines() +
                "                .build();\n" +
                "    }\n";
    }
}
