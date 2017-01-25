/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.instrument.asm;

import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;

public class JinqStreamCloserClassVisitor extends ClassVisitor {

    public JinqStreamCloserClassVisitor(int api) {
        super(api);
    }

    public JinqStreamCloserClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // First, map if the JinqStream is ever created

        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
