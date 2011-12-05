/* Copyright (C) -- The Ohio State University */

#ifndef GLOBAL_FUNCS_H
#define GLOBAL_FUNCS_H
/*---------------------------------------------------------------------------*/
/* global-funcs.h

   Contains prototype information and definitions for the interfaces to
   globally used functions.

   Rev History:         3-15-94  J Michael Smith    -    Created
*/
/*---------------------------------------------------------------------------*/

#include "version.h"

#define DB_VAR_NAME     "ARISTOTLE_DB_DIR"

char *global_get_db_dir();
char *global_add_db_prefix(char *name);
char *global_get_version();

#endif
