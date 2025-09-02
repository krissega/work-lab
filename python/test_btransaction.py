import ctypes
import os
from pathlib import Path

HOME = str(Path.home())
DLL_PATH = f"{HOME}/work-lab/go/libBTransactionService.dylib"  # con prefijo 'lib'
JSON_PATH = f"{HOME}/work-lab/transaccion_example.json".encode()
KEY_PATH  = f"{HOME}/work-lab/keys/aes256.key".encode()
ENC_PATH  = f"{HOME}/work-lab/out/tx.enc".encode()

# Asegura carpeta de salida
os.makedirs(f"{HOME}/work-lab/out", exist_ok=True)

lib = ctypes.CDLL(DLL_PATH)

# graal isolate
lib.graal_create_isolate.restype = ctypes.c_int
lib.graal_create_isolate.argtypes = [ctypes.c_void_p,
                                     ctypes.POINTER(ctypes.c_void_p),
                                     ctypes.POINTER(ctypes.c_void_p)]
isolate = ctypes.c_void_p()
thread = ctypes.c_void_p()
rc = lib.graal_create_isolate(None, ctypes.byref(isolate), ctypes.byref(thread))
if rc != 0:
    raise RuntimeError("Failed to create GraalVM isolate on macOS")

# Signatures
lib.getSecuredTransaction.restype = ctypes.c_char_p
lib.getSecuredTransaction.argtypes = [ctypes.c_void_p, ctypes.c_char_p]

lib.secureAndWriteTransaction.restype = ctypes.c_char_p
lib.secureAndWriteTransaction.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p, ctypes.c_char_p]

lib.decryptTransactionFile.restype = ctypes.c_char_p
lib.decryptTransactionFile.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p]

print("\n[1] getSecuredTransaction")
res1 = lib.getSecuredTransaction(thread, JSON_PATH)
print(res1.decode("utf-8"))

print("\n[2] secureAndWriteTransaction")
res2 = lib.secureAndWriteTransaction(thread, JSON_PATH, KEY_PATH, ENC_PATH)
print(res2.decode("utf-8"))

enc_path_str = ENC_PATH.decode()
if os.path.exists(enc_path_str):
    print("enc exists ✓ size:", os.path.getsize(enc_path_str))
else:
    print("enc missing ✗")

print("\n[3] decryptTransactionFile")
res3 = lib.decryptTransactionFile(thread, ENC_PATH, KEY_PATH)
print(res3.decode("utf-8"))
