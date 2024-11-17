package com.org;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.xml.XmlParser;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.xml.Assertions.xml;

public class XmlToJavaConfigTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new XmlToJavaConfig("test.xml"))
          .parser(XmlParser.builder())
          .cycles(1)
          .expectedCyclesThatMakeChanges(1);
    }

    @Test
    void createJavaConfigFromXml() {
        rewriteRun(
          xml(
            """
                  <!-- src/main/resources/batch-job.xml -->
                              <beans xmlns="http://www.springframework.org/schema/beans"
                                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                     xmlns:batch="http://www.springframework.org/schema/batch"
                                     xsi:schemaLocation="http://www.springframework.org/schema/beans
                                         http://www.springframework.org/schema/beans/spring-beans.xsd
                                         http://www.springframework.org/schema/batch
                                         http://www.springframework.org/schema/batch/spring-batch.xsd">
              
                                  <!-- Job Repository & Transaction Manager -->
                                  <bean id="jobRepository" class="org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean"/>
                                  <bean id="transactionManager" class="org.springframework.batch.support.transaction.ResourcelessTransactionManager"/>
              
                                  <!-- Job Configuration -->
                                  <bean id="personProcessor" class="org.example.PersonItemProcessor"/>
                                  <bean id="jobListener" class="org.example.JobCompletionNotificationListener"/>
              
                                  <bean id="jobLauncher" class="org.springframework.batch.core.launch.support.SimpleJobLauncher">
                                      <property name="jobRepository" ref="jobRepository" />
                                  </bean>
              
                                  <batch:job id="personJob">
                                      <batch:step id="personStep">
                                          <batch:tasklet>
                                              <batch:chunk reader="personReader" processor="personProcessor" writer="personWriter" commit-interval="1"/>
                                          </batch:tasklet>
                                      </batch:step>
                                  </batch:job>
           
        
              
                                  <!-- Reader and Writer Beans -->
                                  <bean id="personReader" class="org.springframework.batch.item.file.FlatFileItemReader">
                                      <property name="resource" value="classpath:input.csv"/>
                                      <property name="lineMapper">
                                          <bean class="org.springframework.batch.item.file.mapping.DefaultLineMapper">
                                              <property name="lineTokenizer">
                                                  <bean class="org.springframework.batch.item.file.transform.DelimitedLineTokenizer">
                                                      <property name="names" value="firstName,lastName"/>
                                                  </bean>
                                              </property>
                                              <property name="fieldSetMapper">
                                                  <bean class="org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper">
                                                      <property name="targetType" value="org.example.Person"/>
                                                  </bean>
                                              </property>
                                          </bean>
                                      </property>
                                  </bean>
              
                                  <bean id="personWriter" class="org.example.PersonItemWriter"/>
                              </beans>
              """
          ),
          java(null,
            """
              package org.example.config;
              
              import org.springframework.batch.item.file.FlatFileItemReader;
              import org.example.PersonItemProcessor;
              import org.example.PersonItemWriter;
              import org.example.Person;
              import org.springframework.batch.core.Job;
              import org.springframework.batch.core.Step;
              import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
              import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
              import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
              import org.springframework.context.annotation.Bean;
              import org.springframework.context.annotation.Configuration;
              
              @Configuration
              @EnableBatchProcessing
              public class PersonJobConfig {
           
                  private final JobBuilderFactory jobBuilderFactory;
                  private final StepBuilderFactory stepBuilderFactory;
              
                  public PersonJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
                      this.jobBuilderFactory = jobBuilderFactory;
                      this.stepBuilderFactory = stepBuilderFactory;
                  }
              
                  @Bean
                  public Step personStep(FlatFileItemReader flatFileItemReader, PersonItemProcessor personItemProcessor, PersonItemWriter personItemWriter) {
                      return stepBuilderFactory.get("personStep")
                              .<Person, Person>chunk(1)
                              .reader(flatFileItemReader)
                              .processor(personItemProcessor)
                              .writer(personItemWriter)
                              .build();
                  }
                  @Bean
                  public Job personJob(Step personStep) {
                      return jobBuilderFactory.get("personJob")
                              .start(personStep)
                              .build();
                  }
              }
              """, s -> s.path("PersonJobConfig.java"))
        );
    }
}
