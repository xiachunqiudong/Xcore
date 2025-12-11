OBJDUMP = riscv64-unknown-linux-gnu-objdump

TARGET = add

gcc:
	clang add.c start.S -o $(TARGET).elf \
	-T link.ld \
	-mabi=lp64 -march=rv64i -static -nostdlib -nostartfiles

dis:
	$(OBJDUMP) $(TARGET).elf -d > $(TARGET).dis

hex:
	python3 $(XCORE_HOME)/tools/elf_util.py $(shell pwd)/$(TARGET).elf
