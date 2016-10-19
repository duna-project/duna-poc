package io.duna.core.io

import io.vertx.core.buffer.Buffer
import java.io.InputStream
import java.util.concurrent.atomic.AtomicLong

/**
 * Wraps a [io.vertx.core.buffer.Buffer] with an [java.io.InputStream].
 *
 * @author Carlos Eduardo Melo [ceduardo.melo@gmail.com]
 * @since 0.1
 *
 * @see io.vertx.core.buffer.Buffer
 * @see java.io.InputStream
 */
class BufferInputStream(val buffer: Buffer) : InputStream() {

  constructor(bytes: ByteArray): this(Buffer.buffer(bytes))

  var pos: AtomicLong = AtomicLong(0L)

  override fun read(): Int {
    return buffer.getByte(pos.andIncrement.toInt()).toInt()
  }

  override fun read(b: ByteArray?): Int {
    buffer.getBytes(b)
    return b?.size ?: 0
  }

  override fun read(b: ByteArray?, off: Int, len: Int): Int {
    println(b?.size)
    buffer.getBytes(off, Math.min(off + len - 1, buffer.length()), b)
    return b?.size ?: 0
  }

  override fun skip(n: Long): Long {
    return pos.addAndGet(n)
  }

  override fun available(): Int {
    return (buffer.length() - pos.get()).toInt()
  }

  override fun reset() {
    pos.set(0)
  }

  override fun markSupported(): Boolean {
    return false
  }
}