#ifndef __BTRANSACTIONSERVICE_H
#define __BTRANSACTIONSERVICE_H

#include <graal_isolate.h>


#if defined(__cplusplus)
extern "C" {
#endif

char* getSecuredTransaction(graal_isolatethread_t*, char*);

char* secureAndWriteTransaction(graal_isolatethread_t*, char*, char*, char*);

char* decryptTransactionFile(graal_isolatethread_t*, char*, char*);

#if defined(__cplusplus)
}
#endif
#endif
