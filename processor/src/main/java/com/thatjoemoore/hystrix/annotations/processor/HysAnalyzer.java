package com.thatjoemoore.hystrix.annotations.processor;

import com.google.auto.common.MoreElements;
import com.thatjoemoore.hystrix.annotations.*;
import com.thatjoemoore.utils.annotations.AbstractAnalyzer;
import com.thatjoemoore.utils.annotations.ElementsExt;
import com.thatjoemoore.utils.hystrix.annotations.*;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by adm.jmooreoa on 12/29/14.
 */
class HysAnalyzer extends AbstractAnalyzer<List<Blueprint>> {

    @Override
    public List<Blueprint> analyze(Set<? extends TypeElement> set) {
        return analyzeClasses(findElements());
    }

    private Set<? extends Element> findElements() {
        Set<Element> set = new HashSet<>();
        for (Element packOrClass : roundEnv().getElementsAnnotatedWith(HysCommands.class)) {
            if (packOrClass.getKind() != ElementKind.PACKAGE) {
                set.add(packOrClass);
            }
        }

        for (Element method : roundEnv().getElementsAnnotatedWith(HysCommand.class)) {
            set.add(method.getEnclosingElement());
        }
        return set;
    }

    private List<Blueprint> analyzeClasses(Set<? extends Element> elements) {
        List<Blueprint> blueprints = new ArrayList<>();

        for (Element each : elements) {
            if (!(each instanceof TypeElement)) {
                continue;
            }
            TypeElement te = (TypeElement) each;
            Blueprint bp = analyzeClass(te);
            if (bp != null) {
                blueprints.add(bp);
            }
        }

        return blueprints;
    }

    private Blueprint analyzeClass(TypeElement elem) {
        TopConfig top = getPackageConfig(elem);

        HysCommands classLevel = elem.getAnnotation(HysCommands.class);

        Blueprint bp = new Blueprint();
        bp.baseElement = elem;
        String groupName = alts(classLevel.group(), top.group, getDefaultGroup(elem));
//        bp.threa = alts(classLevel.threadPool(), top.threadPool);
        bp.packageName = alts(classLevel.generatedPackage(), top.generatedPackage, getDefaultPackage(elem));
        bp.className = alts(classLevel.wrapperClass(), getDefaultWrapperClass(elem));
        bp.doWriteWrapper = alts(classLevel.generateWrapper(), top.generateWrapper, true);

        bp.isTargetInterface = elem.getKind() == ElementKind.INTERFACE;
        bp.originatingElements = new Element[]{
                elem
        };

        String prefix = alts(classLevel.commandPrefix(), elem.getSimpleName().toString());

        HysInclusion classInclusion = elem.getAnnotation(HysInclusion.class);
        HysInclusion.Inclusion incl_class = null;
        String inclPat_class = null;
        if (classInclusion != null) {
            incl_class = classInclusion.inclusion();
            inclPat_class = classInclusion.inclusionPattern();
            validateInclusion(elem, classInclusion);
        }

        HysInclusion.Inclusion inclusion = alts(incl_class, top.inclusion, HysInclusion.Inclusion.ALL);
        String inclPat = alts(inclPat_class, top.inclusionPattern);

        bp.methods = scanMethods(elem, new CmdConfig(groupName, null, prefix, inclusion, inclPat));

        return bp;
    }

    private Blueprint scanMethod(Element element, Set<TypeElement> usedClasses) {
//        Element parent = element.getEnclosingElement();
//        //noinspection SuspiciousMethodCalls
//        if (usedClasses.contains(parent)) {
//            //We already covered this in the class-level scan
//            return null;
//        }
        return null;
    }

    private List<Blueprint.TargetMethod> scanMethods(
            TypeElement elem, CmdConfig cfg) {
        List<Blueprint.TargetMethod> list = new ArrayList<>();
        for (Element each : elem.getEnclosedElements()) {
            Blueprint.TargetMethod tm = analyzeMethod(elem, each, cfg);

            if (tm != null) {
                list.add(tm);
            }
        }

        return list;
    }

    private final class CmdConfig {
        private final String group;
        private final String threadPool;
        private final String prefix;
        private final HysInclusion.Inclusion inclusion;
        private final String inclPattern;

        public CmdConfig(String group, String threadPool, String prefix, HysInclusion.Inclusion inclusion, String inclPattern) {
            this.group = group;
            this.threadPool = threadPool;
            this.prefix = prefix;
            this.inclusion = inclusion;
            this.inclPattern = inclPattern;
        }
    }

    private Blueprint.TargetMethod analyzeMethod(
            TypeElement elem,
            Element method,
            CmdConfig cfg) {
        if (!shouldInclude(method, cfg.inclusion, cfg.inclPattern)) {
            if (canInclude(method) && elem.getKind() == ElementKind.INTERFACE) {
                Blueprint.TargetMethod tm = new Blueprint.TargetMethod(MoreElements.asExecutable(method));
                tm.include = false;
                return tm;
            } else {
                return null;
            }
        }

        ExecutableElement ex = MoreElements.asExecutable(method);

        HysCommand cmd = ex.getAnnotation(HysCommand.class);
        String name = null;
        String group = null;
        String threadPool = null;
        if (cmd != null) {
            name = e2n(cmd.name());
            group = e2n(cmd.group());
            threadPool = e2n(cmd.group());
        }

        AnnotationMirror fallbackMirror = MoreElements.getAnnotationMirror(ex, WithFallback.class).orNull();

        Blueprint.TargetMethod tm = new Blueprint.TargetMethod(ex);
        tm.include = true;
        String prefix = cfg.prefix;
        String cmdName = tm.name.substring(0, 1).toUpperCase() + tm.name.substring(1) + "Command";
        tm.className = alts(name, prefix + cmdName);
        tm.group = alts(group, cfg.group);
        tm.originatingElements = new Element[]{elem, ex};
        AnnotationValue fallback = ElementsExt.annotationValue(fallbackMirror, "fallback", elements());
        AnnotationValue fallbackMethod = ElementsExt.annotationValue(fallbackMirror, "fallbackMethod", elements());
        if (fallback != null) {
            tm.fallback = (TypeMirror) fallback.getValue();
        }
        if (fallbackMethod != null) {
            tm.fallbackMethod = String.valueOf(fallbackMethod.getValue());
        }

        return tm;
    }

