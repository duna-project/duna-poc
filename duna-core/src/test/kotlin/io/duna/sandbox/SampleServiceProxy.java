package io.duna.sandbox;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.duna.core.io.BufferInputStream;
import io.duna.core.io.BufferOutputStream;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.sync.Sync;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;

import static io.vertx.ext.sync.Sync.*;

public class SampleServiceProxy implements SampleService {

    @Inject
    private Vertx vertx;

    @Inject
    private ObjectMapper objectMapper;

    @Override
    @Suspendable
    public POJO call(String m, POJO n, double z, boolean x, List<Object> t) {
        try {
            final BufferOutputStream outputStream = new BufferOutputStream(1024);
            final JsonGenerator generator = objectMapper.getFactory()
                    .createGenerator(outputStream);

            generator.writeStartArray();

            generator.writeObject(m);
            generator.writeObject(n);
            generator.writeObject(z);
            generator.writeObject(x);
            generator.writeObject(t);

            generator.writeEndArray();
            generator.flush();

            Message<Buffer> response = awaitResult(new Consumer<Handler<AsyncResult<Message<Buffer>>>>() {
                @Override
                public void accept(Handler<AsyncResult<Message<Buffer>>> h) {
                    vertx.eventBus().send("address", outputStream.getBuffer(), h);
                }
            });

            final BufferInputStream inputStream = new BufferInputStream(response.body());
            final JsonParser parser = objectMapper.getFactory().createParser(inputStream);

            return parser.readValueAs(POJO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
