/* Copyright (C) -- The Ohio State University */

#ifndef DBH_PDG_H
#define DBH_PDG_H
/* ------------------------------------------------------------------------- */
/* dbh_pdg.h

   Prototypes & structure definitions for pdg database routines.

   Rev History:  3-15-94  J Michael Smith  -     Created
*/
/* ------------------------------------------------------------------------- */

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif

typedef struct pdg_data {
  int     node_id;
  N_LIST  *cd_preds;
  N_LIST  *cd_succs;
  N_LIST  *dd_preds;
  N_LIST  *dd_succs; 
  N_LIST  *cf_preds;
  N_LIST  *cf_succs;
} PDG_DATA;

typedef struct dbh_pdg_info {
  int highest_node_number;
  int number_of_nodes;
  int root_node_id;
  PDG_DATA *pdg_arr;
} DBH_PDG_INFO;

#ifdef DBH_PDG_MOD
DB_FILE *dbh_pdg_begin( char *program_name );
int      dbh_pdg_read( DB_FILE *prog_file, char *procedure_name,
                      DBH_PDG_INFO **pdg_info_struct );
int      dbh_pdg_end( DB_FILE *prog_file );
DB_FILE *dbh_pdg_create( char *program_name );
int      dbh_pdg_write( DB_FILE *prog_file, char *procedure_name,
                      DBH_PDG_INFO *pdg_info_struct );
void     dbh_pdg_free( DBH_PDG_INFO *pdg_info_struct );
#else
extern DB_FILE *dbh_pdg_begin( char *program_name );
extern int      dbh_pdg_read( DB_FILE *prog_file, char *procedure_name,
                             DBH_PDG_INFO **pdg_info_struct );
extern int      dbh_pdg_end( DB_FILE *prog_file );
extern DB_FILE *dbh_pdg_create( char *program_name );
extern int      dbh_pdg_write( DB_FILE *prog_file, char *procedure_name,
                              DBH_PDG_INFO *pdg_info_struct );
extern void     dbh_pdg_free( DBH_PDG_INFO *pdg_info_struct );
#endif

#endif
