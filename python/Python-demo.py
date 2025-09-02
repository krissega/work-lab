import ctypes

# Cargar la DLL
lib = ctypes.CDLL(r"C:\work-lab\java-lib\target\BTransactionService.dll")

# Cargar la librería base de GraalVM (para isolates)
graalvm = ctypes.CDLL(r"C:\work-lab\java-lib\target\BTransactionService.dll")

# Definir tipos
lib.graal_create_isolate.restype = ctypes.c_int
lib.graal_create_isolate.argtypes = [ctypes.c_void_p,
                                     ctypes.POINTER(ctypes.c_void_p),
                                     ctypes.POINTER(ctypes.c_void_p)]

# Crear isolate
isolate = ctypes.c_void_p()
thread = ctypes.c_void_p()

res = lib.graal_create_isolate(None, ctypes.byref(isolate), ctypes.byref(thread))
if res != 0:
    raise RuntimeError("No se pudo crear isolate")

# Definir la firma de tu función exportada
lib.getSecuredTransaction.restype = ctypes.c_char_p
lib.getSecuredTransaction.argtypes = [ctypes.c_void_p, ctypes.c_char_p]



# Ruta al JSON
json_path = b"C:\work-lab\\transaccion_example.json"

# Llamar la función con isolate thread
result = lib.getSecuredTransaction(thread, json_path)

print(result.decode("utf-8"))