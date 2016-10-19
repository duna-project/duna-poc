package io.duna.sandbox;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.duna.core.io.BufferInputStream;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SampleService$callHandler implements Handler<Message<Buffer>> {

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private SampleService service;

    @Override
    @Suspendable
    public void handle(Message<Buffer> event) {
        try {
            final BufferInputStream inputStream = new BufferInputStream(event.body());
            final JsonParser parser = objectMapper.getFactory().createParser(inputStream);

            final String m;
            final POJO n;
            final Integer z;
            final Boolean x;
            final List<Object> t;

            // Array token
            parser.nextToken();

            parser.nextValue();
            m = parser.readValueAs(String.class);

            parser.nextValue();
            n = parser.readValueAs(POJO.class);

            parser.nextValue();
            z = parser.readValueAs(Integer.class);

            parser.nextValue();
            x = parser.readValueAs(Boolean.class);

            parser.nextValue();
            parser.nextToken();
            t = new LinkedList<>();
            parser.readValuesAs(Object.class).forEachRemaining(t::add);

            final Fiber<POJO> result = new Fiber<POJO>() {
                @Override
                protected POJO run() throws SuspendExecution, InterruptedException {
                    return service.call(m, n, z, x, t);
                }
            };

            event.reply(result.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
