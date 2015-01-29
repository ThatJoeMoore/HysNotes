package com.thatjoemoore.utils.hystrix.annotations.processor;

import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.thatjoemoore.utils.annotations.AnalyzeAndWriteProcessor;
import com.thatjoemoore.utils.hystrix.annotations.HysCommand;
import com.thatjoemoore.utils.hystrix.annotations.HysCommands;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

/**
 * Created by adm.jmooreoa on 12/29/14.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class HystrixProcessor extends AnalyzeAndWriteProcessor<List<Blueprint>> {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(
                HysCommand.class.getCanonicalName(),
                HysCommands.class.getCanonicalName()
        );
    }

    private TypeElement getTypeElement(Element each) {
        if (each instanceof TypeElement) {
            return (TypeElement) each;
        } else if (each instanceof ExecutableElement) {
            return getTypeElement(each.getEnclosingElement());
        }
        AnnotationMirror annotationMirror = MoreElements.getAnnotationMirror(each, HysCommands.class)
                .or(MoreElements.getAnnotationMirror(each, HysCommand.class))
                .orNull();
        throw new StopProcessing("Unexpected annotated element", each, annotationMirror, null);
    }

    @Override
    protected Analyzer<List<Blueprint>> getAnalyzer() {
        return new HysAnalyzer();
    }

    @Override
    protected Writer<List<Blueprint>> getWriter() {
        return new HysWriter();
    }

}
