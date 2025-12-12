package ifu

import chisel3._
import chisel3.util._

class leftCircularShift(DW: Int) extends Module {
  val io = IO(new Bundle {
    val in       = Input(UInt(DW.W))
    val shiftNum =  Input(UInt(log2Ceil(DW).W))
    val out      = Output(UInt(DW.W))
  })

  val data = io.in

  val shiftCandidates = Wire(Vec(DW, UInt(DW.W)))
  dontTouch(shiftCandidates)

  shiftCandidates(0) := data

  for (i <- 1 until DW) {
    shiftCandidates(i) := Cat(data((DW-1-i), 0), data((DW-1), (DW-i)))
  }

  io.out := Mux1H(UIntToOH(io.shiftNum), shiftCandidates)

}

class BasicFifo[T <: Data] (EntryNum: Int, gen: T) extends Module {
  val io = IO(new Bundle {
    val push = Input(Bool())
    val pop  = Input(Bool())
    val din  = Input(gen)
    val dout = Output(gen)
  })

  require(isPow2(EntryNum), s"FIFO Entry Num must be power of 2, but $EntryNum")

  val ptrWidth = log2Ceil(EntryNum)

  val entryArray = Seq.fill(EntryNum)(Reg(gen))

  val writePtr_Q = RegInit(0.U(ptrWidth.W))
  val readPtr_Q = RegInit(0.U(ptrWidth.W))

  val readPtrDcd    = UIntToOH(readPtr_Q)
  val writePtrDcd   = UIntToOH(writePtr_Q)
  val writePtrDcdWv = Wire(Vec(EntryNum, Bool()))

  dontTouch(readPtrDcd)
  dontTouch(writePtrDcd)
  dontTouch(writePtrDcdWv)

//------------------------------------------------------------
//                           Write
//------------------------------------------------------------
  when (io.pop) {
    readPtr_Q := readPtr_Q + 1.U
  }

  for (i <- 0 until EntryNum) {
    writePtrDcdWv(i) := writePtrDcd(i) & io.push
  }

  for (e <- 0 until EntryNum) {
    when (writePtrDcdWv(e)) {
      entryArray(e) := io.din
    }
  }

//------------------------------------------------------------
//                           Read
//------------------------------------------------------------
  when (io.push) {
    writePtr_Q := writePtr_Q + 1.U
  }

  io.dout := Mux1H(readPtrDcd, entryArray)

}

class InstQueue[T <: Data] (EntryNum: Int, BankNum: Int, ReadPotr: Int, gen: T) extends Module {
  val io = IO(new Bundle{
    val fetchValidVec  = Input(Vec(BankNum, Bool()))
    val enqueueAllowIn = Output(Bool())
    val fetchInstVec   = Input(Vec(BankNum, gen))
  })

  val bankPtrWidth    = log2Ceil(BankNum)
  val sizeWidth       = log2Ceil(EntryNum) + 1
  val EntryNumPerBank = EntryNum / BankNum

  val enqueueValid = io.fetchValidVec.asUInt.orR
  val enqueueNum   = Wire(UInt(sizeWidth.W))
  val dequeueNum   = Wire(UInt(sizeWidth.W))
  val queueSize_Q  = RegInit(0.U(sizeWidth.W))
  val queueSize_In = Wire(UInt(sizeWidth.W))
  dontTouch(queueSize_In)
  dontTouch(queueSize_Q)

  io.enqueueAllowIn := queueSize_Q + enqueueNum <= EntryNum.U

  val enqueueFire = enqueueValid & io.enqueueAllowIn

  enqueueNum   := PopCount(io.fetchValidVec)
  dequeueNum   := 0.U
  queueSize_In := queueSize_Q + Mux(enqueueFire, enqueueNum, 0.U) - dequeueNum

  when (enqueueFire) {
    queueSize_Q := queueSize_In
  }
  val InstFifoVec = Seq.fill(BankNum)(Module(new BasicFifo(EntryNum=EntryNumPerBank,gen=gen)))

  for (i <- 0 until BankNum) {
    InstFifoVec(i).io.push := io.fetchValidVec(i)
    InstFifoVec(i).io.pop  := false.B
    InstFifoVec(i).io.din  := io.fetchInstVec(i)
  }

}