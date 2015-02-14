package tests.example;

import com.google.testing.compile.JavaFileObjects;
import com.thatjoemoore.hystrix.annotations.processor.HystrixProcessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.*;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

@RunWith(JUnit4.class)
public class ExamplesTest {

    private final DateFormat fakeDateFormat = new DateFormat() {
        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
            toAppendTo.append("{{CURRENT DATE HERE}}");
            return toAppendTo;
        }

        @Override
        public Date parse(String source, ParsePosition pos) {
            return new Date(0);
        }
    };

    private HystrixProcessor processor;

    @Before
    public void setUp() {
        processor = new HystrixProcessor(fakeDateFormat);
    }

    @Test
    public void simpleInterface() {
        run("tests/example/basic/", "RemoteService.java",
                "RemoteServiceDoSomethingCommand.java", "RemoteServiceSomethingElseCommand.java", "RemoteServiceHystrixFacade.java");
    }

    @Test
    public void noInterface_classLevel() {
        run("tests/example/nointerface/", "ClassLevel.java",
                "ClassLevelSayHiCommand.java", "ClassLevelHystrixFacade.java");
    }

    @Test
    public void noInterface_methodLevel() {
        run("tests/example/nointerface/", "MethodLevel.java",
                "MethodLevelRiskyCallCommand.java",
                "MethodLevelAGambleCommand.java");
    }


    private void run(String prefix, String source, String... expected) {
        JavaFileObject fileObject = JavaFileObjects.forResource(prefix + source);
        List<JavaFileObject> expectedFiles = new ArrayList<>();
        for (String each : expected) {
            expectedFiles.add(JavaFileObjects.forResource(prefix + each));
        }

        SplitList split = split(expectedFiles);
        assert_().about(javaSource())
                .that(fileObject)
                .processedWith(processor)
                .compilesWithoutError()
        .and().generatesSources(split.first, split.rest)
        ;
    }

    private static SplitList split(List<JavaFileObject> list) {
        assertThat(list).isNotEmpty();
        JavaFileObject first = list.get(0);
        List<JavaFileObject> rest;
        if (list.size() > 1) {
            rest = list.subList(1, list.size());
        } else {
            rest = Collections.emptyList();
        }
        return new SplitList(first, rest.toArray(new JavaFileObject[rest.size()]));
    }

    private static final class SplitList {
        private final JavaFileObject first;
        private final JavaFileObject[] rest;

        public SplitList(JavaFileObject first, JavaFileObject[] rest) {
            this.first = first;
            this.rest = rest;
        }
    }



}
