#ifndef __BTRANSACTIONSERVICE_H
#define __BTRANSACTIONSERVICE_H

#include <graal_isolate.h>


#if defined(__cplusplus)
extern "C" {
#endif

char* getSecuredTransaction(graal_isolatethread_t*, char*);

#if defined(__cplusplus)
}
#endif
#endif
