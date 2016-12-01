package io.duna.core.implementation.bytecode;

import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.jar.asm.MethodVisitor;

/**
 * Created by carlos on 29/11/16.
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
