/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.implementation.bytecode;

import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

/**
 * An instruction to provide a hint to the verifier about the current state of the stack map frame.
 *
 * TODO Support other kind of frame hints
 *
 * @author <a href="mailto:cemelo@redime.com.br">Carlos Eduardo Melo</a>
 */
public enum Frame implements StackManipulation {
    SAME(Opcodes.F_SAME);

    private int localOpcode;

    Frame(int localOpcode) {
        this.localOpcode = localOpcode;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext) {
        methodVisitor.visitFrame(localOpcode, 0, null, 0, null);
        return new Size(0, 0);
    }
}
