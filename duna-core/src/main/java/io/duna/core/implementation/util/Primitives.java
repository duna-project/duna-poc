/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.implementation.util;

import net.bytebuddy.description.type.TypeDescription;

/**
 * Created by carlos on 01/12/16.
 */
public class Primitives {

    public static TypeDescription primitiveToWrapper(TypeDescription primitive) {
        switch (primitive.getCanonicalName()) {
            case "boolean":
                return new TypeDescription.ForLoadedType(Boolean.class);
            case "byte":
                return new TypeDescription.ForLoadedType(Byte.class);
            case "char":
                return new TypeDescription.ForLoadedType(Character.class);
            case "short":
                return new TypeDescription.ForLoadedType(Short.class);
            case "int":
                return new TypeDescription.ForLoadedType(Integer.class);
            case "float":
                return new TypeDescription.ForLoadedType(Float.class);
            case "long":
                return new TypeDescription.ForLoadedType(Long.class);
            case "double":
                return new TypeDescription.ForLoadedType(Double.class);
            default:
                return null;
        }
    }

    public static TypeDescription wrapperToPrimitive(TypeDescription wrapper) {
        switch (wrapper.getCanonicalName()) {
            case "java.lang.Boolean":
                return new TypeDescription.ForLoadedType(boolean.class);
            case "java.lang.Byte":
                return new TypeDescription.ForLoadedType(byte.class);
            case "java.lang.Character":
                return new TypeDescription.ForLoadedType(char.class);
            case "java.lang.Short":
                return new TypeDescription.ForLoadedType(short.class);
            case "java.lang.Integer":
                return new TypeDescription.ForLoadedType(int.class);
            case "java.lang.Float":
                return new TypeDescription.ForLoadedType(float.class);
            case "java.lang.Long":
                return new TypeDescription.ForLoadedType(long.class);
            case "java.lang.Double":
                return new TypeDescription.ForLoadedType(double.class);
            default:
                return null;
        }
    }
}
