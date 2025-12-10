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

class InstFifoEntry extends Module {
  val io = IO(new Bundle {
    val wen     = Input(Bool())
    val inst_In = Input(UInt(32.W))
    val inst_Q  = Output(UInt(32.W))
  })

  val instReg = RegEnable(io.inst_In, io.wen)

  io.inst_Q := instReg

}

class InstFifo (EntryNum: Int) extends Module {
  val io = IO(new Bundle {
    val push    = Input(Bool())
    // val pop     = Input(Bool())
    val inst_In = Input(UInt(32.W))
    val inst_Q  = Output(UInt(32.W))
  })

  val entryArray = Seq.fill(EntryNum)(Module(new InstFifoEntry))

// Write
  val writePtrDcd_In = Wire(UInt(EntryNum.W))
  val writePtrDcd_Q  = RegInit(0.U(EntryNum.W))

  writePtrDcd_In := Cat(writePtrDcd_Q(EntryNum-2,0), writePtrDcd_Q(EntryNum-1))

  when (io.push) {
    writePtrDcd_Q := writePtrDcd_In
  }

// Read

  for (e <- 0 until EntryNum) {
    entryArray(e).io.wen     := io.push & (writePtrDcd_Q(e))
    entryArray(e).io.inst_In := io.inst_In
  }

  io.inst_Q := entryArray(0).io.inst_Q

}

class InstQueue (EntryNum: Int, BankNum: Int, ReadPotr: Int, WritePort: Int) extends Module {
  val io = IO(new Bundle{
    val fetchValidVec = Input(Vec(WritePort, Bool()))
    val fetchInstVec  = Input(Vec(WritePort, UInt(32.W)))
  })

  def bitRotateLeft(data: UInt, shiftNum: UInt): UInt = {
    val W = data.getWidth
    val shiftCandidates = (0 until W).map { k =>
      if (k == 0) data
      else Cat(data((W-1-k), 0), data((W-1), (W-k)))
    }
    Mux1H(UIntToOH(shiftNum), shiftCandidates)
  }

  val bankPtrWidth = log2Ceil(BankNum)

  val EntryNumPerBank = EntryNum / BankNum

  val InstFifoVec = Seq.fill(BankNum)(Module(new InstFifo(EntryNum=EntryNumPerBank)))

  val bankWritePtr_Q = RegInit(0.U(bankPtrWidth.W))

  val fetchValidVecShifter = Module(new leftCircularShift(WritePort))
  fetchValidVecShifter.io.in       := io.fetchValidVec.asUInt
  fetchValidVecShifter.io.shiftNum := bankWritePtr_Q

  val fetchValidVecShift = fetchValidVecShifter.io.out
  dontTouch(fetchValidVecShift)

  for (bank <- 0 until BankNum) {
    InstFifoVec(bank).io.push    := fetchValidVecShift(bank)
    InstFifoVec(bank).io.inst_In :=  io.fetchInstVec(0)
  }

}