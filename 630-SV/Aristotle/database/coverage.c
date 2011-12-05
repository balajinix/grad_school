
/*------------------------------------------------------------------------*/
/*  coverage.c

    Reports branches covered and need covering

*/
/*------------------------------------------------------------------------*/
/*  Include files:   */ 

#include <stdio.h>
#include <string.h>
#include "dbh_th.h"
#include "dbh_map.h"
#include "dbh_cf.h"

/*------------------------------------------------------------------------*/
/* begin function */

int main(int argc, char *argv[])

/*------------------------------------------------------------------------*/
/*  Description:

    Parameters:
       Takes input arguments.
          1 - (optional) the name of the test info file from which we're 
              printing the th info; if omitted we prompt for one
          2 - (optional) the name of the program whose test file this is;
              if omitted we prompt
          3 - (optional) flag "ALL" or "UNCOV"; if omitted, default UNCOV

    Return Value:
	none

    Global variables used:
	none

    Revision History: 2-15-95  --   Created

*/
/*------------------------------------------------------------------------*/

{
      DB_FILE *th_in;            /* DB_FILE for th file                 */
      DB_FILE *map_in;            /* DB_FILE for map file                 */
      DB_FILE *cf_in;            /* DB_FILE for cf file                 */
      FILE    *th_out;           /* FILE for output file                 */
      DBH_TH_INFO *th_info;      /* pointer to th info record            */
      DBH_MAP_INFO *map_info;    /* map info on file                     */
      DBH_CF_INFO *cf_info;    /* cf info on file                     */
      DBH_PROC_NAMES *proc_list; /* list of procedures in file           */
      char thfile[256],         /* filename of the input file           */
           progname[256],        /* names prog whose tests we're into    */
           outfilename[256];     /* filename of the output file          */
      int lcv;                   /* generic lcv for loops                */
      int all=0;
      int banner=0;

      if (argc < 1 || argc > 4)
      {
          printf("USAGE: coverage <test_history_file> <prog_name> [ALL|UNCOV] \n");
          printf("\n");
          printf("   <test_history_file> - full pathname of test history file\n");
          printf("   <prog_name>         - name of source file with .c \n");
          printf("   ALL|UNCOV           - whether to report all or just those uncovered\n");
          exit(1);
      }
     
      /* if no args are given, prompt for test file name */
      if (argc < 2)
      {
           printf("Enter test history file name: ");
           scanf("%s",thfile);
      }
      else  /* program name is first argument */
      {
           strcpy(thfile,argv[1]);
      }

      /* if no args are given, prompt for program name */
      if (argc < 3)
      {
           printf("Enter name of tested program: ");
           scanf("%s",progname);
      }
      else  /* program name is second argument */
      {
           strcpy(progname,argv[2]);
      }

      /* all or uncov? */
      if (argc == 4)
      {
         if (strcmp(argv[3],"ALL")==0)
            all = 1;
      }

      th_out = stdout;

      /* open the input test info file */
      th_in = dbh_th_begin(thfile);
      if(th_in == NULL)
      {
          fprintf(stderr,"Error opening test history file... \n\n");
          exit(1);
      }

      /* try to open the map file for the program */
      map_in = dbh_map_begin(progname);
      if(map_in == NULL)
      {
          fprintf(stderr,"Error opening map file... \n\n");
          exit(1);
      }

      /* try to open the cf file for the program */
      cf_in = dbh_cf_begin(progname);
      if(cf_in == NULL)
      {
          fprintf(stderr,"Error opening cf file... \n\n");
          exit(1);
      }


      /* get the proc list from the input file */
      proc_list = dbh_com_proc_list(th_in);

      /* loop to go through the info for each procedure  */
      for(lcv=1;lcv<=proc_list->number_of_names;lcv++)
      {

          /* read test info for this procedure */
          if(dbh_th_read(th_in,proc_list->name_array[lcv],&th_info) != 1)
          {
	        fprintf(stderr,"Error reading test history file... \n\n");
                dbh_th_end(th_in);
                dbh_map_end(map_in);
                dbh_cf_end(cf_in);
	        exit(1);
          }

          if (dbh_map_read(map_in,proc_list->name_array[lcv],&map_info)!=1)
          {
                fprintf(stderr,"Error reading map file... \n\n");
                dbh_map_end(map_in);
                dbh_cf_end(cf_in);
                dbh_th_end(th_in);
                exit(1);
          }

          if (dbh_cf_read(cf_in,proc_list->name_array[lcv],&cf_info)!=1)
          {
                fprintf(stderr,"Error reading cf file... \n\n");
                dbh_map_end(map_in);
                dbh_cf_end(cf_in);
                dbh_th_end(th_in);
                exit(1);
          }

          /* write it out in a nice format */
       
          if (!banner)
          {
              fprintf(th_out,"\nTest history information for tests in file %s\n", 
                      thfile); 
              if (dbh_th_trace_type==1)
                 fprintf(th_out,"Trace type: statement\n");
              else if (dbh_th_trace_type==2)
                 fprintf(th_out,"Trace type: branch\n");
              else if (dbh_th_trace_type==3)
                 fprintf(th_out,"Trace type: edge\n");
              else 
                 fprintf(th_out,"Trace type: UNIDENTIFIED\n");
              banner = !banner;
          }

             if(printinfo(th_out,proc_list->name_array[lcv],
                th_info,map_info,cf_info,all) != 1)
             {
                  fprintf(stderr,"Error in call to printinfo... \n\n");
                  dbh_th_end(th_in);
                  dbh_map_end(map_in);
                  dbh_cf_end(cf_in);
                  exit(1);
             }

          /* free the dynamically allocated spaces */
          dbh_map_free(map_info);
          dbh_cf_free(cf_info);
          dbh_th_free(th_info);
      }

      /* close files */
      dbh_th_end(th_in);
      dbh_map_end(map_in);
      dbh_cf_end(cf_in);
      exit(0);

}


