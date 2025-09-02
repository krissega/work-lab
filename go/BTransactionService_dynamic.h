#ifndef __BTRANSACTIONSERVICE_H
#define __BTRANSACTIONSERVICE_H

#include <graal_isolate_dynamic.h>


#if defined(__cplusplus)
extern "C" {
#endif

typedef char* (*getSecuredTransaction_fn_t)(graal_isolatethread_t*, char*);

typedef char* (*secureAndWriteTransaction_fn_t)(graal_isolatethread_t*, char*, char*, char*);

typedef char* (*decryptTransactionFile_fn_t)(graal_isolatethread_t*, char*, char*);

#if defined(__cplusplus)
}
#endif
#endif
