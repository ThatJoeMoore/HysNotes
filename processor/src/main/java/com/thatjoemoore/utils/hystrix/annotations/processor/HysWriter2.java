package com.thatjoemoore.utils.hystrix.annotations.processor;

import com.google.auto.common.MoreTypes;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.squareup.javapoet.*;
import com.thatjoemoore.utils.annotations.AbstractWriter;
import com.thatjoemoore.utils.annotations.TypesExt;

import javax.annotation.Generated;
import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.*;

import static javax.lang.model.element.Modifier.*;

/**
 * Created by adm.jmooreoa on 12/29/14.
 */
class HysWriter2 extends AbstractWriter<List<Blueprint>> {

    private final DateFormat generatedDateFormat;

    HysWriter2(@Nullable DateFormat generatedDateFormat) {
        this.generatedDateFormat = generatedDateFormat == null ? DateFormat.getDateTimeInstance() : generatedDateFormat;
    }

    @Override
    public boolean write(List<Blueprint> blueprints) {
        for (Blueprint blueprint : blueprints) {
            try {
                write(blueprint);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to write blueprint for " + blueprint.baseElement, e);
            }
        }

        return true;
    }

    private void write(Blueprint blueprint) throws IOException {
        Map<Blueprint.TargetMethod, ClassName> commands = writeCommands(blueprint);
        writeInterface(blueprint, commands);
    }

    private Map<Blueprint.TargetMethod, ClassName> writeCommands(Blueprint blueprint) throws IOException {
        final Map<Blueprint.TargetMethod, ClassName> map = new HashMap<>();
        for (Blueprint.TargetMethod method : blueprint.methods) {
            if (!method.include) {
                continue;
            }
            ClassName generated = writeCommand(blueprint, method);
            map.put(method, generated);
        }
        return map;
    }

    private String toBareClassName(TypeMirror mirror) {
        if (mirror instanceof PrimitiveType) {
            return mirror.toString();
        }
        return MoreTypes.asTypeElement(types(), mirror).getQualifiedName().toString();
    }

    private ClassName writeCommand(Blueprint blueprint, Blueprint.TargetMethod method) throws IOException {
        ClassName hysCommand = ClassName.get(HystrixCommand.class);
        TypeName output = ClassName.get(TypesExt.notPrimitive(types(), method.returnType));

        TypeName commandType = ParameterizedTypeName.get(hysCommand, output);

        TypeSpec.Builder spec = TypeSpec.classBuilder(method.className)
                .superclass(commandType)
                .addAnnotation(getGenerated())
                .addOriginatingElement(blueprint.baseElement)
                .addModifiers(PUBLIC);

        TypeName target = ClassName.get(blueprint.baseElement.asType());

        FieldSpec delegate = FieldSpec.builder(target, "___delegate___", Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("The object that actually implements the logic")
                .build();

        spec.addField(delegate);

        //TODO: Add fallback logic
//        FieldSpec fallback = null;
//        if (method.fallback != null) {
//            TypeName fallbackName =
//            fallback = FieldSpec.builder()
//        }
        Args args = analyzeArgs(method.params);
        args.addFieldsTo(spec);

        ClassName factory = ClassName.get(HystrixCommandGroupKey.Factory.class);

        ParameterSpec delegateParam = ParameterSpec.builder(target, "delegate", FINAL)
                .build();

        MethodSpec ctorMain = args.addSetterLines(args.addParamsTo(MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)
                .addStatement("super($T.asKey($S))", factory, method.group)
                .addStatement("this.$N = $N", delegate, delegateParam)
                .addParameter(delegateParam)))
                .build();

        spec.addMethod(ctorMain);

        //TODO: Add fallback constructor
        //TODO: Add arguments object constructor

        MethodSpec impl = MethodSpec.methodBuilder("run")
                .returns(output)
                .addModifiers(PROTECTED)
                .addException(ClassName.get("java.lang", "Exception"))
                .addStatement("return $N.$L($L)", delegate, method.name, args.getInvocationString(false, true))
                .addAnnotation(Override.class)
                .build();

        //TODO: Add fallback method

        spec.addMethod(impl);

        JavaFile jf = JavaFile.builder(blueprint.packageName, spec.build())
                .skipJavaLangImports(true).build();

        System.out.println("===============> " + jf.typeSpec.name);

        jf.writeTo(System.out);
        System.out.println();

        JavaFileObject file = filer().createSourceFile(getFileName(blueprint, method), method.originatingElements);

        try (Writer w = file.openWriter()) {
            jf.writeTo(w);
        }
        return ClassName.get(jf.packageName, jf.typeSpec.name);
    }

