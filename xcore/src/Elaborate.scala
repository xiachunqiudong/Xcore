object Elaborate extends App {

  val firtoolOptions = Array(
    "-O=release",
    "--disable-opt",
    "--disable-all-randomization",
    "--split-verilog",
    "-strip-debug-info",
    "-strip-fir-debug-info",
    "--add-mux-pragmas",
    "--emit-separate-always-blocks",
    "--lowering-options=explicitBitcast,disallowLocalVariables,disallowPortDeclSharing,locationInfoStyle=none",
    "--disable-aggressive-merge-connections",
    "--verilog"
  )

  circt.stage.ChiselStage.emitSystemVerilogFile(new xcore.XcoreTop(), args, firtoolOptions)
}
