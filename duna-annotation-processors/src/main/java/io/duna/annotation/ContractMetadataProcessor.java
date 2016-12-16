/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.annotation;

import io.duna.http.HttpPort;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.AbstractElementVisitor8;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SupportedAnnotationTypes("io.duna.http.HttpPort")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ContractMetadataProcessor extends AbstractProcessor {

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(HttpPort.class)) {
            ParameterMetadataVisitor visitor = new ParameterMetadataVisitor(processingEnv);
            element.accept(visitor, null);

            TypeElement type = (TypeElement) processingEnv.getTypeUtils().asElement(element.asType());

            Map<String, List<String>> methodParameters = visitor.getMethodParameters();

            try {
                FileObject output = processingEnv.getFiler().createResource(
                    StandardLocation.locationFor("META-INF/parameter-names"), null,
                        type.getQualifiedName().toString());

                DataOutputStream outputStream = new DataOutputStream(output.openOutputStream());

                methodParameters.forEach((k, v) -> {
                    try {
                        outputStream.writeUTF(k + "=" + v.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {

            }
        }

        return true;
    }

    class ParameterMetadataVisitor extends AbstractElementVisitor8<Void, Void> {

        private final ProcessingEnvironment processingEnv;

        private Map<String, List<String>> methodParameters = new ConcurrentHashMap<>();

        ParameterMetadataVisitor(ProcessingEnvironment processingEnv) {
            this.processingEnv = processingEnv;
        }

        Map<String, List<String>> getMethodParameters() {
            return methodParameters;
        }

        @Override
        public Void visitPackage(PackageElement e, Void aVoid) {
            return null;
        }

        @Override
        public Void visitType(TypeElement e, Void aVoid) {
            e.getEnclosedElements()
                .stream()
                .filter(el -> el.getKind() == ElementKind.METHOD)
                .forEach(el -> this.visitExecutable((ExecutableElement) el, null));

            return null;
        }

        @Override
        public Void visitVariable(VariableElement e, Void aVoid) {
            return null;
        }

        @Override
        public Void visitExecutable(ExecutableElement e, Void aVoid) {
            TypeElement type = (TypeElement) e.getEnclosingElement();

            StringBuilder signature = new StringBuilder();
            signature.append(type.getQualifiedName().toString())
                .append(".")
                .append(e.getSimpleName())
                .append("(");

            e.getParameters()
                .stream()
                .map(p -> (TypeElement) processingEnv.getTypeUtils().asElement(p.asType()))
                .map(t -> t.getQualifiedName().toString())
                .reduce((a, b) -> a + "," + b)
                .ifPresent(signature::append);

            signature.append(")");

            List<String> parameterNames = new ArrayList<>(e.getParameters().size());

            e.getParameters()
                .stream()
                .map(VariableElement::getSimpleName)
                .map(Object::toString)
                .forEach(parameterNames::add);

            methodParameters.put(signature.toString(), parameterNames);
            return null;
        }

        @Override
        public Void visitTypeParameter(TypeParameterElement e, Void aVoid) {
            return null;
        }
    }

}
