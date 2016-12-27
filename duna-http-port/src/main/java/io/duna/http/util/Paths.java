/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.http.util;

import io.duna.http.HttpInterface;
import io.duna.http.HttpPath;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Paths {

    public static String getHttpInterfacePath(Class<?> contractClass, HttpInterface httpInterface) {
        if (!isExposed(contractClass)) {
            throw new IllegalArgumentException("The method provided must be an HttpInterface.");
        }

        String pathPrefix = contractClass.getAnnotation(HttpPath.class).value();
        String pathSuffix = httpInterface.path();

        if (!pathPrefix.startsWith("/")) pathPrefix = "/" + pathPrefix;

        if (pathPrefix.endsWith("/") && pathSuffix.startsWith("/")) {
            pathPrefix = pathPrefix.replaceFirst("/", "");
        } else if (!pathSuffix.startsWith("/")) {
            pathPrefix = "/" + pathPrefix;
        }

        return pathPrefix + pathSuffix;
    }

    public static List<String> getPathParameters(String path) {
        Matcher m = Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)").matcher(path);
        List<String> result = new ArrayList<>();

        while (m.find()) {
            result.add(m.group().substring(1));
        }

        return result;
    }

    public static boolean isExposed(Class<?> contract) {
        return contract.isAnnotationPresent(HttpPath.class);
    }
}
