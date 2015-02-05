package tests.example.basic;

import com.google.testing.compile.JavaFileObjects;
import com.thatjoemoore.hystrix.annotations.processor.HystrixProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Created by jmooreoa on 1/30/15.
 */
@RunWith(JUnit4.class)
public class BasicInterfaceTest {

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

    @Test
    public void simpleInterface() {
        JavaFileObject fileObject = JavaFileObjects.forResource("tests/example/basic/RemoteService.java");
        assert_().about(javaSource())
                .that(fileObject)
                .processedWith(new HystrixProcessor(fakeDateFormat))
                .compilesWithoutError()
        .and().generatesSources(
                JavaFileObjects.forResource("tests/example/basic/RemoteServiceDoSomethingCommand.java")
                , JavaFileObjects.forResource("tests/example/basic/RemoteServiceSomethingElseCommand.java")
                , JavaFileObjects.forResource("tests/example/basic/RemoteServiceHystrixWrapper.java")
        );
    }

}
