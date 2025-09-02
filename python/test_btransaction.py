import ctypes
import os

# Ruta a la DLL
DLL_PATH = r"C:\work-lab\java-lib\target\BTransactionService.dll"
JSON_PATH = b"C:/work-lab/transaccion_example.json"
KEY_PATH  = b"C:/work-lab/keys/aes256.key"
ENC_PATH  = b"C:/work-lab/out/tx.enc"

# Cargar la DLL
lib = ctypes.CDLL(DLL_PATH)

# Tipos de retorno/argumentos
lib.graal_create_isolate.restype = ctypes.c_int
lib.graal_create_isolate.argtypes = [
    ctypes.c_void_p,
    ctypes.POINTER(ctypes.c_void_p),
    ctypes.POINTER(ctypes.c_void_p)
]

lib.getSecuredTransaction.restype = ctypes.c_char_p
lib.getSecuredTransaction.argtypes = [ctypes.c_void_p, ctypes.c_char_p]

lib.secureAndWriteTransaction.restype = ctypes.c_char_p
lib.secureAndWriteTransaction.argtypes = [
    ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p, ctypes.c_char_p
]

lib.decryptTransactionFile.restype = ctypes.c_char_p
lib.decryptTransactionFile.argtypes = [
    ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p
]

# Crear isolate y thread
isolate = ctypes.c_void_p()
thread = ctypes.c_void_p()
res = lib.graal_create_isolate(None, ctypes.byref(isolate), ctypes.byref(thread))
if res != 0:
    raise RuntimeError(" Failed to create GraalVM isolate")
print(" GraalVM isolate created successfully")

# --- 1. Probar getSecuredTransaction ---
print("\n[1] Testing getSecuredTransaction...")
result = lib.getSecuredTransaction(thread, JSON_PATH)
print("Result JSON:", result.decode("utf-8"))

# --- 2. Probar secureAndWriteTransaction ---
print("\n[2] Testing secureAndWriteTransaction...")
result = lib.secureAndWriteTransaction(thread, JSON_PATH, KEY_PATH, ENC_PATH)
print("Encryption status:", result.decode("utf-8"))

# Confirmar archivo .enc
if os.path.exists(ENC_PATH.decode()):
    print(f" Encrypted file created: {ENC_PATH.decode()} (size={os.path.getsize(ENC_PATH.decode())} bytes)")

# --- 3. Probar decryptTransactionFile ---
print("\n[3] Testing decryptTransactionFile...")
result = lib.decryptTransactionFile(thread, ENC_PATH, KEY_PATH)
print("Decrypted JSON:", result.decode("utf-8"))