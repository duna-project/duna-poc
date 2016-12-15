/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.annotation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.AbstractElementVisitor8;
import javax.lang.model.util.TypeKindVisitor8;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class ParameterMetadataVisitor extends AbstractElementVisitor8<Void, String> {

    private Map<String, List<String>> methodParameters = new ConcurrentHashMap<>();

    private ProcessingEnvironment processingEnvironment;

    public ParameterMetadataVisitor(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    @Override
    public Void visitPackage(PackageElement e, String s) {
        return null;
    }

    @Override
    public Void visitType(TypeElement e, String s) {
        return null;
    }

    @Override
    public Void visitVariable(VariableElement e, String s) {
        return null;
    }

    @Override
    public Void visitExecutable(ExecutableElement e, String s) {
        TypeElement type = (TypeElement) e.getEnclosingElement();

        StringBuilder signature = new StringBuilder();
        signature.append(type.getQualifiedName().toString())
            .append(".")
            .append(e.getSimpleName())
            .append("(");

        e.getParameters()
            .stream()
            .map(p -> (TypeElement) processingEnvironment.getTypeUtils().asElement(p.asType()))
            .map(t -> t.getQualifiedName().toString().replace("java.lang.", ""))
            .reduce((a, b) -> a + "," + b)
            .ifPresent(signature::append);

        signature.append(")");

        List<String> parameterNames = new ArrayList<>(e.getParameters().size());

        e.getParameters()
            .stream()
            .map(VariableElement::getSimpleName)
            .map(Object::toString)
            .forEach(parameterNames::add);

        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.NOTE, signature.toString());
        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.NOTE, parameterNames.toString());
        methodParameters.put(signature.toString(), parameterNames);

        return null;
    }

    @Override
    public Void visitTypeParameter(TypeParameterElement e, String s) {
        return null;
    }
}
