package com.org;

import com.org.enums.BatchType;
import com.org.model.Bean;
import com.org.batch.IBatch;
import com.org.batch.Job;
import com.org.batch.Step;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

import java.nio.file.Paths;
import java.util.*;

@Value
@EqualsAndHashCode(callSuper = false)
public class XmlToJavaConfig extends ScanningRecipe<XmlToJavaConfig.Scanned> {

    @Option(displayName = "Relative file path",
            description = "File path of new file.",
            example = "foo/bar/baz.xml")
    String xmlFileName;

    @Override
    public String getDisplayName() {
        return "Transform XML config into Java config";
    }

    @Override
    public String getDescription() {
        return "Create new Java Config files from existing XML configs.";
    }

    // bean pattern
    private static final XPathMatcher BEAN_MATCHER = new XPathMatcher("//beans/bean[@id]");

    // job pattern
    private static final XPathMatcher JOB_MATCHER_WITH_NAMESPACE = new XPathMatcher("//beans/batch:job");
    private static final XPathMatcher JOB_MATCHER = new XPathMatcher("//beans/job");

    //step pattern
    private static final XPathMatcher STEP_MATCHER_WITH_NAMESPACE = new XPathMatcher("//beans/batch:job/batch:step");
    private static final XPathMatcher STEP_MATCHER = new XPathMatcher("//beans/job/step");

    public static class Scanned {
        List<Job> jobs = new ArrayList<>();
    }

    @Override
    public Scanned getInitialValue(ExecutionContext ctx) {
        return new Scanned();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Scanned acc) {
        return new XmlIsoVisitor<ExecutionContext>() {
            @Override
            public Xml.Document visitDocument(Xml.Document document, ExecutionContext executionContext) {
                BatchJobsVisitor batchJobsVisitor = new BatchJobsVisitor();
                batchJobsVisitor.visitDocument(document, acc.jobs);

                return super.visitDocument(document, executionContext);
            }
        };
    }

    @Override
    public Collection<JavaSourceFile> generate(Scanned acc, ExecutionContext ctx) {
        List<JavaSourceFile> generated = new LinkedList<>();


        JavaParser parser = JavaParser
                .fromJavaVersion()
                .build();
        parser.parse(
                "package org.example.config;\n\n" +
                        acc.jobs.get(0).withJobImports() + "\n" +
                        "import org.springframework.batch.core.Job;\n" +
                        "import org.springframework.batch.core.Step;\n" +
                        "import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;\n" +
                        "import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;\n" +
                        "import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;\n" +
                        "import org.springframework.context.annotation.Bean;\n" +
                        "import org.springframework.context.annotation.Configuration;\n\n" +

                        "@Configuration\n" +
                        "@EnableBatchProcessing\n" +
                        "public class "+acc.jobs.get(0).withClassName()+" {\n\n" +

                        "    private final JobBuilderFactory jobBuilderFactory;\n" +
                        "    private final StepBuilderFactory stepBuilderFactory;\n\n" +

                        "    public "+acc.jobs.get(0).withClassName()+"(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {\n" +
                        "        this.jobBuilderFactory = jobBuilderFactory;\n" +
                        "        this.stepBuilderFactory = stepBuilderFactory;\n" +
                        "    }\n\n" +

                        acc.jobs.get(0).withStepMethods() +

                        "    @Bean\n" +
                        "    public Job "+acc.jobs.get(0).getName()+"("+acc.jobs.get(0).withMethodParams()+") {\n" +
                        "        return jobBuilderFactory.get(\""+acc.jobs.get(0).getName()+"\")\n" +
                        "                .start(personStep)\n" +
                        "                .build();\n" +
                        "    }\n" +
                        "}"
        )
                .map(brandNewFile -> (JavaSourceFile) brandNewFile.withSourcePath(Paths.get(acc.jobs.get(0).withClassName()+".java")))
                .forEach(generated::add);

        return generated;
    }

    public static class BatchJobsVisitor extends XmlIsoVisitor<List<Job>> {

        Job job = new Job();
        Step step = new Step();
        List<Bean> beans = new ArrayList<>();

