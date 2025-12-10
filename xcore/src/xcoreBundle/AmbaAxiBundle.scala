package xcoreBundle

import chisel3._

class ARChannel (AddrWidth: Int) extends Bundle {
  val arValid = Output(Bool())
  val arReady = Input(Bool())
  val arAddr  = Output(UInt(AddrWidth.W))
}

class RChannel (DataWidth: Int) extends Bundle {
  val rValid = Input(Bool())
  val rReady = Output(Bool())
  val rData  = Input(UInt(DataWidth.W))
}

class AmbaAxiBundle (AddrWidth: Int, DataWidth: Int) extends Bundle {
  val arChannel = new ARChannel(AddrWidth)
  val rChannel = new RChannel(DataWidth)
}
