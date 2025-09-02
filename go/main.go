package main

/*
#cgo LDFLAGS: -LC:/work-lab/go -lBTransactionService
#cgo CFLAGS: -IC:/work-lab/go

#include <stdlib.h>
#include "graal_isolate.h"
#include "BTransactionService.h"
*/
import "C"
import (
	"fmt"
	"unsafe"
)

func main() {
	// Step 1: Create GraalVM isolate (mini-VM) and thread
	var isolate *C.graal_isolate_t
	var thread *C.graal_isolatethread_t

	res := C.graal_create_isolate(nil, &isolate, &thread)
	if res != 0 {
		panic("Failed to create GraalVM isolate")
	}
	fmt.Println("GraalVM isolate created successfully")

	// Step 2: JSON path
	jsonPath := "C:/work-lab/transaccion_example.json"

	cPath := C.CString(jsonPath)
	defer C.free(unsafe.Pointer(cPath))

	// Step 3: Call exported DLL function
	result := C.getSecuredTransaction(thread, cPath)

	// Step 4: Convert C string -> Go string
	goResult := C.GoString(result)

	// Step 5: Print response
	fmt.Println("Response from DLL:")
	fmt.Println(goResult)

	// Step 6: Clean up isolate
	C.graal_tear_down_isolate(thread)
}
