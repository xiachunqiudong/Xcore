module ram #(
  parameter DW = 128, // 16Byte
  parameter AW = 16,
  parameter BW = $clog2(DW>>3)
) (
  input  wire          clk,
  input  wire          rst,
  input  wire          arValid,
  output wire          arReady,
  input  wire [AW-1:0] arAddr,
  output wire          rValid,
  input  wire          rReady,
  output wire [DW-1:0] rData
);

  parameter IDLE      = 2'b00;
  parameter READ_DATA = 2'b01;

  reg [1:0] state_In;
  reg [1:0] state_Q;

  parameter DEPTH = 1 << (AW - BW);

  reg [DW-1:0] dataArray [DEPTH-1:0];
  reg [DW-1:0] rdata_Q;

  always @(*) begin
    state_In = state_Q;
    case (state_Q)
      IDLE: begin
        if (arValid) begin
          state_In = READ_DATA;
        end
      end
      READ_DATA: begin
        if (rReady) begin
          state_In = IDLE;
        end
      end
      default: state_In = IDLE;
    endcase
  end

  always @(posedge clk or posedge rst) begin
    if (rst) begin
      state_Q <= IDLE;
    end
    else begin
      state_Q <= state_In;
    end
  end

  assign arReady = state_Q == IDLE;

// read
  always @(posedge clk ) begin
    if (arValid & arReady) begin
      rdata_Q[DW-1:0] <= dataArray[arAddr[AW-1:BW]];
    end
  end

  assign rValid         = state_Q == READ_DATA;
  assign rData[DW-1:0]  = rdata_Q[DW-1:0];

// write
  // always @(posedge clk ) begin
  //   if (wen) begin
  //     dataArray[addr] <= wdata[DW-1:0];
  //   end
  // end

  string ram_init_path;
  
  initial begin
    if ($value$plusargs("ram_init_path=%s", ram_init_path)) begin
      $readmemh(ram_init_path, dataArray);
    end
    else begin
      $display("Can not find ram init path!");
    end
  end

endmodule
