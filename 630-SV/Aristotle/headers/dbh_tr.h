/* Copyright (C) -- The Ohio State University */

#ifndef DBH_TR_H
#define DBH_TR_H
/* ------------------------------------------------------------------------- */
/* dbh_tr.h

   Prototypes & structure definitions for trace database routines.

   Rev History:  11-10-96  G. Rothermel  -  created

*/
/* ------------------------------------------------------------------------- */

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif

typedef struct dbh_tr_info {
  int highestbit;
  unsigned char *tvec;
} DBH_TR_INFO;

#ifdef DBH_TR_MOD
int dbh_tr_numprocs;
int dbh_tr_type;
DB_FILE *dbh_tr_begin( char *tracefile );
int      dbh_tr_read( DB_FILE *trace_file, char *procedure_name,
                      DBH_TR_INFO **tr_info_struct );
int      dbh_tr_end( DB_FILE *trace_file );
void     dbh_tr_free( DBH_TR_INFO *tr_info_struct );
void     dbh_tr_set( DBH_TR_INFO *tr_info_struct, int node);
int      dbh_tr_query( DBH_TR_INFO *tr_info_struct, int node);
void     dbh_tr_unset( DBH_TR_INFO *tr_info_struct, int node);
void     dbh_tr_clear( DBH_TR_INFO *tr_info_struct );
int      dbh_tr_make_tr_struct( DBH_TR_INFO **tr_info_struct, int highestbit);
#else
extern int dbh_tr_numprocs;
extern int dbh_tr_type;
extern DB_FILE *dbh_tr_begin( char *tracefile );
extern int      dbh_tr_read( DB_FILE *trace_file, char *procedure_name,
                      DBH_TR_INFO **tr_info_struct );
extern int      dbh_tr_end( DB_FILE *trace_file );
extern void     dbh_tr_free( DBH_TR_INFO *tr_info_struct );
extern void     dbh_tr_set( DBH_TR_INFO *tr_info_struct, int node);
extern int      dbh_tr_query( DBH_TR_INFO *tr_info_struct, int node);
extern void     dbh_tr_unset( DBH_TR_INFO *tr_info_struct, int node );
extern void     dbh_tr_clear( DBH_TR_INFO *tr_info_struct );
extern int      dbh_tr_make_trstruct( DBH_TR_INFO **tr_info_struct, int highestbit);
#endif
#endif
