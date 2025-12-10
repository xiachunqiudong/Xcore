import sys
import os

chisel_gen_path = sys.argv[1]
filelist_path = f'{chisel_gen_path}/filelist.f'

# 读取所有行并处理
with open(filelist_path, 'r') as f:
    lines = [os.path.join(chisel_gen_path, line.strip()) + '\n' for line in f]

# 覆盖原文件
with open(filelist_path, 'w') as f:
    f.writelines(lines)