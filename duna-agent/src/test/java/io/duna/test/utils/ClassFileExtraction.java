/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.test.utils;

import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.auxiliary.AuxiliaryType;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.pool.TypePool;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClassFileExtraction {

    private static final int CA = 0xCA, FE = 0xFE, BA = 0xBA, BE = 0xBE;

    public static Map<String, byte[]> of(Class<?>... type) throws IOException {
        Map<String, byte[]> result = new HashMap<>();
        for (Class<?> aType : type) {
            result.put(aType.getName(), extract(aType));
        }
        return result;
    }

    private static byte[] extract(Class<?> type, AsmVisitorWrapper asmVisitorWrapper) throws IOException {
        ClassReader classReader = new ClassReader(type.getName());
        ClassWriter classWriter = new ClassWriter(classReader, AsmVisitorWrapper.NO_FLAGS);
        classReader.accept(asmVisitorWrapper.wrap(new TypeDescription.ForLoadedType(type),
            classWriter,
            new IllegalContext(),
            TypePool.Empty.INSTANCE,
            AsmVisitorWrapper.NO_FLAGS,
            AsmVisitorWrapper.NO_FLAGS), AsmVisitorWrapper.NO_FLAGS);
        return classWriter.toByteArray();
    }

    private static byte[] extract(Class<?> type) throws IOException {
        return extract(type, new AsmVisitorWrapper.Compound());
    }

    @Test
    public void testClassFileExtraction() throws Exception {
        byte[] binaryFoo = extract(Foo.class);
        assertTrue(binaryFoo.length > 4);
        assertEquals(binaryFoo[0], new Integer(CA).byteValue());
        assertEquals(binaryFoo[1], new Integer(FE).byteValue());
        assertEquals(binaryFoo[2], new Integer(BA).byteValue());
        assertEquals(binaryFoo[3], new Integer(BE).byteValue());
    }

    private static class Foo {
        /* empty */
    }

    private static class IllegalContext implements Implementation.Context {

        @Override
        public TypeDescription register(AuxiliaryType auxiliaryType) {
            throw new AssertionError("Did not expect method call");
        }

        @Override
        public FieldDescription.InDefinedShape cache(StackManipulation fieldValue, TypeDescription fieldType) {
            throw new AssertionError("Did not expect method call");
        }

        @Override
        public TypeDescription getInstrumentedType() {
            throw new AssertionError("Did not expect method call");
        }

        @Override
        public ClassFileVersion getClassFileVersion() {
            throw new AssertionError("Did not expect method call");
        }
    }
}
