import os
import subprocess
import glob
import shutil
import zipfile
import struct

java_home = r'C:\Program Files\Android\Android Studio\jbr'
sdk_dir = r'C:\Users\gabri\AppData\Local\Android\Sdk'
ndk_bin = os.path.join(sdk_dir, r'ndk\28.2.13676358\toolchains\llvm\prebuilt\windows-x86_64\bin')
clang = os.path.join(ndk_bin, 'clang.exe')
objcopy = os.path.join(ndk_bin, 'llvm-objcopy.exe')
build_tools = os.path.join(sdk_dir, r'build-tools\35.0.0')
platform_jar = os.path.join(sdk_dir, r'platforms\android-34\android.jar')
keystore = r'C:\Users\gabri\.android\debug.keystore'

script_dir = os.path.dirname(os.path.abspath(__file__))
workspace_dir = os.path.abspath(os.path.join(script_dir, '..', '..'))
resources_dir = os.path.join(workspace_dir, 'resources')
original_theme_dir = os.path.join(resources_dir, 'original_theme')
themes_dir = os.path.join(resources_dir, 'themes')

scratch_dir = r'C:\Users\gabri\.gemini\antigravity-ide\brain\3b4c75e5-2565-42d9-83ef-7ef60e147fba\scratch'
unpacked_dir = os.path.join(scratch_dir, 'original_unpacked')
apktool_jar = os.path.join(scratch_dir, 'apktool.jar')
java_exe = os.path.join(java_home, r'bin\java.exe')

env = os.environ.copy()
env['JAVA_HOME'] = java_home
env['PATH'] = os.path.join(java_home, 'bin') + ';' + env.get('PATH', '')

print("=" * 60)
print("MINIROBOT THEME BUILDER (WITH DEFAULT THEME SUPPORT)")
print("=" * 60)

# Step 0: Restore authentic original assets in unpacked_dir/assets/res
print("\n[Step 0] Restoring authentic stock assets to assets/res/...")
res_dir = os.path.join(unpacked_dir, r'assets\res')
restored_count = 0
for root, dirs, files in os.walk(original_theme_dir):
    for f in files:
        src = os.path.join(root, f)
        rel = os.path.relpath(src, original_theme_dir)
        dst = os.path.join(res_dir, rel)
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        shutil.copy2(src, dst)
        restored_count += 1
print(f"Restored {restored_count} original asset files into {res_dir}")

# Sync themes (cyber, minimal, sport) to assets/themes/
print("\n[Step 0.1] Syncing themes into assets/themes/...")
assets_themes_dir = os.path.join(unpacked_dir, r'assets\themes')
for theme_name in ['cyber', 'minimal', 'sport']:
    src_theme = os.path.join(themes_dir, theme_name)
    dst_theme = os.path.join(assets_themes_dir, theme_name)
    if os.path.exists(src_theme):
        shutil.copytree(src_theme, dst_theme, dirs_exist_ok=True)
        print(f"Synced theme '{theme_name}' -> {dst_theme}")

# Step 1: Assemble ARM64 patch hook
print("\n[Step 1] Assembling ARM64 theme search paths hook...")
s_file = os.path.join(script_dir, 'patch_hook.s')
o_file = os.path.join(script_dir, 'patch_hook.o')
bin_file = os.path.join(script_dir, 'patch_hook.bin')

subprocess.run([clang, '--target=aarch64-linux-android24', '-c', s_file, '-o', o_file], check=True)
subprocess.run([objcopy, '-O', 'binary', '--only-section=.text', o_file, bin_file], check=True)

with open(bin_file, 'rb') as f:
    hook_bytes = bytearray(f.read())
print(f"Hook compiled: {len(hook_bytes)} bytes")

# Read ELF relocations from o_file
with open(o_file, 'rb') as f:
    elf_data = f.read()

e_shoff, = struct.unpack_from('<Q', elf_data, 40)
e_shentsize, e_shnum, e_shstrndx = struct.unpack_from('<HHH', elf_data, 58)
shstr_hdr = elf_data[e_shoff + e_shstrndx*e_shentsize : e_shoff + (e_shstrndx+1)*e_shentsize]
shstr_off, = struct.unpack_from('<Q', shstr_hdr, 24)

symtab_off, strtab_off, rela_off, rela_size = None, None, None, 0
for i in range(e_shnum):
    sh = elf_data[e_shoff + i*e_shentsize : e_shoff + (i+1)*e_shentsize]
    sh_name_idx, sh_type = struct.unpack_from('<II', sh, 0)
    name = elf_data[shstr_off + sh_name_idx:].split(b'\x00')[0].decode()
    sh_offset, sh_size = struct.unpack_from('<QQ', sh, 24)
    if name == '.symtab': symtab_off = sh_offset
    elif name == '.strtab': strtab_off = sh_offset
    elif name == '.rela.text': rela_off = sh_offset; rela_size = sh_size

def encode_bl(from_addr, to_addr):
    diff = to_addr - from_addr
    imm26 = (diff >> 2) & 0x03ffffff
    return 0x94000000 | imm26

hook_addr = 0x5542d0
call_site = 0x5178fc
plt_fopen = 0xbc4300
plt_fread = 0xbd1310
plt_fclose = 0xbc4320
plt_addSearchPath = 0xbc3110

