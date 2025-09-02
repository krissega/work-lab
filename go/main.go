package main

/*
#cgo darwin LDFLAGS: -L/Users/krisa/work-lab/go -lBTransactionService
#cgo darwin CFLAGS:  -I/Users/krisa/work-lab/go

#include <stdlib.h>
#include "graal_isolate.h"
#include "BTransactionService.h"
*/
import "C"

import (
	"fmt"
	"os"
	"path/filepath"
	"unsafe"
)

func main() {
	home, _ := os.UserHomeDir()

	jsonPath := filepath.Join(home, "work-lab", "transaccion_example.json")
	keyPath  := filepath.Join(home, "work-lab", "keys", "aes256.key")
	encPath  := filepath.Join(home, "work-lab", "out", "tx.enc")

	_ = os.MkdirAll(filepath.Dir(encPath), 0o755)

	// Create isolate
	var isolate *C.graal_isolate_t
	var thread  *C.graal_isolatethread_t
	if rc := C.graal_create_isolate(nil, &isolate, &thread); rc != 0 {
		panic("Failed to create GraalVM isolate (macOS)")
	}
	fmt.Println("GraalVM isolate created (macOS)")

	// Convert strings
	cJSON := C.CString(jsonPath)
	cKey  := C.CString(keyPath)
	cEnc  := C.CString(encPath)
	defer C.free(unsafe.Pointer(cJSON))
	defer C.free(unsafe.Pointer(cKey))
	defer C.free(unsafe.Pointer(cEnc))

	// 1) getSecuredTransaction
	fmt.Println("\n[1] getSecuredTransaction")
	res1 := C.getSecuredTransaction(thread, cJSON)
	fmt.Println(C.GoString(res1))

	// 2) secureAndWriteTransaction
	fmt.Println("\n[2] secureAndWriteTransaction")
	res2 := C.secureAndWriteTransaction(thread, cJSON, cKey, cEnc)
	fmt.Println(C.GoString(res2))

	if fi, err := os.Stat(encPath); err == nil {
		fmt.Printf("Encrypted file: %s (size=%d bytes)\n", encPath, fi.Size())
	} else {
		fmt.Println("Encrypted file not found:", err)
	}

	// 3) decryptTransactionFile
	fmt.Println("\n[3] decryptTransactionFile")
	res3 := C.decryptTransactionFile(thread, cEnc, cKey)
	fmt.Println(C.GoString(res3))

	// Tear down
	C.graal_tear_down_isolate(thread)
}