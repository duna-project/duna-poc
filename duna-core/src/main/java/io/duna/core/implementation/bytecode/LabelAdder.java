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

/**
 * A stack manipulation used to add a label to the bytecode.
 *
 * A label can be used to perform jumps, goto, and switch instructions
 * and mark try-catch blocks.
 *
 * @author <a href="mailto:cemelo@redime.com.br">Carlos Eduardo Melo</a>
 * @see net.bytebuddy.jar.asm.Label
 */
public class LabelAdder implements StackManipulation {

    net.bytebuddy.jar.asm.Label asmLabel;

    public LabelAdder() {
        this.asmLabel = new net.bytebuddy.jar.asm.Label();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext) {
        methodVisitor.visitLabel(asmLabel);
        return new Size(0, 0);
    }
}
