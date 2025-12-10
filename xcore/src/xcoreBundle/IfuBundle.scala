package xcoreBundle

import chisel3._

class InstDcd extends Bundle {
  val src1Ren = Bool()
  val src2Ren = Bool()
  val dstWen  = Bool()
}

