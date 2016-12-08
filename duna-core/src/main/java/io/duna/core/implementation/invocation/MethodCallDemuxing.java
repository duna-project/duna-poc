/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.implementation.invocation;

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

public class MethodCallDemuxing implements ByteCodeAppender, Implementation {

    private MethodDescription target;

    public MethodCallDemuxing(MethodDescription target) {
        this.target = target;
    }

    public MethodCallDemuxing(Method target) {
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
    public Size apply(MethodVisitor methodVisitor, Context implementationContext, MethodDescription instrumentedMethod) {
        LabelAdder validMethodCall = new LabelAdder();

        StackManipulation parameterValidation = new StackManipulation.Compound(
            FieldAccess.forField(
                new TypeDescription.ForLoadedType(MethodCallDemuxingValidator.class)
                    .getDeclaredFields()
                    .filter(named("INSTANCE"))
                    .getOnly()
            ).read(),

            MethodVariableAccess.REFERENCE.loadFrom(0),

            FieldAccess.forField(
                implementationContext
                    .getInstrumentedType()
                    .getDeclaredFields()
                    .filter(named("method")).getOnly()
            ).read(),

            MethodVariableAccess.REFERENCE.loadFrom(2),
            MethodInvocation.invoke(
                new TypeDescription.ForLoadedType(MethodCallDemuxingValidator.class)
                    .getDeclaredMethods()
                    .filter(named("isValid"))
                    .getOnly()
            )
            // Jump.IF_NE.goTo(validMethodCall)
        );

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
//            parameterValidation,
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
