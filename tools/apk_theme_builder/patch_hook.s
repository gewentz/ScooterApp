.text
.align 2
.global setup_theme_search_paths

setup_theme_search_paths:
    stp x29, x30, [sp, #-80]!
    stp x19, x20, [sp, #16]
    mov x29, sp
    mov x19, x0 // x19 = FileUtils*

    // Try opening /data/data/com.loby.balance.car.google/files/theme.txt
    adr x0, path_theme
    adr x1, str_mode_r
    bl target_fopen
    cbz x0, is_default

    mov x20, x0 // x20 = fp
    add x0, sp, #32 // buffer for 1 char
    mov w1, #1
    mov w2, #1
    mov x3, x20
    bl target_fread

    mov x0, x20
    bl target_fclose

    ldrb w0, [sp, #32]
    cmp w0, #'d' // 'd' -> default (restaura visual original)
    b.eq is_default
    cmp w0, #'s' // 's' -> sport
    b.eq is_sport
    cmp w0, #'c' // 'c' -> cyber
    b.eq is_cyber
    cmp w0, #'m' // 'm' -> minimal
    b.eq is_minimal
    b is_default // fallback seguro para o visual original

is_sport:
    // "themes/sport" -> length 12, SSO byte = 12 << 1 = 24
    mov w2, #24
    ldr x3, =0x000074726f7073 // "sport\0\0\0"
    b apply_theme

is_cyber:
    // "themes/cyber" -> length 12, SSO byte = 12 << 1 = 24
    mov w2, #24
    ldr x3, =0x00007265627963 // "cyber\0\0\0"
    b apply_theme

is_minimal:
    // "themes/minimal" -> length 14, SSO byte = 14 << 1 = 28
    mov w2, #28
    ldr x3, =0x6c616d696e696d // "minimal\0"
    b apply_theme

apply_theme:
    // Clear 24-byte SSO string buffer at sp+48
    stp xzr, xzr, [sp, #48]
    str xzr, [sp, #64]

    // Set SSO length flag (len << 1)
    strb w2, [sp, #48]

    // Store "themes/" at sp+49
    ldr x4, =0x002f73656d656874 // "themes/\0"
    str x4, [sp, #49]

    // Store suffix at sp+56
    str x3, [sp, #56]

    // Call FileUtils::addSearchPath(fu, sso_theme, true) -> busca no tema prioritariamente
    mov x0, x19
    add x1, sp, #48
    mov w2, #1 // front = true
    bl target_addSearchPath

is_default:
    // Construct SSO string for "res" (len 3)
    stp xzr, xzr, [sp, #48]
    str xzr, [sp, #64]
    mov w4, #6 // 3 << 1 = 6
    strb w4, [sp, #48]
    mov w5, #0x6572 // 'r', 'e'
    movk w5, #0x73, lsl #16 // 's', '\0'
    str w5, [sp, #49]

    // Call FileUtils::addSearchPath(fu, sso_res, false) -> busca na pasta original res
    mov x0, x19
    add x1, sp, #48
    mov w2, wzr // front = false
    bl target_addSearchPath

    // Epilogue
    ldp x19, x20, [sp, #16]
    ldp x29, x30, [sp], #80
    ret

// String literals
.align 2
str_mode_r:
    .asciz "r"
.align 2
path_theme:
    .asciz "/data/data/com.loby.balance.car.google/files/theme.txt"
