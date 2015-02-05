package com.thatjoemoore.hystrix.annotations.processor;

import com.google.auto.common.MoreTypes;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.squareup.javawriter.JavaWriter;
import com.thatjoemoore.utils.annotations.AbstractWriter;
import com.thatjoemoore.utils.annotations.TypesExt;

import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.*;

import static com.squareup.javawriter.StringLiteral.forValue;
import static javax.lang.model.element.Modifier.*;

/**
 * Created by adm.jmooreoa on 12/29/14.
 */
class HysWriter extends AbstractWriter<List<Blueprint>> {

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
        writeCommands(blueprint);
        writeInterface(blueprint);
    }

    private void writeCommands(Blueprint blueprint) throws IOException {
        for (Blueprint.TargetMethod method : blueprint.methods) {
            if (!method.include) {
                continue;
            }
            writeCommand(blueprint, method);
        }
    }

    private String toBareClassName(TypeMirror mirror) {
        if (mirror instanceof PrimitiveType) {
            return mirror.toString();
        }
        return MoreTypes.asTypeElement(types(), mirror).getQualifiedName().toString();
    }

    private void writeCommand(Blueprint blueprint, Blueprint.TargetMethod method) throws IOException {
        JavaFileObject file = filer().createSourceFile(getFileName(blueprint, method), method.originatingElements);
        try (JavaWriter writer = new JavaWriter(file.openWriter())) {
            writer.setCompressingTypes(true);
            writer.emitPackage(blueprint.packageName);
            writer.emitEmptyLine();

            Set<String> imports = new HashSet<>();
            imports.add(blueprint.baseElement.toString());

            for (TypeMirror mirror : method.collectImports()) {
                if (mirror instanceof PrimitiveType) {
                    continue;
                }
                imports.add(toBareClassName(mirror));
            }

            writer.emitImports(HystrixCommand.class, HystrixCommand.Setter.class, HystrixCommandGroupKey.class);

            writer.emitImports(imports);

            final String extendsClause = writer.compressType(HystrixCommand.class.getCanonicalName()) + "<" +
                    writer.compressType(TypesExt.notPrimitive(types(), method.returnType).toString()) + ">";
            final String target = writer.compressType(blueprint.baseElement.toString());

            writer.beginType(method.className, "class", EnumSet.of(Modifier.PUBLIC), extendsClause)
                    .emitEmptyLine()
                    .emitField(target, "___delegate___", EnumSet.of(PRIVATE, FINAL));

            if (method.fallback != null) {
                String fallback = writer.compressType(method.fallback.toString());
                writer.emitField(fallback, "___fallback___", EnumSet.of(PRIVATE, FINAL), "new " + fallback + "()");
            }

            List<String> params = new ArrayList<>(method.params.size() * 2);
            params.add(target);
            params.add("___delegate___");

            StringBuilder invocationArgs = new StringBuilder();
            for (Blueprint.Param param : method.params) {
                String type = writer.compressType(param.type.toString());
                writer.emitField(type, param.name, EnumSet.of(PRIVATE, FINAL));
                params.add(type);
                params.add(param.name);
                if (invocationArgs.length() > 0) {
                    invocationArgs.append(", ");
                }
                invocationArgs.append("this.").append(param.name);
            }

            writer.beginConstructor(EnumSet.of(PUBLIC), params, null)
                    .emitStatement("super(HystrixCommandGroupKey.Factory.asKey(" + forValue(method.group) + "))")
                    .emitStatement("this.___delegate___ = ___delegate___");

            for (Blueprint.Param param : method.params) {
                writer.emitStatement("this.%s = %<s", param.name);
            }

            writer.endConstructor()
                    .emitEmptyLine();

            writer.emitAnnotation(Override.class)
                    .beginMethod(writer.compressType(TypesExt.notPrimitive(types(), method.returnType).toString()), "run", EnumSet.of(PROTECTED), null, Arrays.asList("Exception"))
                    .emitStatement("return this.___delegate___.%s(%s)", method.name, invocationArgs)
                    .endMethod();

            //TODO: Fallback

            writer.endType();
        }
    }

    private void writeInterface(Blueprint blueprint) throws IOException {
        if (!blueprint.doWriteWrapper) {
            return;
        }
        JavaFileObject file = filer().createSourceFile(getFileName(blueprint), blueprint.originatingElements);
        try (JavaWriter writer = new JavaWriter(file.openWriter())) {
            writer.emitPackage(blueprint.packageName)
                    .emitEmptyLine();
            Set<String> imports = new HashSet<>();
            imports.add(blueprint.baseElement.toString());
            for (TypeMirror mirror : blueprint.collectImports()) {
                if (mirror instanceof PrimitiveType) {
                    continue;
                }
                imports.add(toBareClassName(mirror));
            }
            writer.emitImports(imports)
                    .emitEmptyLine();

            final String target = writer.compressType(blueprint.baseElement.toString());

            String[] ifaces = blueprint.isTargetInterface ? new String[]{target} : new String[0];
            writer.beginType(blueprint.className, "class", EnumSet.of(PUBLIC), null, ifaces);

            writer.emitEmptyLine()
                    .emitField(target, "___delegate___", EnumSet.of(PRIVATE, FINAL))
                    .emitEmptyLine();

            writer.beginConstructor(EnumSet.of(PUBLIC), target, "delegate")
                    .emitStatement("this.___delegate___ = delegate")
                    .endConstructor();

            for (Blueprint.TargetMethod method : blueprint.methods) {
                writeIfaceMethod(writer, method, blueprint);
            }

            writer.endType();
        }
    }

    private void writeIfaceMethod(JavaWriter writer, Blueprint.TargetMethod method, Blueprint blueprint) throws IOException {
        if (!method.include && !blueprint.isTargetInterface) {
            return;
        }
        final List<String> paramsList = new ArrayList<>(method.params.size() * 2);
        final StringBuilder invocation = new StringBuilder();

        for (Blueprint.Param param : method.params) {
            final String type = writer.compressType(param.type.toString());
            paramsList.add(type);
            paramsList.add(param.name);

            invocation.append(", ").append(param.name);
        }

        final List<String> exceptions = Lists.transform(method.exceptions, new ToCompressedType(writer));

        writer.emitEmptyLine().emitAnnotation(Override.class)
                .beginMethod(writer.compressType(method.returnType.toString()), method.name, EnumSet.of(PUBLIC, FINAL),
                        paramsList, exceptions);
        final String ret = method.returnType.getKind() != TypeKind.VOID ? "return " : "";

        if (method.include) {
            writer.emitStatement("%snew %s(___delegate___%s).execute()", ret, method.className, invocation);
        } else {
            writer.emitStatement("%s___delegate___.%s(%s)", ret, method.name, invocation.substring(2));
        }
        writer.endMethod().emitEmptyLine();
    }

    private static final class ToCompressedType implements Function<TypeMirror, String> {
        private final JavaWriter writer;

        private ToCompressedType(JavaWriter writer) {
            this.writer = writer;
        }

        @Nullable
        @Override
        public String apply(@Nullable TypeMirror input) {
            if (input == null) return null;
            return writer.compressType(input.toString());
        }
    }

    private CharSequence getFileName(Blueprint blueprint, Blueprint.TargetMethod method) {
        return blueprint.packageName + '.' + method.className;
    }

    private static String getFileName(Blueprint blueprint) {
        return blueprint.packageName + '.' + blueprint.className;
    }

}
