import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * Created by carlos on 02/12/16.
 */
public class SendingVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        startFuture.complete();

        vertx.eventBus().<String> send("receive", "request", r -> {
           System.out.println(r.result());
        });
    }
}
