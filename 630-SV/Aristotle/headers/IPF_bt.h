/* Copyright (C) -- The Ohio State University */

/*
   This file is the header file used in a program instrumented
   to report branch trace information.
   December 1, 2000 -- Alexey Malishevsky -- changed IPF_BT_FUNCNAMEMAX
*/

#include <stdlib.h>    
#include <stdio.h>    
#include <string.h>   
#include <ctype.h>   

#define IPF_BT_PROGNAMEMAX 256
#define IPF_BT_FUNCNAMEMAX 20480

#ifdef IPF_bt_INIT
#define EXTERN /* */
#else
#define EXTERN extern
#endif

EXTERN char  ipf_bt_progname[IPF_BT_PROGNAMEMAX];
EXTERN char  ipf_bt_tracefile[IPF_BT_PROGNAMEMAX];
EXTERN char  ipf_bt_tracefile_initial[IPF_BT_PROGNAMEMAX];
EXTERN int   ipf_bt_numprocs;
EXTERN char  **ipf_bt_func_map; 
EXTERN unsigned char  **ipf_bt_edges; 
EXTERN int   *ipf_bt_edge_count;
EXTERN int   ipf_bt_sig_switch;

#undef    EXTERN

/* function prototypes */

void IPF_bt_setup(char *progname, int numprocs);
void IPF_bt_init(char *procname, int numbranches, int entryedge,
		  int numprocs, char *progname);
void IPF_bt_term (char *tracefile);
int IPF_bt_set_branch(char *procname, int Tbranchid, int Fbranchid,
		      int condition);
void IPF_bt_set_switch_branch(char *procname, int branchid);
void IPF_bt_sig_catcher(int);
