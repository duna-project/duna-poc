package io.duna.core.service.impl;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.core.JsonParser;
import io.duna.core.DunaException;
import io.duna.core.io.BufferInputStream;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;

public class ASMActionHandler implements Handler<Message<Buffer>> {

    private Object service;

    @Override
    @Suspendable
    public void handle(Message<Buffer> event) {
        try {
            BufferInputStream inputStream = new BufferInputStream(event.body());
            JsonParser parser = Json.mapper.getFactory().createParser(inputStream);

            // TODO: Add filter support here

            parser.nextToken(); // START_OBJECT

            // Read fields
            parser.nextToken(); // FIELD_NAME
            parser.nextToken(); // FIELD_VALUE
            Object field0 = parser.readValuesAs(Object.class);

            parser.close();

            Object response = service.equals(field0);

            event.reply(response);
        } catch (Throwable t) {
            throw new DunaException(t);
        }
    }
}