        @Override
        public Xml.Document visitDocument(Xml.Document document, List<Job> jobs) {
            BeansVisitor beansVisitor = new BeansVisitor();
            beansVisitor.visitDocument(document, beans);
            Xml.Document doc = super.visitDocument(document, jobs);
            System.out.println(jobs.size());
            return doc;
        }

        private <T extends IBatch> void setBatchName(Xml.Tag tag, T obj){
            tag.getAttributes()
                    .stream()
                    .filter(atr -> atr.getKey().getName().equals("id"))
                    .findFirst()
                    .ifPresent(atr -> obj.setName(atr.getValue().getValue()));
        }

        @Override
        public Xml.Tag visitTag(Xml.Tag tag, List<Job> jobs) {
            if(JOB_MATCHER_WITH_NAMESPACE.matches(getCursor()) || JOB_MATCHER.matches(getCursor())) {
                job = new Job(); // new job each time we visit job marker
                setBatchName(tag, job);
                System.out.println("FIND JOB!!!! " + job.getName());
            }

            if(STEP_MATCHER_WITH_NAMESPACE.matches(getCursor()) || STEP_MATCHER.matches(getCursor())) {
                step = new Step();
                setBatchName(tag, step);
                System.out.println("FIND STEP!!!! " + step.getName());
            }
            return super.visitTag(tag, jobs);
        }

        @Override
        public Xml.Tag.Closing visitTagClosing(Xml.Tag.Closing tagClosing, List<Job> jobs) {
            if(JOB_MATCHER_WITH_NAMESPACE.matches(getCursor()) || JOB_MATCHER.matches(getCursor())) {
                jobs.add(job);
                System.out.println("-----END OF JOB-----");
            }

            if(STEP_MATCHER_WITH_NAMESPACE.matches(getCursor()) || STEP_MATCHER.matches(getCursor())) {
                job.addStep(step);
                System.out.println("-----END OF STEP-----");
            }
            return super.visitTagClosing(tagClosing, jobs);
        }

        @Override
        public Xml.Attribute visitAttribute(Xml.Attribute attribute, List<Job> jobs) {
            String attributeValue = attribute.getValue().getValue();
            String attributeKey = attribute.getKey().getName();

            if(attributeKey.matches("reader|processor|writer")) {
                System.out.println("FIND " + attributeValue + " !!!");
                AbstractMap.SimpleEntry<String, Bean> beanRef = new AbstractMap.SimpleEntry<>(attributeValue, new Bean());
                beans.stream()
                        .filter(b -> b.getName().equals(attributeValue))
                        .findFirst()
                        .ifPresent(beanRef::setValue);

                step.setBeanRef(attributeKey, beanRef);
                step.setBatchType(BatchType.CHUNK);
            }
            if(attributeKey.equals("commit-interval")) {
                System.out.println("FIND COMMIT INTERVAL!!!! " + attributeValue);
                step.setCommitInterval(attributeValue);
            }
            return super.visitAttribute(attribute, jobs);
        }
    }

    public static class BeansVisitor extends XmlIsoVisitor<List<Bean>> {

        @Override
        public Xml.Document visitDocument(Xml.Document document, List<Bean> beans) {
            Xml.Document doc = super.visitDocument(document, beans);
            System.out.println(beans.size());
            return doc;
        }
        @Override
        public Xml.Tag visitTag(Xml.Tag tag, List<Bean> beans) {
            if(BEAN_MATCHER.matches(getCursor())) {

                Bean bean = new Bean();
                tag.getAttributes().forEach(atr -> {
                    String attributeName = atr.getKey().getName();
                    String attributeValue = atr.getValue().getValue();
                    if ("id".equals(attributeName)) {
                        bean.setName(attributeValue);
                    } else if ("class".equals(attributeName)) {
                        bean.setBeanClassPath(attributeValue);
                    }
                });
                beans.add(bean);
                System.out.println("FIND BEAN!!!! " + bean.getName());
            }
            return super.visitTag(tag, beans);
        }
    }
}
