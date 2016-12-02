import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.duna.core.proxy.RemoteServiceProxyFactory;
import io.vertx.core.json.Json;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;

import java.lang.reflect.Field;

/**
 * Created by carlos on 02/12/16.
 */
public class ReceivingVerticle extends SyncVerticle {

    private final Class<ServiceA> proxyClass = new RemoteServiceProxyFactory()
        .loadProxyForService(ServiceA.class);

    @Override
    @Suspendable
    public void start() throws Exception {
        ServiceA proxy = proxyClass.newInstance();

        Field vertxField = proxyClass.getDeclaredField("vertx");
        Field mapperField = proxyClass.getDeclaredField("objectMapper");

        vertxField.setAccessible(true);
        vertxField.set(proxy, vertx);

        mapperField.setAccessible(true);
        mapperField.set(proxy, Json.prettyMapper);

        vertx.eventBus().<String> consumer("receive", Sync.fiberHandler(r -> {
            String result = proxy.ping(r.body());

            System.out.println(result);

            r.reply(result);
        }));

        System.out.println("Deployed");
    }
}
