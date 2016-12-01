package io.duna.core.implementation.bytecode;

import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

/**
 * TODO refactor this
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
