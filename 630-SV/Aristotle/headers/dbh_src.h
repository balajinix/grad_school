/* Copyright (C) -- The Ohio State University */

/*------------------------------------------------------------------------*/
/*  dbh_src.h

    Prototypes and structure definitions for source handler

    Revision History:
        6-30-94     David A. Nedved       -- Created

*/
/*------------------------------------------------------------------------*/

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif

typedef struct
    {
    int number_of_lines;
    char **src_arr;
    } DBH_SRC_INFO;

#ifdef DBH_SRC_MOD
DB_FILE *dbh_src_begin (char *program_name);
int dbh_src_read(DB_FILE *prog_file, DBH_SRC_INFO **src_info_struct);
int dbh_src_end(DB_FILE *prog_file);
void dbh_src_free (DBH_SRC_INFO *src_info_struct);
#else
extern DB_FILE *dbh_src_begin (char *program_name);
extern int dbh_src_read(DB_FILE *prog_file, DBH_SRC_INFO **src_info_struct);
extern int dbh_src_end(DB_FILE *prog_file);
extern void dbh_src_free (DBH_SRC_INFO *src_info_struct);
#endif





