import co.paralleluniverse.fibers.SuspendExecution;
import io.duna.example.ServiceB;

public class CallServiceMethod {
    public Object call(ServiceB service, Object[] parameters) throws SuspendExecution {
        if (parameters.length != 2) {
            throw new IllegalArgumentException("Invalid number of elements in parameters array.");
        }

        return service.forwardPing((int) parameters[0], (String) parameters[1]);
    }
}
