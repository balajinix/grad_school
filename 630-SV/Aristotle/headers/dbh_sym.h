/* Copyright (C) -- The Ohio State University */

#ifndef DBH_SYM_H
#define DBH_SYM_H
/* ------------------------------------------------------------------------- */
/* dbh_sym.h

   Prototypes & structure definitions for symbol information

   Rev History:  4-4-94  J Michael Smith  -     Created
                 3-7-97  Jim Jones -- added the return node to the call site
		                      list structure
		 2-19-98  Ning Ci -- added 3 free memory routines for
		 call_sites, ai_sites, and exit_sites which are pointer to
		 LIST_HEADER struct.
		 9-1-98  Jim Jones -- extended structures for use with PAF and
		                      the pafarfe front-end
*/
/* ------------------------------------------------------------------------- */

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif
#ifndef DBH_LIST_H
#include "dbh_list.h"
#endif

/* Ning Ci -- add caution measure to prevent redefinition of 
   DBH_PROC_NAME_SIZE and DBH_VAR_NAME_SIZE */

#ifndef DBH_PROC_NAME_SIZE
#define DBH_PROC_NAME_SIZE 80
#endif 	/* #ifndef DBH_PROC_NAME_SIZE */

#ifndef DBH_VAR_NAME_SIZE
#define DBH_VAR_NAME_SIZE 80
#endif  /* #ifndef DBH_VAR_NAME_SIZE */

#define DBH_SYM_FORMAT_ARISTOTLE_CLASSIC 1
#define DBH_SYM_FORMAT_PAFARFE 2

/* THE FOLLOWING 2 STRUCTURES ARE USED FOR SYM_TABLE INFO */
typedef struct dbh_sym_dat {
    int   var_id;	/* unique id for each node */
    int  var_type;	/* signifies whether node is a formal parameter, local
			   variable */
    char  var_name[DBH_VAR_NAME_SIZE];	/* variable's name */
    int   is_var_pointer;	/* indicates if variable is a pointer */
    int   is_var_static;	/* indicates if variable is persistent */
    int   is_var_extern;	/* indicates if variable is external */
  char decl_proc_name[DBH_PROC_NAME_SIZE]; /* this field is only populated
					      when using the pafarfe front-end,
					      and even then, only when the
					      variable is not a "true" global
					      stored in a "4" record.
					      Otherwise, it is always a null
					      string */
} DBH_SYM_DAT;

/* header structure for SYM_TABLE */
typedef struct sym_head {
  int format;
  int number_of_symbols;
  DBH_SYM_DAT *sym_arr;
  int number_of_params;
  int *param_ids;        /* this field should be used as the index into the
			    sym_arr array.  In classic Aristotle format 
			    (.sym file created by cfe or gccfe), the elements
			    of this array were actually the variable ID's of
			    the parameters, however, in the .sym file created
			    by pafarfe, this could not be, because all
			    variables were considered at the global scope and
			    thus had negative variable ID's, so we used an
			    ordinary incremental numbering here e.g. 1,2,3,...
			    But, in both cases this number can be used as an
			    index into the sym_arr to find the symbol entry */
  int highest_node_id;
  int number_of_nodes;
} DBH_SYM_TABLE;


/* THE FOLLOWING STRUCTURES ARE MANIPULATED IN GENERIC LISTS */
typedef struct dbh_call_site_node {
  int  node_id;     /* node id at which call was made */
  char proc_name[DBH_PROC_NAME_SIZE];
  int  rtn_node_id; /* node id of the corresponding return node -- jim jones */
  int  number_of_parameters;
  int *parm_array;  /* size is NUMBER_OF_PARAMETERS+1 ... starts at 1 not 0 */
} DBH_CALL_NODE;

typedef struct dbh_ai_node {
  int node_id;
  int var_def_id;
  int var_use_id;  
} DBH_AI_NODE;

typedef struct dbh_exit_node {
  int node_id;
} DBH_EXIT_NODE;


#ifdef DBH_SYM_MOD
DB_FILE *dbh_sym_begin( char *program_name, DBH_SYM_TABLE **glob_info_struct );
int      dbh_sym_read( DB_FILE *prog_file, char *procedure_name,
                      DBH_SYM_TABLE **sym_info_struct,
		      LIST_HEADER **call_sites, 
		      LIST_HEADER **ai_site, 
		      LIST_HEADER **exit_site );
int      dbh_sym_end( DB_FILE *prog_file );
DB_FILE *dbh_sym_create( char *program_name );
DB_FILE *dbh_sym_create_ar_classic( char *program_name );
DB_FILE *dbh_sym_create_pafarfe( char *program_name );
int      dbh_sym_write( DB_FILE *prog_file, char *procedure_name,
                      DBH_SYM_TABLE *sym_info_struct,
		      LIST_HEADER *call_sites, 
		      LIST_HEADER *ai_site, 
		      LIST_HEADER *exit_site );
int      dbh_sym_flush_glob( DB_FILE *prog_file,
			    DBH_SYM_TABLE *global_info_struct );
void     dbh_sym_free_symb( DBH_SYM_TABLE *sym_info_struct );
void     dbh_sym_free_glob( DBH_SYM_TABLE *glob_info_struct );
void 	 dbh_sym_free_call_sites(LIST_HEADER * call_sites);
void 	 dbh_sym_free_ai_site(LIST_HEADER * ai_site);
void 	 dbh_sym_free_exit_site(LIST_HEADER * exit_site);
#else
extern DB_FILE *dbh_sym_begin( char *program_name, DBH_SYM_TABLE **glob_info_struct );
extern int      dbh_sym_read( DB_FILE *prog_file, char *procedure_name,
                      DBH_SYM_TABLE **sym_info_struct,
		      LIST_HEADER **call_sites, 
		      LIST_HEADER **ai_site, 
		      LIST_HEADER **exit_site );
extern int      dbh_sym_end( DB_FILE *prog_file );
extern DB_FILE *dbh_sym_create( char *program_name );
extern DB_FILE *dbh_sym_create_ar_classic( char *program_name );
extern DB_FILE *dbh_sym_create_pafarfe( char *program_name );
extern int      dbh_sym_write( DB_FILE *prog_file, char *procedure_name,
                      DBH_SYM_TABLE *sym_info_struct,
		      LIST_HEADER *call_sites, 
		      LIST_HEADER *ai_site, 
		      LIST_HEADER *exit_site );
extern int      dbh_sym_flush_glob( DB_FILE *prog_file,
				   DBH_SYM_TABLE *global_info_struct );
extern void     dbh_sym_free_symb( DBH_SYM_TABLE *sym_info_struct );
extern void     dbh_sym_free_glob( DBH_SYM_TABLE *glob_info_struct );
extern void   	dbh_sym_free_call_sites(LIST_HEADER * call_sites);
extern void 	 dbh_sym_free_ai_site(LIST_HEADER * ai_site);
extern void 	 dbh_sym_free_exit_site(LIST_HEADER * exit_site);
#endif /* #ifdef DBH_SYM_MOD */

#endif /* #ifndef DBH_SYM_H */

