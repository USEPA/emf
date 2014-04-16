#!/usr/bin/perl
#/afs/isis/pkg/perl/bin/perl

# Script name: compare_invs.pl
# Created by: Catherine Seppanen (cseppan@unc.edu)
# Last modified: 8/17/2005
#
# Usage: compare_invs.pl <inventory format> <inventory 1> <inventory 2 (filename or directory)> <percent difference>
#
# Description: This script compares two inventory files. It is designed to be an
# alternative to 'diff' which prints the whole line rather than just the columns
# that differ. Additionally, you can specify a percent difference, below which
# the script will not print the values. This value is optional; the script will
# use 0.1% as the default value.

use strict;
use Carp qw(cluck confess);

require 'ORLNonpoint.pm';
require 'ORLNonroad.pm';
require 'ORLOnroad.pm';
require 'ORLPoint.pm';


# get command line arguments

# initialize format objects based on command-line option
my $formatname = $ARGV[0];
my ($format1, $format2);
eval "\$format1 = Format::$formatname->new(); \$format2 = Format::$formatname->new();";
confess "Unrecognized file format $formatname" if $@;

# open first file
open (my $file1, $ARGV[1]) or confess "Could not open file $ARGV[1]";

# second file may just be a directory, so append first filename
my $filename = (-d $ARGV[2]) ? "$ARGV[2]/$ARGV[1]" : $ARGV[2];
open (my $file2, $filename) or confess "Could not open file $filename";

# last argument is percent difference
my $maxDiff = defined($ARGV[3]) ? $ARGV[3] / 100 : 0.001;


my $status = 0;

# variables for reading files
my %filehash;
my $key;

# read and store first file
print "Processing file 1...\n";
while (<$file1>) {
    
    # split line into fields and skip blank or comment lines
    my @fields = $format1->splitLine($_);
    next unless scalar(@fields);
    
    # check format of all fields
    unless ($format1->checkFields(\@fields)) {
        $status = 1;
        next;
    }

    # create unique key for entry
    $key = $format1->makeKey(\@fields);
    
    # check if current entry is duplicate
    if (exists $filehash{$key}) {
        warn "Source/pollutant $key in 1st file at line " . $format1->lineNumber() .
             " already encountered at line " . $filehash{$key}{'line'};
        $status = 1;
        next;
    }
    
    # store entry data
    $filehash{$key}{'fields'} = \@fields;
    $filehash{$key}{'found'}  = 0;
    $filehash{$key}{'line'}   = $format1->lineNumber();
}

# read and compare second file
print "Processing file 2...\n";
while (<$file2>) {

    my @fields = $format2->splitLine($_);
    next unless scalar(@fields);
    
    unless ($format2->checkFields(\@fields)) {
        $status = 1;
        next;
    }
    
    $key = $format2->makeKey(\@fields);

    # try to match entry with 1st file
    unless (exists $filehash{$key}) {
        warn "Source/pollutant $key only in 2nd file at line " . 
             $format2->lineNumber();
        $status = 1;
        next;
    }
    
    # check if current entry is duplicate
    if ($filehash{$key}{'found'}) {
        warn "Source/pollutant $key in 2nd file at line " . $format2->lineNumber() .
             " already encountered at line " . $filehash{$key}{'found'};
        $status = 1;
        next;
    }
    
    $filehash{$key}{'found'} = $format2->lineNumber();

    # compare all fields in entry
    unless ($format2->compareFields(\@fields, $filehash{$key}{'fields'})) {
        warn "Files differ: 1st file, line " . $filehash{$key}{'line'} .
             "; 2nd file, line " . $format2->lineNumber() . 
             " (see earlier errors)";
        $status = 1;
    }

}

# check that all entries in 1st file were matched
foreach $key (keys %filehash) {
    unless ($filehash{$key}{'found'}) {
        warn "Source/pollutant $key only in 1st file at line " .
             $filehash{$key}{'line'};
        $status = 1;
    }
}

close $file1;
close $file2;

if ($status) {
    print "Status: failure\n";
} else {
    print "Status: success\n";
}

exit($status);
