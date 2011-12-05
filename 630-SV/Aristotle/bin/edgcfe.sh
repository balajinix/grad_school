#!/usr/local/bin/perl

# Copyright (C) -- The Ohio State University

#---------------------------------------------------------------------------
#  cfe
#
#  Script used to call the EDG front-end.  Enables user to specify multiple
#  C source files for the EDG front-end to parse.  Stores all intermediate
#  language files in the Aristotle database directory.  Supplies command
#  line options to the EDG front-end.  Enables user to specify a source file
#  for EDG to parse that is not in the current directory.
#
#  Revision History:
#     02.03.98  Jim Jones              -- Created
#
#  Note:
#     On certain platforms, certain define flags must be set.  These cases,
#     when discovered, should be added to this script.  For example, note
#     how the "-D__hp9000s300" flag is set for HP-UX.
#

require "pwd.pl";

# get the host type environment variable
$HOSTTYPE = $ENV{'HOSTTYPE'};
if ($HOSTTYPE eq "hp700") { $defines = "-D__hp9000s300"; }
elsif ($HOSTTYPE eq "sun4") { $defines = "-Dsparc -D__EXTENSIONS__"; }
# add additional cases here in the form:
# elsif ($HOSTTYPE eq ...

# initialize the PWD environment variable to remember where this script was
# called from
&initpwd;
$rootdir = $ENV{'PWD'};

# walk through command line arguments to find any flags for the compiler FE
foreach $i (0 .. $#ARGV) {
	    
    # get the i'th argument
    $_ = $ARGV[$i];

    # find if first character is a minus symbol
    /^(.)(.*)/;
    if ( $1 eq "-" ) {
	$defines = $defines . " " . $ARGV[$i];
    }

}

# for each command line argument...
foreach $i (0 .. $#ARGV) {
	    
    # get the i'th argument
    $_ = $ARGV[$i];

    # if its a compiler flag, skip it
    /^(.)(.*)/;
    if ( $1 ne "-" ) {

	# get the i'th argument
	$_ = $ARGV[$i];

	# parse the argument to separate the path from the file name
	/^(.*\/)*(.*)$/;

	# change to the directory where the source file resides (see note 1 below)
	if ($1 ne "") {
	    &chdir("$1") || print STDERR "cfe: Can't access directory $1\n";
	}

	# remove any existing IL and source files with the same file name 
	`rm -f $ENV{'ARISTOTLE_DB_DIR'}/$2*`;

	# call EDG front-end
	$retval = system( "edgcfe --c --no_remove_unneeded_entities -I. $defines -o$ENV{'ARISTOTLE_DB_DIR'}/$2il $2" );

	# retval of 65280 denotes that the executable was not found
	if ( $retval == 65280 ) {
	    print STDERR "edgcfe.sh: EDG executable directory must be in PATH\n",
		"edgcfe.sh: cannot find edgcfe\n";
	}

	# if there was an error retval (value other than 0), exit
	if ( $retval ) {
	    print STDERR "edgcfe.sh: error parsing source file \"$ARGV[$i]\"... exiting.\n";
	    exit 2;
	}

	# copy source file to database directory
	`cp $2 $ENV{'ARISTOTLE_DB_DIR'}/$2`;

	# change back to the directory where the script was called from so that 
	# subsequent chdir's won't fail
	&chdir("$rootdir");
    }

}


# (1) We must change to the directory where the source resides and call EDG
#     there because EDG stores the entire source file-name argument in the IL.
#     So, if you were in the samples directory and called 
#     edgcpfe schedule2/schedule2.c, the source file-name stored in the IL
#     would be schedule2/schedule2.c.










