package io.duna.core.service.invocation;

import io.duna.core.bytecode.ArrayLength;
import io.duna.core.bytecode.Frame;
import io.duna.core.bytecode.Jump;
import io.duna.core.bytecode.LabelAdder;
import io.duna.core.util.Primitives;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.*;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.collection.ArrayAccess;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class ProxyCallInterceptorMethod implements ByteCodeAppender {

    private Method target;

    ProxyCallInterceptorMethod(Method target) {
        this.target = target;
    }

    @Override
    public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext,
                      MethodDescription instrumentedMethod) {
        if (!instrumentedMethod.getReturnType().asErasure().represents(Object.class)) {
            throw new IllegalArgumentException(instrumentedMethod + " must return an object.");
        }

        List<StackManipulation> serviceInvocation = new LinkedList<>();

        LabelAdder validArgumentsLabelAdder = new LabelAdder();
        LabelAdder invalidArgumentsLabelAdder = new LabelAdder();

        // Validate the number of elements in the parameter array
        serviceInvocation.add(MethodVariableAccess.REFERENCE.loadFrom(2));
        serviceInvocation.add(ArrayLength.INSTANCE);
        serviceInvocation.add(IntegerConstant.forValue(target.getParameterCount()));
        serviceInvocation.add(Jump.IF_IEQUAL.goTo(validArgumentsLabelAdder));

        // Jump number of arguments is invalid, throw exception
        serviceInvocation.add(invalidArgumentsLabelAdder);
        serviceInvocation.add(TypeCreation.of(new TypeDescription.ForLoadedType(IllegalArgumentException.class)));
        serviceInvocation.add(Duplication.SINGLE);
        serviceInvocation.add(new TextConstant("Invalid number of elements in parameters array."));
        serviceInvocation.add(MethodInvocation.invoke(new MethodDescription.ForLoadedConstructor(getConstructor(IllegalArgumentException.class, String.class))));
        serviceInvocation.add(Throw.INSTANCE);

        // Load the service instance
        serviceInvocation.add(validArgumentsLabelAdder);
//        serviceInvocation.add(Frame.SAME);
        serviceInvocation.add(MethodVariableAccess.REFERENCE.loadFrom(1));

        // Load the parameters array
        for (int i = 0; i < target.getParameterCount(); i++) {
            serviceInvocation.add(MethodVariableAccess.REFERENCE.loadFrom(2));
            serviceInvocation.add(IntegerConstant.forValue(i));
            serviceInvocation.add(ArrayAccess.REFERENCE.load());

            if (target.getParameterTypes()[i].isPrimitive()) {
                Class<?> wrapperType = Primitives.getWrapper(target.getParameterTypes()[i]);

                serviceInvocation.add(TypeCasting.to(new TypeDescription.ForLoadedType(wrapperType)));
                serviceInvocation.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(
                        Primitives.getPrimitiveValueMethod(wrapperType))));
            } else {
                serviceInvocation.add(TypeCasting.to(new TypeDescription.ForLoadedType(
                        target.getParameterTypes()[i])));
            }
        }

        // Invoke the service method
        serviceInvocation.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(target)));

        if (target.getReturnType().isPrimitive()) {
            if (target.getReturnType().getCanonicalName().equals("void")) {
                serviceInvocation.add(MethodReturn.VOID);
            } else {
                serviceInvocation.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(
                        Primitives.getWrapperValueMethod(target.getReturnType()))));
                serviceInvocation.add(MethodReturn.REFERENCE);
            }
        } else {
            serviceInvocation.add(MethodReturn.REFERENCE);
        }

        StackManipulation.Size operandStackSize = new StackManipulation.Compound(serviceInvocation)
                .apply(methodVisitor, implementationContext);

        return new Size(operandStackSize.getMaximalSize(),
                instrumentedMethod.getStackSize());
    }

    private Constructor<?> getConstructor(Class<?> target, Class<?> ... parameters) {
        try {
            return target.getConstructor(parameters);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
