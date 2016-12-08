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

public enum Jump {
    IF_IGREATER_EQUAL(Opcodes.IF_ICMPGE, new StackManipulation.Size(-2 , 0)),
    IF_IEQUAL(Opcodes.IF_ICMPEQ, new StackManipulation.Size(-2 , 0)),
    IF_NE(Opcodes.IFNE, new StackManipulation.Size(-1 , 0));

    private Integer loadOpcode;

    private StackManipulation.Size sizeImpact;

    Jump(Integer loadOpcode, StackManipulation.Size sizeImpact) {
        this.loadOpcode = loadOpcode;
        this.sizeImpact = sizeImpact;
    }

    public StackManipulation goTo(LabelAdder labelAdder) {
        return new StackManipulation() {
            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext) {
                methodVisitor.visitJumpInsn(loadOpcode, labelAdder.asmLabel);
                return sizeImpact;
            }
        };
    }
}
