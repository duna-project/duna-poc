/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.implementation.invocation;

import io.duna.core.implementation.bytecode.Jump;
import io.duna.core.implementation.bytecode.LabelAdder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.collection.ArrayAccess;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

/**
 * A bytecode appender used to call a method by using an array's items as
 * parameters.
 *
 * This instruction stacks all the items of an array, cast every item to
 * the respective method's parameter type and calls the target method.
 *
 * @author <a href="mailto:cemelo@redime.com.br">Carlos Eduardo Melo</a>
 */
public class DestructuringMethodCall implements ByteCodeAppender, Implementation {

    private MethodDescription target;

    /**
     * @param target the target method
     */
    public DestructuringMethodCall(MethodDescription target) {
        this.target = target;
    }

    /**
     * @param target the target method
     */
    public DestructuringMethodCall(Method target) {
        this.target = new MethodDescription.ForLoadedMethod(target);
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {
        return this;
    }

    @Override
    public InstrumentedType prepare(InstrumentedType instrumentedType) {
        return instrumentedType;
    }

    @Override
    public Size apply(MethodVisitor methodVisitor, Context implementationContext,
                      MethodDescription instrumentedMethod) {

        // TODO Validate the array length against the method's param count, and param types.

        final List<StackManipulation> paramLoadingInstructionList = new LinkedList<>();

        paramLoadingInstructionList.add(MethodVariableAccess.REFERENCE.loadFrom(1));
        paramLoadingInstructionList.add(TypeCasting.to(target.getDeclaringType()));

        for (int i = 0; i < target.getParameters().size(); i++) {
            TypeDescription.Generic parameterType =
                target.getParameters().get(i).getType();

            paramLoadingInstructionList.add(MethodVariableAccess.REFERENCE.loadFrom(2));
            paramLoadingInstructionList.add(IntegerConstant.forValue(i));
            paramLoadingInstructionList.add(ArrayAccess.REFERENCE.load());

            if (parameterType.isPrimitive()) {
                paramLoadingInstructionList.add(TypeCasting.to(parameterType.asErasure().asBoxed()));
                paramLoadingInstructionList.add(MethodInvocation.invoke(
                    parameterType.asErasure().asBoxed()
                        .getDeclaredMethods()
                        .filter(named(parameterType.asErasure().asUnboxed().getCanonicalName() + "Value"))
                        .getOnly()
                ));
            } else {
                paramLoadingInstructionList.add(TypeCasting.to(parameterType));
            }
        }

        StackManipulation parameterLoading =
            new StackManipulation.Compound(paramLoadingInstructionList);

        StackManipulation methodCall = new StackManipulation.Compound(
            parameterLoading,
            MethodInvocation.invoke(target)
        );

        StackManipulation returnResult;

        if (target.getReturnType().isPrimitive()) {
            TypeDescription boxedType = target.getReturnType().asErasure().asBoxed();

            returnResult = new StackManipulation.Compound(
                MethodInvocation.invoke(
                    boxedType
                        .getDeclaredMethods()
                        .filter(named("valueOf")
                            .and(takesArguments(target.getReturnType().asErasure())))
                        .getOnly()
                ),
                MethodReturn.REFERENCE
            );
        } else {
            returnResult = new StackManipulation.Compound(
                MethodReturn.REFERENCE
            );
        }

        StackManipulation methodBody = new StackManipulation.Compound(
            methodCall,
            returnResult
        );

        StackManipulation.Size operandStackSize = methodBody.apply(methodVisitor,
            implementationContext);

        return new Size(operandStackSize.getMaximalSize(),
            instrumentedMethod.getStackSize());
    }
}
