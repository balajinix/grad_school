/* Copyright (C) -- The Ohio State University */

#ifndef DBH_DFI_H
#define DBH_DFI_H
/* ------------------------------------------------------------------------- */
/* dbh_dfi.h

   Prototypes & structure definitions for control-flow database routines.

   Rev History:  3-15-94  J Michael Smith  -     Created
                 5-15-94  G Rothermel  -  added E_LIST information
*/
/* ------------------------------------------------------------------------- */

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif

typedef struct v_list {
   int varid;
   struct v_list *next;
} V_LIST;

typedef struct dfi_data {
  int     node_id;
  N_LIST  *cf_preds;
  N_LIST  *cf_succs;
  int     num_defs;
  V_LIST  *defs;
  int     num_uses;
  V_LIST  *uses;
} DFI_DATA;

typedef struct dbh_dfi_info {
  int highest_node_number;
  int number_of_nodes;
  int root_node_id;
  DFI_DATA *dfi_arr;
} DBH_DFI_INFO;

#ifdef DBH_DFI_MOD
DB_FILE *dbh_dfi_begin( char *program_name );
int      dbh_dfi_read( DB_FILE *prog_file, char *procedure_name,
                      DBH_DFI_INFO **dfi_info_struct );
int      dbh_dfi_end( DB_FILE *prog_file );
DB_FILE *dbh_dfi_create( char *program_name );
int      dbh_dfi_write( DB_FILE *prog_file, char *procedure_name,
                      DBH_DFI_INFO *dfi_info_struct );
void     dbh_dfi_free( DBH_DFI_INFO *dfi_info_struct );
#else
extern DB_FILE *dbh_dfi_begin( char *program_name );
extern int      dbh_dfi_read( DB_FILE *prog_file, char *procedure_name,
                             DBH_DFI_INFO **dfi_info_struct );
extern int      dbh_dfi_end( DB_FILE *prog_file );
extern DB_FILE *dbh_dfi_create( char *program_name );
extern int      dbh_dfi_write( DB_FILE *prog_file, char *procedure_name,
                              DBH_DFI_INFO *dfi_info_struct );
extern void     dbh_dfi_free( DBH_DFI_INFO *dfi_info_struct );
#endif

#endif
