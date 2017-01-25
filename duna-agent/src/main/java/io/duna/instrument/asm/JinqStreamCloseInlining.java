/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.instrument.asm;

import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;

import java.util.HashSet;
import java.util.Set;

public class JinqStreamCloseInlining extends MethodVisitor {

    private static final Set<String> jinqStreamImplementations = new HashSet<>();

    static {
        jinqStreamImplementations.add("Lorg.jinq.orm.stream.Stream;");
        jinqStreamImplementations.add("Lorg.jinq.orm.stream.JinqStream;");
        jinqStreamImplementations.add("Lorg.jinq.orm.stream.NonQueryJinqStream;");
        jinqStreamImplementations.add("Lorg.jinq.orm.stream.QueryJinqStream;");
        jinqStreamImplementations.add("Lorg.jinq.orm.stream.LazyWrappedStream;");
        jinqStreamImplementations.add("Lorg.jinq.jpa.QueryJPAJinqStream;");
        jinqStreamImplementations.add("Lorg.jinq.jpa.JPAJinqStream;");
    }

    private boolean hasJinqStream;

    private boolean createsJinqStream;

    private boolean closesJinqStream;

    private boolean hasNonVoidReturnType;

    private Set<Integer> jinqStreams;

    public JinqStreamCloseInlining(int api, boolean hasNonVoidReturnType) {
        super(api);
        this.jinqStreams = new HashSet<>();
        this.hasNonVoidReturnType = hasNonVoidReturnType;
    }

    public JinqStreamCloseInlining(int api, MethodVisitor mv, boolean hasNonVoidReturnType) {
        super(api, mv);
        this.jinqStreams = new HashSet<>();
        this.hasNonVoidReturnType = hasNonVoidReturnType;
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
         if (jinqStreamImplementations.contains(signature)) {
             hasJinqStream = true;
             jinqStreams.add(index);
         }

        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitEnd() {
        if (!hasNonVoidReturnType && !closesJinqStream) {
            // Close all the streams
        }

        super.visitEnd();
    }
}
