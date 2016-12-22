/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.io;

import io.vertx.core.buffer.Buffer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public class BufferOutputStream extends OutputStream {

    private Buffer buffer;

    public BufferOutputStream() {
        this.buffer = Buffer.buffer();
    }

    public BufferOutputStream(Buffer buffer) {
        this.buffer = buffer;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    @Override
    public void write(int b) throws IOException {
        buffer.appendByte((byte) b);
    }

    @Override
    public void write(@NotNull byte[] b) throws IOException {
        buffer.appendBytes(b);
    }

    @Override
    public void write(@NotNull byte[] b, int off, int len) throws IOException {
        buffer.appendBytes(b, off, len);
    }
}
