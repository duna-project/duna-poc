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
    public int read(@NotNull byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        if (off + len > b.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        byte[] result = new byte[len];

        buffer.getBytes(position.get(), Math.min(len, buffer.length() - position.get()), result);
        System.arraycopy(result, 0, b, off, len);

        position.addAndGet(len);
        return len;
    }

    @Override
    public long skip(long n) throws IOException {
        long begin = n;

        while (position.incrementAndGet() < buffer.length() && n > 0) n--;
        return begin - n;
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
