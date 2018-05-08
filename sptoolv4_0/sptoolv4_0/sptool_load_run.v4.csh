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
$POSTGRES_BIN/psql -U sptool -v ON_ERROR_STOP=1 -t -f $SPTOOL_SRC_HOME/table_defs_emf_run.sql $SPTOOL_DB
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
$PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB mechanism $MECHANISM
if ( $status != 0 ) then
    echo "ERROR: perl script failed for importing $dataset"
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing $dataset" -t "e"
    exit ( 1 )
endif
# *******************************

set dataset = (PM mechanisms)
echo "Importing $dataset"
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing $dataset"  ## log w/ EMF server
$PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB mechanismPM $MECHANISMPM
if ( $status != 0 ) then
    echo "ERROR: perl script failed for importing $dataset"
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing $dataset" -t "e"
    exit ( 1 )
endif
# *******************************

set dataset = (mechanism descriptions)
echo "Importing $dataset"
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing $dataset"  ## log w/ EMF server
$PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB mechanism_description $MECHANISM_DESCRIPTION
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
$PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB invtable $INVTABLE
if ( $status != 0 ) then
    echo "ERROR: perl script failed for importing $dataset"
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing $dataset" -t "e"
    exit ( 1 )
endif
# *******************************

set dataset = (carbons table)
echo "Importing $dataset"
$EMF_CLIENT -k $EMF_JOBKEY -m "Importing $dataset"  ## log w/ EMF server
$PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB carbons $CARBONS
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
    $PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB static $PROFILES_STATIC
    if ( $status != 0 ) then
	echo "ERROR: perl script failed for importing $dataset"
	$EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing $dataset" -t "e"
	exit ( 1 )
    endif
else
   echo "===>>> WARNING <<<==="
   echo "===>>> WARNING <<<===        No $dataset file defined for import.  "
   echo "===>>> WARNING <<<==="
   echo 
endif
# *******************************






set dataset = (gas profiles)
if $?PROFILES_GAS then
    echo "Importing $dataset $PROFILES_GAS"
    $EMF_CLIENT -k $EMF_JOBKEY -m "Importing gas profile properties"  ## log w/ EMF server
    $PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB gas_profiles $PROFILES_GAS
    if ( $status != 0 ) then
        echo "ERROR: perl script failed for importing $dataset file $PROFILES_GAS"
        #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing gas profile properties" -t "e"
        exit ( 1 )
    endif
else
   echo "===>>> WARNING <<<==="
   echo "===>>> WARNING <<<===        No $dataset file defined for import.  "
   echo "===>>> WARNING <<<==="
   echo 
endif

set dataset = (gas profile weights)
if $?WEIGHTS_GAS then
    echo "Importing $dataset $WEIGHTS_GAS"
    #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "Importing gas profile weights"  ## log w/ EMF server
    $PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB gas_profile_weights $WEIGHTS_GAS
    if ( $status != 0 ) then
        echo "ERROR: perl script failed for importing $dataset file $WEIGHTS_GAS"
        #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing gas profile weights" -t "e"
        exit ( 1 )
    endif
else
   echo "===>>> WARNING <<<==="
   echo "===>>> WARNING <<<===        No $dataset file defined for import.  "
   echo "===>>> WARNING <<<==="
   echo 
endif

set dataset = (PM profiles)
if $?PROFILES_PM then
    echo "Importing $dataset $PROFILES_PM"
    #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "Importing PM profile properties"  ## log w/ EMF server
    $PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB pm_profiles $PROFILES_PM
    if ( $status != 0 ) then
        echo "ERROR: perl script failed for importing $dataset file $PROFILES_PM"
        #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing PM profile properties" -t "e"
        exit ( 1 )
    endif
else
   echo "===>>> WARNING <<<==="
   echo "===>>> WARNING <<<===        No $dataset file defined for import.  "
   echo "===>>> WARNING <<<==="
   echo 
endif

set dataset = (PM profile weights)
if $?WEIGHTS_PM then
    echo "Importing $dataset $WEIGHTS_PM"
    #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "Importing PM profile weights"  ## log w/ EMF server
    $PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB pm_profile_weights $WEIGHTS_PM
    if ( $status != 0 ) then
        echo "ERROR: perl script failed for importing $dataset file $WEIGHTS_PM"
        #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing PM profile weights" -t "e"
        exit ( 1 )
    endif
else
   echo "===>>> WARNING <<<==="
   echo "===>>> WARNING <<<===        No $dataset file defined for import.  "
   echo "===>>> WARNING <<<==="
   echo 
