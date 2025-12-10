package xcore

import chisel3._
import chisel3.util._
import config._
import xcoreBundle._
import ifu._
import exu._

class XcoreTop extends XModule with RequireAsyncReset {

  val io = IO(new Bundle {
    val axiChannel = new AmbaAxiBundle(AddrWidth=32, DataWidth=AXI_DW)
  })

  val ifuTop = Module(new IfuTop)

  val decoderVec = Seq.fill(IFU_WIDTH)(Module(new Decoder))

  io.axiChannel.arChannel <> ifuTop.io.ifuARChannel
  io.axiChannel.rChannel  <> ifuTop.io.ifuRChannel

  for (i <- 0 until IFU_WIDTH) {
    decoderVec(i).io.inst := ifuTop.io.instVec(i)
  }

}