package ifu

import chisel3._
import chisel3.util._
import config._
import xcoreBundle._

class IfuTop extends XModule {
  val io = IO(new Bundle{
    val ifuARChannel = new ARChannel(AddrWidth = PADDR_WIDTH)
    val ifuRChannel  = new RChannel(DataWidth = AXI_DW)
    val instValidVec = Output(Vec(IFU_WIDTH, Bool()))
    val instVec      = Output(Vec(IFU_WIDTH, UInt(32.W)))
  })

  val pc_wen = Wire(Bool())
  val pc_In = Wire(UInt(XLEN.W))
  val pc_Q = RegInit(0.U(XLEN.W))

  val ifuRdataWordVec = Wire(Vec(4, UInt(32.W)))

  val fetchInstVec = Wire(Vec(IFU_WIDTH, UInt(32.W)))

  io.ifuARChannel.arValid := true.B
  io.ifuARChannel.arAddr  := Cat(pc_Q(PADDR_WIDTH-1,0), 0.U(4.W))

  io.ifuRChannel.rReady := true.B

  for (i <- 0 until 4) {
    ifuRdataWordVec := io.ifuRChannel.rData((i+1)*32-1,i*32)
  }

  for (i <- 0  until IFU_WIDTH) {
    io.instValidVec(i) := io.ifuRChannel.rValid
    io.instVec(i)      := io.ifuRChannel.rData((i+1)*32-1,i*32)
    fetchInstVec(i)    := io.ifuRChannel.rData((i+1)*32-1,i*32)
  }

  pc_In := pc_Q + 4.U

  pc_wen := io.ifuRChannel.rValid

  when(pc_wen) {
    pc_Q := pc_In
  }

  val instQueue = Module(new InstQueue(EntryNum=8, BankNum=1, ReadPotr=1, gen=UInt(32.W)))

  for (r <- 0 until IFU_WIDTH) {
    instQueue.io.fetchValidVec(r) := io.ifuRChannel.rValid
    instQueue.io.fetchInstVec(r)  := fetchInstVec(r)
  }

}
