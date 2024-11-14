package com.org;

import org.openrewrite.jgit.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = false)
public class XmlToJavaConfig extends Recipe {

    @Override
    public String getDisplayName() {
        return "Transform XML config into Java config";
    }

    @Override
    public String getDescription() {
        return "Create new Java Config files from existing XML configs.";
    }

    // job pattern
    private static final XPathMatcher JOB_MATCHER_WITH_NAMESPACE = new XPathMatcher("//beans/batch:job");
    private static final XPathMatcher JOB_MATCHER = new XPathMatcher("//beans/job");

    //step pattern
    private static final XPathMatcher STEP_MATCHER_WITH_NAMESPACE = new XPathMatcher("//beans/batch:job/batch:step");
    private static final XPathMatcher STEP_MATCHER = new XPathMatcher("//beans/job/step");

    private static List<BatchJob> jobs = new ArrayList<>();

    @Override
    public XmlIsoVisitor<ExecutionContext> getVisitor() {


        BatchJob job = new BatchJob();

        return new XmlIsoVisitor<ExecutionContext>(){

            @Override
            public Xml.Document visitDocument(Xml.Document document, ExecutionContext executionContext) {
                Xml.Document doc = super.visitDocument(document, executionContext);
                System.out.println(jobs.size());
                return doc;
            }

            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                if(JOB_MATCHER_WITH_NAMESPACE.matches(getCursor()) || JOB_MATCHER.matches(getCursor())) {
                    String jobName = tag.getAttributes()
                            .stream()
                            .filter(atr -> atr.getKey().getName().equals("id"))
                            .findFirst()
                            .get()
                            .getValue()
                            .getValue();

                    System.out.println("FIND JOB!!!! " + jobName);
                    job.setName(jobName);

                }

                if(STEP_MATCHER_WITH_NAMESPACE.matches(getCursor()) || STEP_MATCHER.matches(getCursor())) {
                    String stepName = tag.getAttributes()
                            .stream()
                            .filter(atr -> atr.getKey().getName().equals("id"))
                            .findFirst()
                            .get()
                            .getValue()
                            .getValue();
                    System.out.println("FIND STEP!!!! " + stepName);
                    job.addStep(stepName);
                }
                return super.visitTag(tag, executionContext);
            }

            public Xml.Tag.Closing visitTagClosing(Xml.Tag.Closing tagClosing, ExecutionContext executionContext) {
                if(JOB_MATCHER_WITH_NAMESPACE.matches(getCursor()) || JOB_MATCHER.matches(getCursor())) {
                    jobs.add(job);
                    System.out.println("-----END OF JOB-----");
                }
                return super.visitTagClosing(tagClosing, executionContext);
            }

            @Override
            public Xml.Attribute visitAttribute(Xml.Attribute attribute, ExecutionContext executionContext) {
                if(attribute.getKey().getName().equals("reader")) {
                    System.out.println("FIND READER!!!! " + attribute.getValue().getValue());
                    job.setReader(attribute.getValue().getValue());
                }
                if(attribute.getKey().getName().equals("processor")) {
                    System.out.println("FIND PROCESSOR!!!! " + attribute.getValue().getValue());
                    job.setProcessor(attribute.getValue().getValue());
                }
                if(attribute.getKey().getName().equals("writer")) {
                    System.out.println("FIND WRITER!!!! " + attribute.getValue().getValue());
                    job.setWriter(attribute.getValue().getValue());
                }
                if(attribute.getKey().getName().equals("commit-interval")) {
                    System.out.println("FIND COMMIT INTERVAL!!!! " + attribute.getValue().getValue());
                }
                return super.visitAttribute(attribute, executionContext);
            }

        };
    }

    @Getter
    @Setter
    private static class BatchJob{
        private String name;
        private List<String> steps = new ArrayList<>();
        @Nullable
        private String reader;
        @Nullable
        private String processor;
        @Nullable
        private String writer;

        public void addStep(String step){
            steps.add(step);
        }
    }
}
