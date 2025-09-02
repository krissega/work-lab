import ctypes
import os
import sys

# ========= ABSOLUTE PATHS (raw strings) =========
USER = r"/Users/usr_rinner"  # <-- cambia si tu usuario/macOS path es distinto

LIB_PATH  = rf"{USER}/work-lab/go/libBTransactionService.dylib"
JSON_PATH = rf"{USER}/work-lab/transaccion_example.json"
KEY_PATH  = rf"{USER}/work-lab/keys/aes256.key"
ENC_PATH  = rf"{USER}/work-lab/out/tx.enc"

# ========= SMALL HELPERS =========
def fail(msg: str):
    print(f" {msg}")
    sys.exit(1)

def ensure(path: str, kind: str):
    if not os.path.exists(path):
        fail(f"{kind} no existe: {path}")

def bind(lib, candidates, restype, argtypes):
    last = None
    for name in candidates:
        try:
            fn = getattr(lib, name)
            fn.restype = restype
            fn.argtypes = argtypes
            return fn
        except AttributeError as e:
            last = e
            continue
    raise AttributeError(f"No encontré ninguna de {candidates} en {LIB_PATH} ({last})")

# ========= PRECHECKS =========
print("🔎 Verificando rutas absolutas (macOS)…")
ensure(LIB_PATH,  "Librería nativa (.dylib)")
ensure(JSON_PATH, "JSON de entrada")
ensure(KEY_PATH,  "Llave AES")
os.makedirs(os.path.dirname(ENC_PATH), exist_ok=True)
print(" Entradas OK")

# ========= LOAD DYLIB =========
print(f"🔗 Cargando: {LIB_PATH}")
lib = ctypes.CDLL(LIB_PATH)

# ========= TYPES =========
c_void_p = ctypes.c_void_p
c_char_p = ctypes.c_char_p

# ========= ISOLATE LIFECYCLE =========
lib.graal_create_isolate.restype = ctypes.c_int
lib.graal_create_isolate.argtypes = [c_void_p, ctypes.POINTER(c_void_p), ctypes.POINTER(c_void_p)]

try:
    lib.graal_tear_down_isolate.restype = None
    lib.graal_tear_down_isolate.argtypes = [c_void_p]
except Exception:
    pass

isolate = c_void_p()
thread = c_void_p()
rc = lib.graal_create_isolate(None, ctypes.byref(isolate), ctypes.byref(thread))
if rc != 0:
    fail(f"No pude crear isolate de GraalVM (rc={rc})")
print(" GraalVM isolate creado (macOS)")

# ========= BIND EXPORTED FUNCTIONS =========
get_tx = bind(
    lib,
    ["getSecuredTransaction", "IsolateEnterStub__BTransactionService__getSecuredTransaction"],
    c_char_p,
    [c_void_p, c_char_p],
)

secure_write = bind(
    lib,
    ["secureAndWriteTransaction", "IsolateEnterStub__BTransactionService__secureAndWriteTransaction"],
    c_char_p,
    [c_void_p, c_char_p, c_char_p, c_char_p],
)

decrypt_file = bind(
    lib,
    ["decryptTransactionFile", "IsolateEnterStub__BTransactionService__decryptTransactionFile"],
    c_char_p,
    [c_void_p, c_char_p, c_char_p],
)

# ========= RUN CALLS =========
try:
    print("\n[1] getSecuredTransaction")
    r1 = get_tx(thread, JSON_PATH.encode("utf-8"))
    print(r1.decode("utf-8"))

    print("\n[2] secureAndWriteTransaction")
    r2 = secure_write(thread, JSON_PATH.encode("utf-8"), KEY_PATH.encode("utf-8"), ENC_PATH.encode("utf-8"))
    print(r2.decode("utf-8"))

    if os.path.exists(ENC_PATH):
        print(f"Archivo cifrado: {ENC_PATH} (size={os.path.getsize(ENC_PATH)} bytes)")
    else:
        print(f" No se creó el archivo: {ENC_PATH}")

    print("\n[3] decryptTransactionFile")
    r3 = decrypt_file(thread, ENC_PATH.encode("utf-8"), KEY_PATH.encode("utf-8"))
    print(r3.decode("utf-8"))

finally:
    try:
        lib.graal_tear_down_isolate(thread)
        print("\n🔚 Isolate cerrado")
    except Exception:
        pass