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

    @Override
    public XmlIsoVisitor<ExecutionContext> getVisitor() {


        return new XmlIsoVisitor<ExecutionContext>(){

            @Override
            public Xml.Document visitDocument(Xml.Document document, ExecutionContext executionContext) {
                BatchJobsVisitor batchJobsVisitor = new BatchJobsVisitor();
                List<BatchJob> jobs = new ArrayList<>();
                batchJobsVisitor.visitDocument(document, jobs);
                return super.visitDocument(document, executionContext);
            }

        };
    }

    public static class BatchJobsVisitor extends XmlIsoVisitor<List<BatchJob>> {
        BatchJob job = null;
        Step step = null;

        @Override
        public Xml.Document visitDocument(Xml.Document document, List<BatchJob> jobs) {
            Xml.Document doc = super.visitDocument(document, jobs);
            System.out.println(jobs.size());
            return doc;
        }

        @Override
        public Xml.Tag visitTag(Xml.Tag tag, List<BatchJob> jobs) {
            if(JOB_MATCHER_WITH_NAMESPACE.matches(getCursor()) || JOB_MATCHER.matches(getCursor())) {
                job = new BatchJob(); // new job each time we visit job marker
                tag.getAttributes()
                        .stream()
                        .filter(atr -> atr.getKey().getName().equals("id"))
                        .findFirst()
                        .ifPresent(atr -> job.setName(atr.getValue().getValue()));

                System.out.println("FIND JOB!!!! " + job.getName());

            }

            if(STEP_MATCHER_WITH_NAMESPACE.matches(getCursor()) || STEP_MATCHER.matches(getCursor())) {
                step = new Step();
                tag.getAttributes()
                        .stream()
                        .filter(atr -> atr.getKey().getName().equals("id"))
                        .findFirst()
                        .ifPresent(atr -> step.setName(atr.getValue().getValue()));

                System.out.println("FIND STEP!!!! " + step.getName());
            }
            return super.visitTag(tag, jobs);
        }

        @Override
        public Xml.Tag.Closing visitTagClosing(Xml.Tag.Closing tagClosing, List<BatchJob> jobs) {
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
        public Xml.Attribute visitAttribute(Xml.Attribute attribute, List<BatchJob> jobs) {
            if(attribute.getKey().getName().equals("reader")) {
                System.out.println("FIND READER!!!! " + attribute.getValue().getValue());
                step.setReader(attribute.getValue().getValue());
            }
            if(attribute.getKey().getName().equals("processor")) {
                System.out.println("FIND PROCESSOR!!!! " + attribute.getValue().getValue());
                step.setProcessor(attribute.getValue().getValue());
            }
            if(attribute.getKey().getName().equals("writer")) {
                System.out.println("FIND WRITER!!!! " + attribute.getValue().getValue());
                step.setWriter(attribute.getValue().getValue());
            }
            if(attribute.getKey().getName().equals("commit-interval")) {
                System.out.println("FIND COMMIT INTERVAL!!!! " + attribute.getValue().getValue());
                step.setCommitInterval(attribute.getValue().getValue());
            }
            return super.visitAttribute(attribute, jobs);
        }
    }

    @Getter
    @Setter
    public static class BatchJob{
        private String name;
        private List<Step> steps = new ArrayList<>();

        public void addStep(Step step){
            steps.add(step);
        }
    }

    @Getter
    @Setter
    public static class Step{
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
}
