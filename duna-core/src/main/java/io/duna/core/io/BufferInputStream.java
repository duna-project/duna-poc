/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.io;

import io.vertx.core.buffer.Buffer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class BufferInputStream extends InputStream {

    private Buffer buffer;

    private AtomicInteger position = new AtomicInteger(0);

    public BufferInputStream(Buffer buffer) {
        this.buffer = buffer;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    @Override
    public int read() throws IOException {
        return buffer.getByte(position.getAndIncrement());
    }

    @Override
    public int read(@NotNull byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    @Override
    public int read(@NotNull byte[] bytes, int off, int len) throws IOException {
        if (off + len > bytes.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        byte[] result = new byte[len];

        buffer.getBytes(position.get(), Math.min(position.get() + len, buffer.length()), result);
        System.arraycopy(result, 0, bytes, off, len);

        position.addAndGet(len);
        return len;
    }

    @Override
    public long skip(long amount) throws IOException {
        long end = amount;

        while (position.incrementAndGet() < buffer.length() && end > 0) end--;
        return amount - end;
    }

    @Override
    public int available() throws IOException {
        return buffer.length() - position.get();
    }

    @Override
    public synchronized void reset() throws IOException {
        position.set(0);
    }

    @Override
    public boolean markSupported() {
        return false;
    }
}
