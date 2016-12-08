/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.io

import io.vertx.core.buffer.Buffer
import java.io.OutputStream

/**
 * Wraps a [io.vertx.core.buffer.Buffer] with an [java.io.OutputStream].
 *
 * @author Carlos Eduardo Melo [ceduardo.melo@gmail.com]
 * @since 0.1
 *
 * @see io.vertx.core.buffer.Buffer
 * @see java.io.OutputStream
 */
class BufferOutputStream(val buffer: Buffer) : OutputStream() {

  constructor(initialBufferSize: Int): this(Buffer.buffer(initialBufferSize))
  constructor(bytes: ByteArray): this(Buffer.buffer(bytes))

  override fun write(b: Int) {
    buffer.appendByte(b.toByte())
  }

  override fun write(b: ByteArray?) {
    buffer.appendBytes(b)
  }

  override fun write(b: ByteArray?, off: Int, len: Int) {
    buffer.appendBytes(b, off, len)
  }
}
