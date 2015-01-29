package com.thatjoemoore.utils.hystrix.annotations.processor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;

/**
 * Created by adm.jmooreoa on 12/29/14.
 */
public class StopProcessing extends RuntimeException {

    final String message;
    final Element element;
    final AnnotationMirror annotation;
    final AnnotationValue value;

    public StopProcessing(String message, Element element, AnnotationMirror annotation, AnnotationValue value) {
        super(message);
        this.message = message;
        this.element = element;
        this.annotation = annotation;
        this.value = value;
    }
}
