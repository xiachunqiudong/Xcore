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

  val fetchInstVec = Wire(Vec(IFU_WIDTH, UInt(32.W)))

  io.ifuARChannel.arValid := true.B
  io.ifuARChannel.arAddr  := pc_Q(PADDR_WIDTH-1,0)

  io.ifuRChannel.rReady := true.B


  for (i <- 0  until IFU_WIDTH) {
    io.instValidVec(i) := io.ifuRChannel.rValid
    io.instVec(i)      := io.ifuRChannel.rData((i+1)*32-1,i*32)
    fetchInstVec(i)    := io.ifuRChannel.rData((i+1)*32-1,i*32)
  }

  pc_In := pc_Q + 4.U

  pc_wen := io.ifuARChannel.arValid & io.ifuARChannel.arReady

  when(pc_wen) {
    pc_Q := pc_In
  }

  val instQueue = Module(new InstQueue(EntryNum=32, BankNum=4, ReadPotr=4, WritePort=4))

  for (r <- 0 until IFU_WIDTH) {
    instQueue.io.fetchValidVec(r) := true.B
    instQueue.io.fetchInstVec(r) := fetchInstVec(r)
  }

}

