module xcore_tb;

  parameter PERIOD = 5;

  logic        clk;
  logic        rst;
  logic [63:0] cycleCnt_Q;

  initial begin
    clk = 0;
    rst = 1;
    #PERIOD rst = 0;
    forever #(PERIOD/2) clk = ~clk;
  end

  always @(posedge clk or posedge rst) begin
    if (rst) begin
      cycleCnt_Q[63:0] <= 'd0;
    end
    else begin
      cycleCnt_Q[63:0] <= cycleCnt_Q[63:0] + 'd1;
    end
  end

  always @(posedge clk) begin
    if (cycleCnt_Q[63:0] >= 'd20000) begin
      $fsdbDumpoff;
      $finish;
    end
  end

  parameter RAM_AW = 20;
  parameter AXI_AW = 32;
  parameter AXI_DW = 128;

  logic              arValid;
  logic              arReady;
  logic [AXI_AW-1:0] arAddr;
  logic              rValid;
  logic              rReady;
  logic [AXI_DW-1:0] rData;

  XcoreTop
  xcoreTop(
    .clock                          (clk),
    .reset                          (rst),
    .io_axiChannel_arChannel_arValid(arValid),
    .io_axiChannel_arChannel_arReady(arReady),
    .io_axiChannel_arChannel_arAddr (arAddr[AXI_AW-1:0]),
    .io_axiChannel_rChannel_rValid  (rValid),
    .io_axiChannel_rChannel_rReady  (rReady),
    .io_axiChannel_rChannel_rData   (rData[AXI_DW-1:0])
  );

  ram #(
    .AW(RAM_AW),
    .DW(AXI_DW)
  ) u_ram(
    .clk    (clk),
    .rst    (rst),
    .arValid(arValid),
    .arReady(arReady),
    .arAddr (arAddr[RAM_AW-1:0]),
    .rValid (rValid),
    .rReady (rReady),
    .rData  (rData[AXI_DW-1:0])
  );

  string fsdb_file = "/home/host/project/ysyx-workbench/xcore/sim/build/tb.fsdb";

  initial begin
    $fsdbDumpfile(fsdb_file, 1024);
    $fsdbDumpvars(0, xcore_tb);
    $fsdbDumpvars("+struct");
    $fsdbDumpvars("+mda");
    $fsdbDumpon;
  end

endmodule