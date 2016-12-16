/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.parsing;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.TypeDeclarationStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParameterNameScanner extends GenericVisitorAdapter<Map<String, String>, String> {

    private Map<String, String> methodParameters = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> visit(CompilationUnit n, String arg) {
        StringBuilder packageName = new StringBuilder();

        n.getPackage()
            .map(PackageDeclaration::getPackageName)
            .ifPresent(packageName::append);

        return super.visit(n, packageName.toString());
    }

    @Override
    public Map<String, String> visit(TypeDeclarationStmt n, String arg) {
        if (!(n.getTypeDeclaration().isAnnotationPresent("Service") ||
            n.getTypeDeclaration().isAnnotationPresent("io.duna.core.service.Service"))) {
            return methodParameters;
        }

        String typeName = arg + "." + n.getTypeDeclaration().getName();
        return super.visit(n, typeName);
    }

    @Override
    public Map<String, String> visit(MethodDeclaration methodDeclaration, String arg) {
        String typeName = methodDeclaration.getAncestorOfType(TypeDeclaration.class).getNameAsString();

        String methodName = arg + "." + typeName +
            "(" +
            methodDeclaration
                .getDeclarationAsString(false, false, false) +
            ")";

        methodParameters.put(methodName,
            methodDeclaration
                .getParameters()
                .stream()
                .map(NodeWithSimpleName::getNameAsString)
                .reduce((a, b) -> a + "," + b)
                .orElse(""));

        return methodParameters;
    }
}
