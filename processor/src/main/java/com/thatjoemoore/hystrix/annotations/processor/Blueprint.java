package com.thatjoemoore.hystrix.annotations.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by adm.jmooreoa on 12/30/14.
 */
class Blueprint {

    public Element baseElement;
    public String packageName;
    public String className;
    public boolean doWriteWrapper;
    public boolean isTargetInterface;

    public List<TargetMethod> methods;
    public Element[] originatingElements;


    public static class TargetMethod {
        public boolean include;
        public String className;
        public String group;

        public final String name;
        public final TypeMirror returnType;
        public final List<Param> params;
        public final List<? extends TypeMirror> exceptions;

        public Element[] originatingElements;
        public TypeMirror fallback;
        public String fallbackMethod;

        public TargetMethod(ExecutableElement el) {
            this.name = el.getSimpleName().toString();
            this.returnType = el.getReturnType();
            this.exceptions = el.getThrownTypes();
            this.params = Param.create(el.getParameters());
        }

        public Set<TypeMirror> collectImports() {
            Set<TypeMirror> set = new HashSet<>();
            set.add(returnType);
            for (Param param : params) {
                set.add(param.type);
            }
            set.addAll(exceptions);
            if (fallback != null) {
                set.add(fallback);
            }
            return set;
        }
    }

    public static class Param {
        public TypeMirror type;
        public String name;

        public Param(TypeMirror type, String name) {
            this.type = type;
            this.name = name;
        }

        public static List<Param> create(List<? extends VariableElement> elems) {
            List<Param> list = new ArrayList<>();
            for (VariableElement elem : elems) {
                list.add(new Param(elem.asType(), elem.getSimpleName().toString()));
            }
            return list;
        }
    }

    public Set<TypeMirror> collectImports() {
        Set<TypeMirror> set = new HashSet<>();
        set.add(baseElement.asType());
        for (TargetMethod each : methods) {
            set.addAll(each.collectImports());
        }
        return set;
    }
}