/* ---------------------------------------------------------------------- */
/* begin function */

int printinfo( FILE *th_file,
                     char *procedure_name,
                     DBH_TH_INFO *th_info_struct,
                     DBH_MAP_INFO *map_info_struct,
                     DBH_CF_INFO *cf_info_struct,
                     int printall
                    )

/* ---------------------------------------------------------------------- */
/* Description:

         Write out the th information for procedure PROCEDURE_NAME
         from TH_INFO_STRUCT in human-readable format.

   Parameters:
         In:       th_file - pointer to file to write to, might be stdout

                   procedure_name - name of procedure whose th information
                                    we're going to write

                   th_info_struct - structure with th info to write.

                   map_info_struct - structure with map info 

                   cf_info_struct - structure with cf info 


   Return value: returns 1 on success, 0 on failure

   Globals used: none

   Revision History: 2-15-94     -  Created

*/
/* ---------------------------------------------------------------------- */

{

  int COLMAX = 80;
  char str[80];
  int i,j,tcount,pnode,snum;
  DBH_CF_EDGE_INFO *edge_list;


  /* write procedure header */

  fprintf( th_file, "\n\n");
  fprintf( th_file, "Function: %s    \n\n", procedure_name );
  fprintf( th_file, " edgeid  coverage status   sourceline,edgelabel \n");
  fprintf( th_file, "---------------------------------------------------\n");

  /* write out th info */

  for (j=1;j<=th_info_struct->highest_object_number;j++)
  {

      /* if dealing with stmt traces, avoid printing info on nodes that
         aren't really stmt nodes in the graph */
      if ( dbh_th_trace_type == 1)
      {
         if ((dbh_map_stmt_node(map_info_struct,j))==0)
            continue;
      }

      /* if dealing with branch traces, print info only for entry node
         and edges that originate at predicate nodes */
      if ( dbh_th_trace_type == 2)
      {
          if ( dbh_cf_get_edge_list(cf_info_struct,&edge_list) != 1 )
          {
              printf("\nUnable to read cf edge list\n");
              return 0;
          }

          pnode = edge_list[j].pred_node;

          if ( (j!=1) && !(dbh_map_pred_node(map_info_struct,pnode)) )
              continue;

          /* here, get statement number and edge label */
          /* can't just get number on current node, it might
             be a pnode and you'd need the cnode i think? */

          snum = map_info_struct->map_dat[pnode].end_stmt_number;
          if (snum == 0)
              snum = map_info_struct->map_dat[pnode-1].end_stmt_number;
 
      }

      tcount = 0;
      for (i=0;i<=th_info_struct->highest_test_id;i++)
         if (dbh_th_query(th_info_struct,j,i))
            tcount++;

      if (tcount == 0)
          fprintf( th_file, "%6d        NOT HIT          %6d,%s\n",
               th_info_struct->object_arr[j].object_id,snum,edge_list[j].edge_label);
      if (printall==1 && tcount > 0)
          fprintf( th_file, "%6d        HIT              %6d,%s\n",
               th_info_struct->object_arr[j].object_id,snum,edge_list[j].edge_label);

  }

  return 1;
}


