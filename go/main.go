package main

/*
#cgo LDFLAGS: -LC:/work-lab/go -lBTransactionService
#cgo CFLAGS:  -IC:/work-lab/go

#include <stdlib.h>
#include "graal_isolate.h"
#include "BTransactionService.h"
*/
import "C"

import (
	"fmt"
	"os"
	"unsafe"
)

func main() {
	// --- Step 0: paths (adjust if needed) ---
	jsonPath := "C:/work-lab/transaccion_example.json"
	keyPath  := "C:/work-lab/keys/aes256.key"
	encPath  := "C:/work-lab/out/tx.enc"

	// Ensure output dir exists (like Python did)
	_ = os.MkdirAll("C:/work-lab/out", 0o755)// 0o prefix for  octal base and 755 file permissions :)

	// --- Step 1: create isolate/thread (required by @CEntryPoint) ---
	var isolate *C.graal_isolate_t
	var thread  *C.graal_isolatethread_t
	if rc := C.graal_create_isolate(nil, &isolate, &thread); rc != 0 {
		panic("Failed to create GraalVM isolate")
	}
	fmt.Println(" GraalVM isolate created")

	// Convert Go strings -> C strings
	cJSON := C.CString(jsonPath)
	cKey  := C.CString(keyPath)
	cEnc  := C.CString(encPath)
	defer C.free(unsafe.Pointer(cJSON))
	defer C.free(unsafe.Pointer(cKey))
	defer C.free(unsafe.Pointer(cEnc))

	// --- Step 2: getSecuredTransaction(JSON) -> JSON string ---
	fmt.Println("\n[1] getSecuredTransaction")
	res1 := C.getSecuredTransaction(thread, cJSON)
	fmt.Println(C.GoString(res1))

	// --- Step 3: secureAndWriteTransaction(JSON, KEY, OUT) -> status JSON ---
	fmt.Println("\n[2] secureAndWriteTransaction")
	res2 := C.secureAndWriteTransaction(thread, cJSON, cKey, cEnc)
	fmt.Println(C.GoString(res2))

	// Check encrypted file
	if fi, err := os.Stat(encPath); err == nil {
		fmt.Printf("Encrypted file: %s (size=%d bytes)\n", encPath, fi.Size())
	} else {
		fmt.Println("Encrypted file not found:", err)
	}

	// --- Step 4: decryptTransactionFile(OUT, KEY) -> original JSON ---
	fmt.Println("\n[3] decryptTransactionFile")
	res3 := C.decryptTransactionFile(thread, cEnc, cKey)
	fmt.Println(C.GoString(res3))

	// --- Step 5: tear down isolate (good practice) ---
	C.graal_tear_down_isolate(thread)
}