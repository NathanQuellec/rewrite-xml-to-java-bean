package com.org;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

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
                return super.visitDocument(document, executionContext);
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
                }
                return super.visitTag(tag, executionContext);
            }

            @Override
            public Xml.Attribute visitAttribute(Xml.Attribute attribute, ExecutionContext executionContext) {
                if(attribute.getKey().getName().equals("reader")) {
                    System.out.println("FIND READER!!!! " + attribute.getValue().getValue());
                }
                if(attribute.getKey().getName().equals("processor")) {
                    System.out.println("FIND PROCESSOR!!!! " + attribute.getValue().getValue());
                }
                if(attribute.getKey().getName().equals("writer")) {
                    System.out.println("FIND WRITER!!!! " + attribute.getValue().getValue());
                }
                if(attribute.getKey().getName().equals("commit-interval")) {
                    System.out.println("FIND COMMIT INTERVAL!!!! " + attribute.getValue().getValue());
                }
                return super.visitAttribute(attribute, executionContext);
            }
        };
    }
}