target_map = {
    'target_fopen': plt_fopen,
    'target_fread': plt_fread,
    'target_fclose': plt_fclose,
    'target_addSearchPath': plt_addSearchPath,
}

for i in range(0, rela_size, 24):
    r_offset, r_info, r_addend = struct.unpack_from('<QQq', elf_data, rela_off + i)
    sym_idx = r_info >> 32
    st_name, = struct.unpack_from('<I', elf_data, symtab_off + sym_idx*24)
    sym_name = elf_data[strtab_off + st_name:].split(b'\x00')[0].decode()
    if sym_name in target_map:
        target_plt = target_map[sym_name]
        struct.pack_into('<I', hook_bytes, r_offset, encode_bl(hook_addr + r_offset, target_plt))
        print(f"  Resolved {sym_name} @ 0x{r_offset:x} -> PLT 0x{target_plt:x}")

so_path = os.path.join(unpacked_dir, r'lib\arm64-v8a\libcpp_empty_test.so')
print(f"\n[Step 2] Patching native library {so_path}...")
with open(so_path, 'rb') as f:
    so_data = bytearray(f.read())

so_data[hook_addr : hook_addr + len(hook_bytes)] = hook_bytes

struct.pack_into('<I', so_data, call_site, encode_bl(call_site, hook_addr))
for i in range(7):
    struct.pack_into('<I', so_data, call_site + 4 + i*4, 0xd503201f) # nop

with open(so_path, 'wb') as f:
    f.write(so_data)
print(f"Patched native hook at 0x{hook_addr:x} and call at 0x{call_site:x}!")

# Step 3: Compile ThemeManager.java
print("\n[Step 3] Compiling ThemeManager.java...")
classes_dir = os.path.join(script_dir, 'classes_out')
os.makedirs(classes_dir, exist_ok=True)
java_file = os.path.join(script_dir, 'ThemeManager.java')
javac = os.path.join(java_home, r'bin\javac.exe')
cmd = [javac, '--release', '8', '-cp', platform_jar, '-d', classes_dir, java_file]
subprocess.run(cmd, env=env, check=True)
print("javac successful!")

# Step 4: Convert to DEX with d8
print("\n[Step 4] Converting to DEX with d8...")
dex_dir = os.path.join(script_dir, 'dex_out')
os.makedirs(dex_dir, exist_ok=True)
d8 = os.path.join(build_tools, 'd8.bat')
class_files = glob.glob(os.path.join(classes_dir, '**', '*.class'), recursive=True)
cmd = [d8, '--lib', platform_jar, '--output', dex_dir] + class_files
subprocess.run(cmd, env=env, check=True)
print("d8 successful!")

# Step 5: Convert DEX to smali via apktool
print("\n[Step 5] Converting DEX to smali...")
dummy_apk = os.path.join(script_dir, 'dummy.apk')
with zipfile.ZipFile(dummy_apk, 'w') as zf:
    zf.write(os.path.join(dex_dir, 'classes.dex'), 'classes.dex')

dummy_out = os.path.join(script_dir, 'dummy_out')
if os.path.exists(dummy_out):
    shutil.rmtree(dummy_out)

cmd = [java_exe, '-jar', apktool_jar, 'd', '-f', '-r', dummy_apk, '-o', dummy_out]
subprocess.run(cmd, env=env, check=True)

src_smali = os.path.join(dummy_out, r'smali\org\cocos2dx\cpp')
dst_smali = os.path.join(unpacked_dir, r'smali\org\cocos2dx\cpp')
for f in glob.glob(os.path.join(src_smali, 'ThemeManager*.smali')):
    dest_path = os.path.join(dst_smali, os.path.basename(f))
    shutil.copy2(f, dest_path)
    print(f"  Copied smali: {os.path.basename(f)}")

# Step 6: Build APK with apktool
print("\n[Step 6] Building APK with apktool...")
unaligned_apk = os.path.join(script_dir, 'themed_app_unaligned.apk')
cmd = [java_exe, '-jar', apktool_jar, 'b', unpacked_dir, '-o', unaligned_apk]
subprocess.run(cmd, env=env, check=True)
print("apktool build finished!")

# Step 7: zipalign
print("\n[Step 7] Aligning APK with zipalign...")
final_apk = os.path.join(script_dir, 'MiniRobot_Themed_aligned.apk')
if os.path.exists(final_apk):
    os.remove(final_apk)
zipalign = os.path.join(build_tools, 'zipalign.exe')
cmd = [zipalign, '-p', '-f', '4', unaligned_apk, final_apk]
subprocess.run(cmd, env=env, check=True)

# Step 8: apksigner
print("\n[Step 8] Signing APK with apksigner...")
apksigner = os.path.join(build_tools, 'apksigner.bat')
cmd = [apksigner, 'sign', '--ks', keystore, '--ks-pass', 'pass:android', final_apk]
subprocess.run(cmd, env=env, check=True)

# Step 9: Save to workspace root
workspace_apk = os.path.join(workspace_dir, 'MiniRobot_Themed.apk')
shutil.copy2(final_apk, workspace_apk)
print("\n" + "=" * 60)
print(f"SUCCESS! APK generated and saved to:")
print(f"{workspace_apk} ({os.path.getsize(workspace_apk):,} bytes)")
print("Themes supported: Default (Stock), Minimal, Sport, Cyber")
print("=" * 60)
