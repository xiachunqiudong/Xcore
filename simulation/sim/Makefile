
TOP = xcore_tb

BUILD_DIR = ./build

FILELIST += -file $(XCORE_HOME)/rtl/simulation/filelist.f
FILELIST += -file $(XCORE_HOME)/rtl/chisel_gen/filelist.f
build:
	mkdir -p $(BUILD_DIR) && \
	cd $(abspath $(BUILD_DIR)) && \
	vcs -assert svaext -full64 -timescale=1ns/1ns +lint=TFIPC-L -debug_acc+all -debug_access -sverilog \
	-kdb -lca -DVCS \
	+vcs+loopreport+10000 \
	$(FILELIST) \
	-top $(TOP) -l build.log

sim:
	$(BUILD_DIR)/simv

vd:
	verdi -elab $(BUILD_DIR)/simv.daidir/kdb \
	-ssf $(BUILD_DIR)/tb.fsdb &

.PHONY: build vd
