/* Copyright (C) -- The Ohio State University */

#ifndef DBH_TH_H
#define DBH_TH_H
/* ------------------------------------------------------------------------- */
/* dbh_th.h

   Prototypes & structure definitions for test information routines.

   Rev History:  2-20-95  G. Rothermel  -  created

*/
/* ------------------------------------------------------------------------- */

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif

typedef struct th_data {
  int     object_id;
  unsigned char *tvec;
} TH_DATA;

typedef struct dbh_th_info {
  int highest_object_number;
  int highest_test_id;
  TH_DATA *object_arr;
} DBH_TH_INFO;

#ifdef DBH_TH_MOD
int      dbh_th_trace_type;    /* 1 = statement, 2 = branch */
DB_FILE *dbh_th_begin( char *test_dir );
int      dbh_th_read( DB_FILE *th_file, char *procedure_name,
                      DBH_TH_INFO **th_info_struct );
int      dbh_th_end( DB_FILE *th_file );
DB_FILE *dbh_th_create( char *test_dir );
int      dbh_th_write( DB_FILE *th_file, char *procedure_name,
                       DBH_TH_INFO *th_info_struct );
void     dbh_th_free( DBH_TH_INFO *th_info_struct );
void     dbh_th_set( DBH_TH_INFO *th_info_struct, int object, int testid);
int      dbh_th_query( DBH_TH_INFO *th_info_struct, int object, int testid);
void     dbh_th_unset( DBH_TH_INFO *th_info_struct, int object, int testid);
void     dbh_th_clear( DBH_TH_INFO *th_info_struct );
int      dbh_th_make_th_struct( DBH_TH_INFO **th_info_struct, int maxobject, int maxtest);
void dbh_th_copy( DBH_TH_INFO *th_info_struct_1, int loc1,
                  DBH_TH_INFO *th_info_struct_2, int loc2 );
int dbh_th_equal( DBH_TH_INFO *th_info_struct_1, int loc1,
                  DBH_TH_INFO *th_info_struct_2, int loc2 );
int dbh_th_union( DBH_TH_INFO *th_info_struct_1, int loc1,
                  DBH_TH_INFO *th_info_struct_2, int loc2 );
#else
extern int      dbh_th_trace_type;    /* 1 = statement, 2 = branch */
extern DB_FILE *dbh_th_begin( char *test_dir );
extern int      dbh_th_read( DB_FILE *th_file, char *procedure_name,
                      DBH_TH_INFO **th_info_struct );
extern int      dbh_th_end( DB_FILE *th_file );
extern DB_FILE *dbh_th_create( char *test_dir );
extern int      dbh_th_write( DB_FILE *th_file, char *procedure_name,
                       DBH_TH_INFO *th_info_struct );
extern void     dbh_th_free( DBH_TH_INFO *th_info_struct );
extern void     dbh_th_set( DBH_TH_INFO *th_info_struct, int object, int testid);
extern int      dbh_th_query( DBH_TH_INFO *th_info_struct, int object, int testid);
extern void     dbh_th_unset( DBH_TH_INFO *th_info_struct, int object, int testid);
extern void     dbh_th_clear( DBH_TH_INFO *th_info_struct );
extern int      dbh_th_make_th_struct( DBH_TH_INFO **th_info_struct, int maxobject, int maxtest);
extern void dbh_th_copy( DBH_TH_INFO *th_info_struct_1, int loc1,
                  DBH_TH_INFO *th_info_struct_2, int loc2 );
extern int dbh_th_equal( DBH_TH_INFO *th_info_struct_1, int loc1,
                  DBH_TH_INFO *th_info_struct_2, int loc2 );
extern int dbh_th_union( DBH_TH_INFO *th_info_struct_1, int loc1,
                  DBH_TH_INFO *th_info_struct_2, int loc2 );
#endif

#endif
