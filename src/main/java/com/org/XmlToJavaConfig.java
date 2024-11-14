package com.org;

import com.org.model.Bean;
import com.org.model.Job;
import com.org.model.Step;
import lombok.EqualsAndHashCode;
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
    // bean pattern
    private static final XPathMatcher BEAN_MATCHER = new XPathMatcher("//beans/bean[@id]");

    // job pattern
    private static final XPathMatcher JOB_MATCHER_WITH_NAMESPACE = new XPathMatcher("//beans/batch:job");
    private static final XPathMatcher JOB_MATCHER = new XPathMatcher("//beans/job");

    //step pattern
    private static final XPathMatcher STEP_MATCHER_WITH_NAMESPACE = new XPathMatcher("//beans/batch:job/batch:step");
    private static final XPathMatcher STEP_MATCHER = new XPathMatcher("//beans/job/step");

    @Override
    public XmlIsoVisitor<ExecutionContext> getVisitor() {
        List<Job> jobs = new ArrayList<>();

        return new XmlIsoVisitor<ExecutionContext>(){

            @Override
            public Xml.Document visitDocument(Xml.Document document, ExecutionContext executionContext) {
                BatchJobsVisitor batchJobsVisitor = new BatchJobsVisitor();
                batchJobsVisitor.visitDocument(document, jobs);


                return super.visitDocument(document, executionContext);
            }
        };
    }

    public static class BatchJobsVisitor extends XmlIsoVisitor<List<Job>> {
        Job job = null;
        Step step = null;
        List<Bean> beans = new ArrayList<>();

        @Override
        public Xml.Document visitDocument(Xml.Document document, List<Job> jobs) {
            BeansVisitor beansVisitor = new BeansVisitor();
            beansVisitor.visitDocument(document, beans);
            Xml.Document doc = super.visitDocument(document, jobs);
            System.out.println(jobs.size());
            return doc;
        }

        @Override
        public Xml.Tag visitTag(Xml.Tag tag, List<Job> jobs) {
            if(JOB_MATCHER_WITH_NAMESPACE.matches(getCursor()) || JOB_MATCHER.matches(getCursor())) {
                job = new Job(); // new job each time we visit job marker
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
            if(attributeKey.equals("reader")) {
                System.out.println("FIND READER!!!! " + attributeValue);
                step.setReader(attributeValue);
            }
            if(attributeKey.equals("processor")) {
                System.out.println("FIND PROCESSOR!!!! " + attributeValue);
                step.setProcessor(attributeValue);
            }
            if(attributeKey.equals("writer")) {
                System.out.println("FIND WRITER!!!! " + attributeValue);
                step.setWriter(attributeValue);
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
                tag.getAttributes().stream().filter(atr -> atr.getKey().getName().equals("id"))
                        .findFirst()
                        .ifPresent(atr -> bean.setName(atr.getValue().getValue()));
                tag.getAttributes().stream().filter(atr -> atr.getKey().getName().equals("class"))
                        .findFirst()
                        .ifPresent(atr -> bean.setBeanClass(atr.getValue().getValue()));

                beans.add(bean);
                System.out.println("FIND BEANS!!!! " + bean.getName());
            }
            return super.visitTag(tag, beans);
        }
    }
}
