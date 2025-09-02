#ifndef __BTRANSACTIONSERVICE_H
#define __BTRANSACTIONSERVICE_H

#include <graal_isolate_dynamic.h>


#if defined(__cplusplus)
extern "C" {
#endif

typedef char* (*getSecuredTransaction_fn_t)(graal_isolatethread_t*, char*);

#if defined(__cplusplus)
}
#endif
#endif
