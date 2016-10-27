package io.duna.core.proxy_gen.internal;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.duna.asm.Type;
import io.duna.core.io.BufferInputStream;
import io.duna.core.io.BufferOutputStream;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.sync.Sync;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public class ASMTypes {
    static final Type BUFFER_TYPE = Type.getType(Buffer.class);
    static final Type BUFFER_INPUT_STREAM = Type.getType(BufferInputStream.class);
    static final Type BUFFER_OUTPUT_STREAM = Type.getType(BufferOutputStream.class);
    static final Type CLASS_TYPE = Type.getType(Class.class);
    static final Type CONSUMER_TYPE = Type.getType(Consumer.class);
    static final Type EVENT_BUS_TYPE = Type.getType(EventBus.class);
    static final Type INPUT_STREAM_TYPE = Type.getType(InputStream.class);
    static final Type JSON_FACTORY = Type.getType(JsonFactory.class);
    static final Type JSON_GENERATOR = Type.getType(JsonGenerator.class);
    static final Type JSON_PARSER_TYPE = Type.getType(JsonParser.class);
    static final Type MESSAGE_TYPE = Type.getType(Message.class);
    static final Type OBJECT_TYPE = Type.getType(Object.class);
    static final Type OBJECT_MAPPER = Type.getType(ObjectMapper.class);
    static final Type OUTPUT_STREAM_TYPE = Type.getType(OutputStream.class);
    static final Type STRING_TYPE = Type.getType(String.class);
    static final Type SYNC_TYPE = Type.getType(Sync.class);
    static final Type VERTX = Type.getType(Vertx.class);
    static final Type VERTX_HANDLER_TYPE = Type.getType(Handler.class);
}
