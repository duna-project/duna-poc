package io.duna.core.bytecode;

import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

public enum Jump {
    IF_IGREATER_EQUAL(Opcodes.IF_ICMPGE),
    IF_IEQUAL(Opcodes.IF_ICMPEQ);

    private Integer loadOpcode;

    Jump(Integer loadOpcode) {
        this.loadOpcode = loadOpcode;
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
                return new Size(-2, 0);
            }
        };
    }
}
