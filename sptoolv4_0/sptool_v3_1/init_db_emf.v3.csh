#!/bin/csh -f
# 
#  This initialization file needs to be once per installation                     ---
#  Rerunning will delete the an existing database and install the latest version  ---
#  of the Speciation Modeling Tool and the shared raw data.                       ---
#
#  Variables to be predefined prior to calling:
#      SPTOOL_SRC_HOME - set to the Speciation Tool source code directory
#      SPTOOL_DB - set to the database name for this installation
#============================================================================================
#  v3.0
#  01Jun2011
#============================================================================================

set script_name = init_db_emf.csh
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
    echo "New database: SPTOOL_DB = $SPTOOL_DB"
else
    set exitstatus = 1
    echo "SCRIPT ERROR: Required environment variable SPTOOL_DB not set"
    echo "              in script $script_name"
endif

if ( $exitstatus != 0 ) then
    ## log w/ EMF server that script is running
    $EMF_CLIENT -k $EMF_JOBKEY -m "ERROR: $script_name aborting because missing key environmental variables SPTOOL_SRC_HOME and SPTOOL_DB" -t "e"
    echo "ABORT: $script_name script aborting with errors"
    exit ( 1 )
endif

## in emf
date

## echo which psql is being used
echo "Path to psql: $POSTGRES_BIN"

$POSTGRES_BIN/dropdb -U sptool $SPTOOL_DB
# if dropping the DB fails, don't continue
#if ( $? != 0 ) exit(1);

$EMF_CLIENT -k $EMF_JOBKEY -m "Creating database $SPTOOL_DB"  ## log w/ EMF server

## requires that run on postgres server
## and that local connections are trusted (i.e. no passwords needed)

$POSTGRES_BIN/createdb -U sptool $SPTOOL_DB
if ( $status != 0 ) then
    set errMsg = "ERROR: psql failed to create a new database $SPTOOL_DB. Usually means database already exists."
    echo $errMsg
    $EMF_CLIENT -k $EMF_JOBKEY -m "$errMsg" -t "e"
    exit ( 1 )
endif
$POSTGRES_BIN/createlang -U sptool plpgsql $SPTOOL_DB
if ( $status != 0 ) then
    set errMsg = "ERROR: psql failed to create plpgsql language in database"
    echo $errMsg
    $EMF_CLIENT -k $EMF_JOBKEY -m "$errMsg" -t "e"
#    exit ( 1 )
endif
$POSTGRES_BIN/psql -U sptool -c "create schema shared" $SPTOOL_DB
if ( $status != 0 ) then
    set errMsg = "ERROR: psql failed to create shared schema"
    echo $errMsg
    $EMF_CLIENT -k $EMF_JOBKEY -m "$errMsg" -t "e"
    exit ( 1 )
endif
$POSTGRES_BIN/psql -U sptool -c "grant create on database $SPTOOL_DB to public" $SPTOOL_DB
if ( $status != 0 ) then
    set errMsg = "ERROR: psql failed to grant permissions on schema"
    echo $errMsg
    $EMF_CLIENT -k $EMF_JOBKEY -m "$errMsg" -t "e"
    exit ( 1 )
endif
$POSTGRES_BIN/psql -U sptool -c "grant all on schema shared to public" $SPTOOL_DB
if ( $status != 0 ) then
    set errMsg = "ERROR: psql failed to grant permissions on schema"
    echo $errMsg
    $EMF_CLIENT -k $EMF_JOBKEY -m "$errMsg" -t "e"
    exit ( 1 )
endif

## setup functions and initialize tables
echo "Defining custom functions and initializing tables"
$EMF_CLIENT -k $EMF_JOBKEY -m "Definining custom functions and initializing tables"  ## log w/ EMF server
## need to add ON_ERROR_STOP=1 so that it will give a non-zero error status if it
## fails to run all commands in file (otherwise a failure will return 0)

$POSTGRES_BIN/psql -U sptool -v ON_ERROR_STOP=1 -q -f $SPTOOL_SRC_HOME/drop_table.sql $SPTOOL_DB
if ( $status != 0 ) then
    set errMsg = "ERROR: psql failed to initialize drop table function (drop_table.sql)"
    echo $errMsg
    $EMF_CLIENT -k $EMF_JOBKEY -m "$errMsg" -t "e"
    exit ( 1 )
endif

$POSTGRES_BIN/psql -U sptool -v ON_ERROR_STOP=1 -q -t -f $SPTOOL_SRC_HOME/table_defs.sql $SPTOOL_DB
if ( $status != 0 ) then
    set errMsg = "ERROR: psql failed to initialize shared tables (table_defs.sql)"
    echo $errMsg
    $EMF_CLIENT -k $EMF_JOBKEY -m "$errMsg" -t "e"
    exit ( 1 )
endif

$POSTGRES_BIN/psql -U sptool -v ON_ERROR_STOP=1 -q -t -f $SPTOOL_SRC_HOME/table_inps.sql $SPTOOL_DB
if ( $status != 0 ) then
    set errMsg = "ERROR: psql failed to define scenario tables function (table_inps.sql) "
    echo $errMsg
    $EMF_CLIENT -k $EMF_JOBKEY -m "$errMsg" -t "e"
    exit ( 1 )
endif

$POSTGRES_BIN/psql -U sptool -v ON_ERROR_STOP=1 -q -t -f $SPTOOL_SRC_HOME/make_splits.sql $SPTOOL_DB
if ( $status != 0 ) then
    set errMsg = "ERROR: psql failed to define split factor functions (make_splits.sql)"
    echo $errMsg
    $EMF_CLIENT -k $EMF_JOBKEY -m "$errMsg" -t "e"
    exit ( 1 )
endif

$POSTGRES_BIN/psql -U sptool -v ON_ERROR_STOP=1 -q -t -f $SPTOOL_SRC_HOME/make_pm_splits.sql $SPTOOL_DB
if ( $status != 0 ) then
    set errMsg = "ERROR: psql failed to define PM split factor functions (make_pm_splits.sql)"
    echo $errMsg
    $EMF_CLIENT -k $EMF_JOBKEY -m "$errMsg" -t "e"
    exit ( 1 )
endif

$POSTGRES_BIN/psql -U sptool -v ON_ERROR_STOP=1 -q -t -f $SPTOOL_SRC_HOME/prep_out.sql $SPTOOL_DB
if ( $status != 0 ) then
    set errMsg = "ERROR: psql failed to define output functions (prep_out.sql)"
    echo $errMsg
    $EMF_CLIENT -k $EMF_JOBKEY -m $errMsg -t "e"
    exit ( 1 )
endif

date

