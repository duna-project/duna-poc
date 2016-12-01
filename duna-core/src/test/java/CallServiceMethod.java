import co.paralleluniverse.fibers.SuspendExecution;
import io.duna.core.implementation.MethodCallDemuxingValidator;
import io.duna.example.ServiceB;

import java.lang.reflect.Method;

public class CallServiceMethod {

    private Method method;

    public Object call(ServiceB service, Object[] parameters) throws SuspendExecution {
        if (!(MethodCallDemuxingValidator.INSTANCE.isValid(method, parameters))) {
            throw new IllegalArgumentException();
        }

        return service.forwardPing((int) parameters[0], (String) parameters[1]);
    }
}