endif

set dataset = (species mapping)
if $?SPECIES_RENAME then
    echo "Importing $dataset $SPECIES_RENAME"
    #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "Importing species name map"  ## log w/ EMF server
    $PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB rename_species $SPECIES_RENAME
    if ( $status != 0 ) then
        echo "ERROR: perl script failed for importing $dataset file $SPECIES_RENAME"
        #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing species rename map" -t "e"
        exit ( 1 )
    endif
else
   echo "===>>> WARNING <<<==="
   echo "===>>> WARNING <<<===        No $dataset file defined for import.  "
   echo "===>>> WARNING <<<==="
   echo 
endif

set dataset = (species properties)
if $?SPECIES_PROPERTIES then
    echo "Importing $dataset $SPECIES_PROPERTIES"
    #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "Importing species properties"  ## log w/ EMF server
    $PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB species $SPECIES_PROPERTIES
    if ( $status != 0 ) then
        echo "ERROR: perl script failed for importing $dataset file $SPECIES_PROPERTIES"
        #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing species properties" -t "e"
        exit ( 1 )
    endif
else
   echo "===>>> WARNING <<<==="
   echo "===>>> WARNING <<<===        No $dataset file defined for import.  "
   echo "===>>> WARNING <<<==="
   echo 
endif

set dataset = (CAMx FCRS)
if $?CAMX_FCRS then
    echo "Importing $dataset $CAMX_FCRS"
    #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "Importing CAMx FRCS"  ## log w/ EMF server
    $PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB camx_fcrs $CAMX_FCRS
    if ( $status != 0 ) then
        echo "ERROR: perl script failed for importing $dataset file $CAMX_FCRS"
        #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing camx fcrs" -t "e"
        exit ( 1 )
    endif
else
   echo "===>>> WARNING <<<==="
   echo "===>>> WARNING <<<===        No $dataset file defined for import.  "
   echo "===>>> WARNING <<<==="
   echo 
endif

set dataset = (VBS SVOC factors)
if $?VBS_SVOC_FACTORS then
    echo "Importing $dataset $VBS_SVOC_FACTORS"
    #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "Importing VBS SVOC FACTORS"  ## log w/ EMF server
    $PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB  vbs_svoc_factors $VBS_SVOC_FACTORS
    if ( $status != 0 ) then
        echo "ERROR: perl script failed for importing $dataset file $VBS_SVOC_FACTORS"
        #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing VBS SVOC factors" -t "e"
        exit ( 1 )
    endif
else
   echo "===>>> WARNING <<<==="
   echo "===>>> WARNING <<<===        No $dataset file defined for import.  "
   echo "===>>> WARNING <<<==="
   echo 
endif

set dataset = (VBS IVOC factors)
if $?VBS_IVOC_FACTORS then
    echo "Importing $dataset $VBS_IVOC_FACTORS"
    #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "Importing VBS IVOC FACTORS"  ## log w/ EMF server
    $PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB  vbs_ivoc_factors $VBS_IVOC_FACTORS
    if ( $status != 0 ) then
        echo "ERROR: perl script failed for importing $dataset file $VBS_IVOC_FACTORS"
        #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing VBS IVOC factors" -t "e"
        exit ( 1 )
    endif
else
   echo "===>>> WARNING <<<==="
   echo "===>>> WARNING <<<===        No $dataset file defined for import.  "
   echo "===>>> WARNING <<<==="
   echo 
endif

set dataset = (IVOC species)
if $?IVOC_SPECIES then
    echo "Importing $dataset $IVOC_SPECIES"
    #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "Importing IVOC SPECIES"  ## log w/ EMF server
    $PERL_BIN/perl $SPTOOL_SRC_HOME/import_rawdata.pl $SPTOOL_DB  ivoc_species $IVOC_SPECIES
    if ( $status != 0 ) then
        echo "ERROR: perl script failed for importing $dataset file $IVOC_SPECIES"
        #emf#	$EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: perl script failed for importing IVOC species" -t "e"
        exit ( 1 )
    endif
else
   echo "===>>> WARNING <<<==="
   echo "===>>> WARNING <<<===        No $dataset file defined for import.  "
   echo "===>>> WARNING <<<==="
   echo 
endif









# Create output directory for the next step
if ( ! -e $EMF_SCRIPTDIR/out ) then
    mkdir $EMF_SCRIPTDIR/out
    chmod ugo+rwx $EMF_SCRIPTDIR/out
endif

date

exit( 0 )