    private static Args analyzeArgs(List<Blueprint.Param> args) {
        List<Args.Arg> l = new ArrayList<>();

        for (Blueprint.Param param : args) {
            TypeName type = TypeName.get(param.type);
            final String name = "arg_" + param.name;
            Args.Arg a = new Args.Arg(type,
                    FieldSpec.builder(type, name, PRIVATE, FINAL)
                            .build(),
                    ParameterSpec.builder(type, name, FINAL).build()
            );
            l.add(a);
        }
        return new Args(l);
    }

    private static final class Args {
        private final List<Arg> args;

        public Args(List<Arg> args) {
            this.args = args;
        }

        public TypeSpec.Builder addFieldsTo(TypeSpec.Builder builder) {
            for (Arg arg : args) {
                builder.addField(arg.field);
            }
            return builder;
        }

        public MethodSpec.Builder addParamsTo(MethodSpec.Builder builder) {
            for (Arg arg : args) {
                builder.addParameter(arg.param);
            }
            return builder;
        }

        public MethodSpec.Builder addSetterLines(MethodSpec.Builder builder) {
            for (Arg arg : args) {
                builder.addStatement("this.$N = $N", arg.field, arg.param);
            }
            return builder;
        }

        public String getInvocationString(boolean openWithComma, boolean includeThis) {
            StringBuilder sb = new StringBuilder();
            for (Arg arg : args) {
                if (openWithComma || sb.length() > 0) {
                    sb.append(", ");
                }
                if (includeThis) {
                    sb.append("this.");
                }
                sb.append(arg.field.name);
            }
            return sb.toString();
        }

        private static final class Arg {
            private final TypeName type;
            private final FieldSpec field;
            private final ParameterSpec param;

            public Arg(TypeName type, FieldSpec field, ParameterSpec param) {
                this.type = type;
                this.field = field;
                this.param = param;
            }
        }
    }

    private AnnotationSpec getGenerated() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", "Generated by " + getClass().getCanonicalName())
                .addMember("date", "$S", generatedDateFormat.format(new Date()))
                .build();
    }

    private void writeInterface(Blueprint blueprint, Map<Blueprint.TargetMethod, ClassName> commands) throws IOException {
        if (!blueprint.doWriteWrapper) {
            return;
        }

        TypeName target = ClassName.get(blueprint.baseElement.asType());

        FieldSpec delegate = FieldSpec.builder(target, "___delegate___", Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("The object that actually implements the logic")
                .build();

        TypeSpec.Builder spec = TypeSpec.classBuilder(blueprint.className)
                .addSuperinterface(target)
                .addAnnotation(getGenerated())
                .addOriginatingElement(blueprint.baseElement)
                .addModifiers(PUBLIC);

        spec.addField(delegate);

        ParameterSpec delegateParam = ParameterSpec.builder(target, "delegate", FINAL).build();

        spec.addMethod(MethodSpec.constructorBuilder()
                        .addParameter(delegateParam)
                        .addStatement("this.$N = $N", delegate, delegateParam)
                        .build()
        );

        for (Blueprint.TargetMethod method : blueprint.methods) {
            ClassName command = commands.get(method);
            MethodSpec methodSpec = writeIfaceMethod(method, blueprint, delegate, command);
            if (methodSpec != null) {
                spec.addMethod(methodSpec);
            }
        }

        JavaFile jf = JavaFile.builder(blueprint.packageName, spec.build())
                .skipJavaLangImports(true).build();

        JavaFileObject file = filer().createSourceFile(getFileName(blueprint), blueprint.originatingElements);

        try (Writer w = file.openWriter()) {
            jf.writeTo(w);
        }
    }

    private MethodSpec writeIfaceMethod(Blueprint.TargetMethod method, Blueprint blueprint, FieldSpec delegateField, ClassName command) throws IOException {
        if (command == null && !blueprint.isTargetInterface) {
            return null;
        }
        Args args = analyzeArgs(method.params);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.name)
                .addAnnotation(Override.class)
                .returns(TypeName.get(method.returnType))
                .addModifiers(PUBLIC);

        args.addParamsTo(builder);

        CodeBlock body;
        if (command != null) {
            body = CodeBlock.builder()
                    .addStatement("return new $T(this.$N$L).execute()",
                            command, delegateField, args.getInvocationString(true, false))
                    .build();
        } else {
            body = CodeBlock.builder()
                    .addStatement("this.$N.$L($L)", delegateField, args.getInvocationString(false, false))
                    .build();
        }

        builder.addCode(body);

        return builder.build();
    }

    private CharSequence getFileName(Blueprint blueprint, Blueprint.TargetMethod method) {
        return blueprint.packageName + '.' + method.className;
    }

    private static String getFileName(Blueprint blueprint) {
        return blueprint.packageName + '.' + blueprint.className;
    }

}
