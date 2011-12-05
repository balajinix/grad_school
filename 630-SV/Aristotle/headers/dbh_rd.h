/* Copyright (C) -- The Ohio State University */

#ifndef DBH_RD_H
#define DBH_RD_H
/* ------------------------------------------------------------------------- */
/* dbh_rd.h

   Prototypes & structure defs for reaching definitions database routines.

   Rev History:  5-31-94  David A. Nedved        -     Created

*/
/* ------------------------------------------------------------------------- */

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif

typedef struct rd_data 
  {
  int     node_id;

  int     num_defs;
  int    *node_id_arr;
  int    *var_id_arr;
  } DBH_RD_DATA;

typedef struct dbh_rd_info 
  {
  int highest_node_number;
  int number_of_nodes;
  DBH_RD_DATA *rd_dat;
  } DBH_RD_INFO;


#ifdef DBH_RD_MOD
DB_FILE *dbh_rd_begin( char *program_name );
int      dbh_rd_read( DB_FILE *prog_file, char *procedure_name,
                      DBH_RD_INFO **rd_info_struct );
int      dbh_rd_end( DB_FILE *prog_file );
DB_FILE *dbh_rd_create( char *program_name );
int      dbh_rd_write( DB_FILE *prog_file, char *procedure_name,
                      DBH_RD_INFO *rd_info_struct );
void     dbh_rd_free( DBH_RD_INFO *rd_info_struct );
#else
extern DB_FILE *dbh_rd_begin( char *program_name );
extern int      dbh_rd_read( DB_FILE *prog_file, char *procedure_name,
                             DBH_RD_INFO **rd_info_struct );
extern int      dbh_rd_end( DB_FILE *prog_file );
extern DB_FILE *dbh_rd_create( char *program_name );
extern int      dbh_rd_write( DB_FILE *prog_file, char *procedure_name,
                              DBH_RD_INFO *rd_info_struct );
extern void     dbh_rd_free( DBH_RD_INFO *rd_info_struct );
#endif

#endif
