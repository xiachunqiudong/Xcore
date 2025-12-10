package config

import chisel3._

class XModule extends Module {
  val XLEN: Int = 64
  val IFU_WIDTH: Int = 4

  val PADDR_WIDTH = 32
  val AXI_DW      = 128
}
