#!/bin/csh -f
# 
#  This script imports the raw data to the speciation tool database
#
#  User must first:
#      SPTOOL_SRC_HOME - set to the Speciation Tool source code directory
#      SPTOOL_DB - set to the database name for this installation
#============================================================================================
#  v3.0
#  01Jun2011
#============================================================================================

set script_name = sptool_load_shared.csh
set exitstatus = 0

# Ensure that required environment variables are set
if ( $?SPTOOL_SRC_HOME ) then
    echo "SPTOOL_SRC_HOME = $SPTOOL_SRC_HOME"
else
    set exitstatus = 1
    echo "SCRIPT ERROR: Required environment variable SPTOOL_SRC_HOME not set"
    echo "              in script $script_name"
endif

if ( $?SPTOOL_DB ) then
    echo "SPTOOL_DB      = $SPTOOL_DB"
else
    set exitstatus = 1
    echo "SCRIPT ERROR: Required environment variable SPTOOL_DB not set"
    echo "              in script $script_name"
endif

if ( $exitstatus != 0 ) then
    ## log w/ EMF server that script is running
    $EMF_CLIENT -k $EMF_JOBKEY -m "ABORT: $script_name script aborting with errors" -t "e"
    echo "ABORT: $script_name script aborting with errors"
    exit ( 1 )
endif

date

# Reinitialize EMF sptool 'run' tables
echo "Dropping and reinitializing run-specific tables in $SPTOOL_DB database"
$EMF_CLIENT -k $EMF_JOBKEY -m "Dropping and reinitializing run-specific tables in $SPTOOL_DB database"  ## log w/ EMF server
$POSTGRES_BIN/psql -U sptool -v ON_ERROR_STOP=1 -t -f $SPTOOL_SRC_HOME/table_defs.sql $SPTOOL_DB
if ( $status != 0 ) then
    echo "ERROR: psql script failed to drop and reinitialize run-specific tables"
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: psql script failed to drop and reinitialize run-specific tables" -t "e"
    exit ( 1 )
endif
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing run data into database: $SPTOOL_DB"  ## log w/ EMF server


set dataset = mechanisms
echo "Importing $dataset"
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing $dataset"  ## log w/ EMF server

# argument list is <database name> <table name> <inputfile>
perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB mechanism $MECHANISM
if ( $status != 0 ) then
    echo "ERROR: perl script failed for importing $dataset"
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing $dataset" -t "e"
    exit ( 1 )
endif
# *******************************

set dataset = (PM mechanisms)
echo "Importing $dataset"
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing $dataset"  ## log w/ EMF server
perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB mechanismPM $MECHANISMPM
if ( $status != 0 ) then
    echo "ERROR: perl script failed for importing $dataset"
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing $dataset" -t "e"
    exit ( 1 )
endif
# *******************************

set dataset = (mechanism descriptions)
echo "Importing $dataset"
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing $dataset"  ## log w/ EMF server
perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB mechanism_description $MECHANISM_DESCRIPTION
if ( $status != 0 ) then
    echo "ERROR: perl script failed for importing $dataset"
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing $dataset" -t "e"
    exit ( 1 )
endif
# *******************************

set dataset = (inventory table)
echo "Inventory dataset = $INVTABLE"
echo "Importing $dataset"
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing $dataset"  ## log w/ EMF server
perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB invtable $INVTABLE
if ( $status != 0 ) then
    echo "ERROR: perl script failed for importing $dataset"
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing $dataset" -t "e"
    exit ( 1 )
endif
# *******************************

set dataset = (carbons table)
echo "Importing $dataset"
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing $dataset"  ## log w/ EMF server
perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB carbons $CARBONS
if ( $status != 0 ) then
    echo "ERROR: perl script failed for importing $dataset"
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing $dataset" -t "e"
    exit ( 1 )
endif
# *******************************

set dataset = (static profiles)
## only import if static E.V. is set
if ( $?PROFILES_STATIC ) then
    echo "Importing $dataset"
    $EMF_CLIENT -k $EMF_JOBKEY -m "Importing $dataset"  ## log w/ EMF server
    perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB static $PROFILES_STATIC
    if ( $status != 0 ) then
	echo "ERROR: perl script failed for importing $dataset"
	$EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing $dataset" -t "e"
	exit ( 1 )
    endif
else
    echo "Not importing $dataset"
endif
# *******************************

# Create output directory for the next step
if ( ! -e $EMF_SCRIPTDIR/out ) then
    mkdir $EMF_SCRIPTDIR/out
    chmod ugo+rwx $EMF_SCRIPTDIR/out
endif

date

exit( 0 )

