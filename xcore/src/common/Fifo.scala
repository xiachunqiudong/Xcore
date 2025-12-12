package common

import chisel3._
import chisel3.util._

class FIFO[T <: Data] (DW: Int, gen: T) extends Module {
  val io = IO(new Bundle{
    val push  = Input(Bool())
    val pop   = Input(Bool())
    val empty = Output(Bool())
    val full  = Output(Bool())
    val din   = Input(gen)
    val dout  = Output(gen)
  })

  val entryNum = 1 << DW

  val dataArray = Seq.fill(entryNum)(gen)

  val readPtr  = RegInit(0.U(DW.W))
  val writePtr = RegInit(0.U(DW.W))

  when(io.push) {
    writePtr := writePtr + 1.U
  }

  when(io.pop) {
    readPtr := readPtr + 1.U
  }

  // Write
  for (i <- 0 until entryNum) {
    if (writePtr == i.U) {
      dataArray(i) := io.din
    }
  }

  // Read
  io.dout := Mux1H(UIntToOH(readPtr), dataArray)

}

