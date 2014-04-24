#!/bin/sh

##------------------------------------------------------------##
## Basic backup script for the EMF database
##
##    The backup can be used with pg_restore to recreate an
##    EMF database
##------------------------------------------------------------##

## Postgres root directory
export POSTGRESDIR=/usr/local/PostgreSQL/8.3

## temporary directory to put dump of database and
## final directory (potentially mass storage) to save dump in
## NOTE: there should be sufficient room in both for a full backup
export TMPDIR=/data1/tmp
export FINALDIR=/data2/db_backup

## verbose flag (empty if don't want pg_dump to list each table)
export VERBOSE_FLAG="-v"
##export VERBOSE_FLAG=""

## backup file
export BACKUP_FILE=EMF_db_`date +%F`.backup
export LOG_FILE=log.`date +%F`

##----------------------------------------------##

## path to pg_dump
export POSTGRESBINDIR=$POSTGRESDIR/bin


## check if tmp and final directory exist, if not create them
if [ ! -d $TMPDIR ]; then
    echo making tmpdir $TMPDIR
    mkdir -p $TMPDIR
fi
if [ ! -d $FINALDIR ]; then
    echo making final dir $FINALDIR
    mkdir -p $FINALDIR
fi


## backup all of EMF database to one file and move from temp to final path
echo Beginning backup `date` > $TMPDIR/$LOG_FILE
$POSTGRESBINDIR/pg_dump -U emf -F c $VERBOSE_FLAG -f ${TMPDIR}/$BACKUP_FILE EMF >> $TMPDIR/$LOG_FILE 2>&1

echo "Moving backup from temp directory ($TMPDIR) to final directory ($FINALDIR)" >> $TMPDIR/$LOG_FILE  2>&1
mv ${TMPDIR}/$BACKUP_FILE ${FINALDIR}/$BACKUP_FILE

echo Finished backup `date` >> $TMPDIR/$LOG_FILE  2>&1

## move the log to final dir
mv ${TMPDIR}/$LOG_FILE ${FINALDIR}/$LOG_FILE
