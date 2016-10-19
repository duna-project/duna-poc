package io.duna.core.bytecode

import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.implementation.bytecode.StackManipulation
import net.bytebuddy.implementation.bytecode.StackSize
import net.bytebuddy.jar.asm.MethodVisitor
import net.bytebuddy.jar.asm.Opcodes

enum class MethodVariableStore(val storeOpcode: Int,
                               val shortcutOffset: Int,
                               val size: StackManipulation.Size) {

  INTEGER(Opcodes.ISTORE, 5, StackSize.SINGLE),
  LONG(Opcodes.LSTORE, 8, StackSize.DOUBLE),
  FLOAT(Opcodes.FSTORE, 11, StackSize.SINGLE),
  DOUBLE(Opcodes.DSTORE, 14, StackSize.DOUBLE),
  REFERENCE(Opcodes.ASTORE, 17, StackSize.SINGLE);

  constructor(opcode: Int,
              shortcutOffset: Int,
              size: StackSize) : this(opcode, shortcutOffset, size.toIncreasingSize())

  fun storeOffset(offset: Int): StackManipulation {
    return OffsetStoring(offset)
  }

  fun forType(typeDescription: TypeDescription): MethodVariableStore {
    if (typeDescription.isPrimitive) {
      return when {
        typeDescription.represents(Int::class.java) -> INTEGER
        typeDescription.represents(Long::class.java) -> LONG
        typeDescription.represents(Float::class.java) -> FLOAT
        typeDescription.represents(Double::class.java) -> DOUBLE
        else -> throw IllegalArgumentException("Invalid type for variable")
      }
    }

    return REFERENCE
  }

  protected inner class OffsetStoring(val variableOffset: Int) : StackManipulation {

    override fun apply(methodVisitor: MethodVisitor?,
                       implementationContext: Implementation.Context?): StackManipulation.Size {
      when (variableOffset) {
        in 0..3 -> methodVisitor?.visitInsn(
            this@MethodVariableStore.storeOpcode +
            this@MethodVariableStore.shortcutOffset +
            variableOffset)
        else -> methodVisitor?.visitVarInsn(this@MethodVariableStore.storeOpcode, variableOffset)
      }

      return this@MethodVariableStore.size
    }

    override fun isValid(): Boolean {
      return true
    }

    override fun toString(): String {
      return "MethodVariableStore.OffsetStoring{" +
          "MethodVariableStore=${this@MethodVariableStore}," +
          "variableOffset=${this.variableOffset}"
    }
  }
}