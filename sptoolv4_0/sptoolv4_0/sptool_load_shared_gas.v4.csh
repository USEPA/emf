#!/bin/csh -f
# 
#  This script imports the shared raw data to the speciation tool database
#  These are the data that are typically constant for a version of the
#  Speciation Tool, such as the tables exported from the SPECIATE4.0 database.
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

echo "Importing shared data into database: $SPTOOL_DB"
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing shared data into database: $SPTOOL_DB"  ## log w/ EMF server

echo "Importing gas profile properties"
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing gas profile properties"  ## log w/ EMF server

# argument list is <database name> <table name> <inputfile>
perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB gas_profiles $PROFILES_GAS
if ( $status != 0 ) then
    echo "ERROR: perl script failed for importing gas profile properties"
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing gas profile properties" -t "e"
    exit ( 1 )
endif
# *******************************

echo "Importing gas profile weights"
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing gas profile weights"  ## log w/ EMF server
perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB gas_profile_weights $WEIGHTS_GAS
if ( $status != 0 ) then
    echo "ERROR: perl script failed for importing gas profile weights"
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing gas profile weights" -t "e"
    exit ( 1 )
endif
# *******************************

# obsolete echo "Importing gas species map"
# obsolete $EMF_CLIENT -k $EMF_JOBKEY -m "Importing gas species map"  ## log w/ EMF server
# obsolete perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB gas_species $SPECIES_MAP_GAS
# obsolete if ( $status != 0 ) then
# obsolete     echo "ERROR: perl script failed for importing gas species map"
# obsolete     $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing gas species map" -t "e"
# obsolete     exit ( 1 )
# obsolete endif
# *******************************

echo "Importing species name map"
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing species name map"  ## log w/ EMF server
perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB rename_species $SPECIES_MAP
if ( $status != 0 ) then
    echo "ERROR: perl script failed for importing species name map"
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing species name map" -t "e"
    exit ( 1 )
endif
# *******************************


# *******************************
echo "Importing species properties"
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing species properties"  ## log w/ EMF server
perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB species $SPECIES_PROPERTIES
if ( $status != 0 ) then
    echo "ERROR: perl script failed for importing species properties"
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing species properties" -t "e"
    exit ( 1 )
endif
# *******************************

date

exit( 0 )
