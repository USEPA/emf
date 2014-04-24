#!/bin/bash

## Creates an EMF database and populates it 
## with the EPA's training material.
##
## Should be run from the same directory as this file 
##
## postgres user must already be created -- see the postgresql
## installation for details
##------------------------------------------------------------##

## create emf user and the 'EMF' database
createuser -U postgres -s -P emf
createdb -U emf -E UTF8 EMF


## do a restore of the database from the training backup
pg_restore -d EMF -U emf emf_training_db_05102007.backup

