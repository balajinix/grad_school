/* Copyright (C) -- The Ohio State University */

#ifndef DBH_CD_H
#define DBH_CD_H
/* ------------------------------------------------------------------------- */
/* dbh_cd.h

   Prototypes & structure definitions for control-flow database routines.

   Rev History:  3-15-94  J Michael Smith  -     Created
                 4-30-98  S. Sinha         - added dbh_cd_get_intra_cd()
*/
/* ------------------------------------------------------------------------- */

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif
#ifndef DBH_MAP_H
#include "dbh_map.h"
#endif

typedef struct cd_data {
  int     node_id;
  N_LIST  *cd_preds;
  N_LIST  *cd_succs;
} CD_DATA;

typedef struct dbh_cd_info {
  int highest_node_number;
  int number_of_nodes;
  int root_node_id;
  CD_DATA *cd_arr;
} DBH_CD_INFO;

#ifdef DBH_CD_MOD
DB_FILE *dbh_cd_begin( char *program_name );
int      dbh_cd_read( DB_FILE *prog_file, char *procedure_name,
                      DBH_CD_INFO **cd_info_struct );
int      dbh_cd_end( DB_FILE *prog_file );
DB_FILE *dbh_cd_create( char *program_name );
int      dbh_cd_write( DB_FILE *prog_file, char *procedure_name,
                      DBH_CD_INFO *cd_info_struct );
void     dbh_cd_free( DBH_CD_INFO *cd_info_struct );
void     dbh_cd_get_intra_cd( DBH_CD_INFO *cd_info_struct,
                              DBH_MAP_INFO *map_info_struct, int node_id,
                              N_LIST **intra_cd );
#else
extern DB_FILE *dbh_cd_begin( char *program_name );
extern int      dbh_cd_read( DB_FILE *prog_file, char *procedure_name,
                             DBH_CD_INFO **cd_info_struct );
extern int      dbh_cd_end( DB_FILE *prog_file );
extern DB_FILE *dbh_cd_create( char *program_name );
extern int      dbh_cd_write( DB_FILE *prog_file, char *procedure_name,
                              DBH_CD_INFO *cd_info_struct );
extern void     dbh_cd_free( DBH_CD_INFO *cd_info_struct );
extern void     dbh_cd_get_intra_cd( DBH_CD_INFO *cd_info_struct,
                                     DBH_MAP_INFO *map_info_struct, int node_id,
                                     N_LIST **intra_cd );
#endif

#endif