    private boolean canInclude(Element candidate) {
        boolean annotated = MoreElements.isAnnotationPresent(candidate, HysCommand.class);
        if (candidate.getKind() != ElementKind.METHOD) {
            validate(annotated, "@HysCommand can only be placed on a method", candidate, HysCommand.class);
            return false;
        }
        ExecutableElement ex = (ExecutableElement) candidate;
        if (!ex.getModifiers().contains(Modifier.PUBLIC)) {
            validate(annotated, "@HysCommand can only be placed on a public method", ex, HysCommand.class);
            return false;
        }
        if (ex.getModifiers().contains(Modifier.STATIC)) {
            validate(annotated, "@HysCommand cannot be placed on a static method", candidate, HysCommand.class);
            return false;
        }
        return true;
    }

    private boolean shouldInclude(Element candidate, HysInclusion.Inclusion inclusion, String inclPattern) {
        if (!canInclude(candidate)) {
            return false;
        }
        HysCommand cmd = candidate.getAnnotation(HysCommand.class);
        ExecutableElement ex = (ExecutableElement) candidate;
        switch (inclusion) {
            case EXPLICIT:
                return cmd != null;
            case PATTERN:
                return ex.getSimpleName().toString().matches(inclPattern);
            case ALL:
                return true;
            default:
                throw new IllegalArgumentException("Invalid inclusion value: " + inclPattern);
        }
    }

    private String getDefaultGroup(TypeElement elem) {
        List<PackageElement> packs = ElementsExt.getPackages(elem, elements());
        StringBuilder sb = new StringBuilder();
        for (PackageElement pack : packs) {
            sb.append(Character.toLowerCase(pack.getSimpleName().charAt(0)));
        }
        return sb.reverse().append('.').append(elem.getSimpleName())
                .toString();
    }

    private String getDefaultPackage(TypeElement elem) {
        return MoreElements.getPackage(elem).getQualifiedName().toString();
    }

    private String getDefaultWrapperClass(TypeElement elem) {
        return elem.getSimpleName() + "HystrixWrapper";
    }

    @SafeVarargs
    private final <T> T alts(T... alts) {
        for (T alt : alts) {
            if (alt instanceof String) {
                if (e2n((String) alt) != null) {
                    return alt;
                }
            } else if (alt != null) {
                return alt;
            }
        }
        return null;
    }

    private TopConfig getPackageConfig(TypeElement elem) {
        TopConfig cfg = new TopConfig();

        PackageElement defs = ElementsExt.getFirstAnnotatedPackage(elem, HysDefaults.class, elements());
        PackageElement inclusion = ElementsExt.getFirstAnnotatedPackage(elem, HysInclusion.class, elements());

        if (defs != null) {
            HysDefaults hd = defs.getAnnotation(HysDefaults.class);
            cfg.group = e2n(hd.group());
            cfg.threadPool = e2n(hd.threadPool());
            logger().warning("ThreadPool is not yet supported", defs, MoreElements.getAnnotationMirror(defs, HysDefaults.class).orNull());
            cfg.generatedPackage = e2n(hd.generatedPackage());
            cfg.generateWrapper = hd.generateWrapper();
        }

        if (inclusion != null) {
            HysInclusion inc = inclusion.getAnnotation(HysInclusion.class);
            cfg.inclusion = inc.inclusion();
            cfg.inclusionPattern = e2n(inc.inclusionPattern());
            validateInclusion(inclusion, inc);
        }

        return cfg;
    }

    private void validateInclusion(Element element, HysInclusion inclusion) {
        validate(inclusion.inclusion() == HysInclusion.Inclusion.PATTERN && e2n(inclusion.inclusionPattern()) != null,
                "If inclusion is PATTERN, inclusionPattern must be specified", element, inclusion);
        if (inclusion.inclusion() != HysInclusion.Inclusion.PATTERN && e2n(inclusion.inclusionPattern()) != null) {
            logger().mandatory("If inclusion is not PATTERN, inclusionPattern will be ignored", element, inclusion);
        }
    }

    /**
     * empty string to null
     *
     * @param maybeEmpty
     * @return
     */
    private static String e2n(String maybeEmpty) {
        if (maybeEmpty == null || maybeEmpty.trim().isEmpty()) {
            return null;
        }
        return maybeEmpty;
    }

    private void validate(boolean condition, String message, Element element, Annotation annotation) {
        if (!condition) {
            logger().fatal(message, element, annotation);
        }
    }

    private void validate(boolean condition, String message, Element element, Class<? extends Annotation> annotation) {
        if (!condition) {
            logger().fatal(message, element, annotation);
        }
    }

    private static final class TopConfig {
        String group;
        String threadPool;
        String generatedPackage;
        boolean generateWrapper;
        String inclusionPattern;
        HysInclusion.Inclusion inclusion;
    }

    private static final class Result {
        private final List<Blueprint> blueprints;
        private final Set<TypeElement> elements;

        public Result(List<Blueprint> blueprints, Set<TypeElement> elements) {
            this.blueprints = blueprints;
            this.elements = elements;
        }
    }

}