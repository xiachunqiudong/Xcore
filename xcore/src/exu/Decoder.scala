package exu

import chisel3._
import chisel3.util._
import chisel3.util.experimental.decode._
import config._
import xcoreBundle._


class InstInfoPla extends XModule {
  val io = IO(new Bundle{
    val inst    = Input(UInt(32.W))
    val instDcd = Output(new InstDcd()) 
  })

  val instDcd = Wire(UInt(3.W))

  instDcd := decoder(io.inst, RV32I_InstTable.RV32I_MAP)

  io.instDcd.src1Ren := instDcd(0)
  io.instDcd.src2Ren := instDcd(1)
  io.instDcd.dstWen  := instDcd(2)

}

class Decoder extends XModule {
  val io = IO(new Bundle{
    val inst = Input(UInt(32.W))
    val uop  = Output(new Uop)
  })

  io.uop.lsrc1 := io.inst(19,15)
  io.uop.lsrc2 := io.inst(24,20)
  io.uop.ldst  := io.inst(11,7)

  val instDcd = Wire(new InstDcd())

  val instInfoPla = Module(new InstInfoPla)

  instInfoPla.io.inst := io.inst

  instDcd := instInfoPla.io.instDcd


  
}
