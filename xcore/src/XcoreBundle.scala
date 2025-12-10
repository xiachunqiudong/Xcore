package xcoreBundle

import chisel3._

class Uop extends Bundle {
  val lsrc1 = UInt(5.W)
  val lsrc2 = UInt(5.W)
  val ldst  = UInt(5.W)
}
