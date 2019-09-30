#!/usr/bin/perl
#################################################################
## EMF_util
##
##    A series of utilities for sending EMF messages, errors,
##    and outputs from the perl programs to the EMF server
##
#################################################################

## Test if using EMF, by checking for EMF_CLIENT environmental variable
## if there is a value, then will act as true in a condition
## Note: use_emf has global scope
my $EMF_CLIENT = $ENV{"EMF_CLIENT"};
$use_emf = "f";
if ($EMF_CLIENT) {
    $use_emf = "t";
}

## subroutine to print a message and send it through the EMF
sub print_emf{
    ## loop over inputs
    my $msg ="";
    foreach my $elem (@_) {
	$msg = $msg . $elem;
    }
    ## replace any newline characters w/ space b/c can't have newlines in EMF message
    $msg =~ s/\n/ /g;
    if ($use_emf eq "t"){
	system("\$EMF_CLIENT -k \$EMF_JOBKEY -m '$msg'");
    }
    print "$msg\n\n";
}

## subroutine to send error message through EMF before die
sub die_emf{
    ## loop over inputs
    my $err_msg ="";
    foreach my $elem (@_) {
	$err_msg = $err_msg . $elem;
    }
    ## replace any newline characters w/ space b/c can't have newlines in EMF message
    $err_msg =~ s/\n/ /g;

    if ($use_emf eq "t"){
	system("\$EMF_CLIENT -k \$EMF_JOBKEY -m '$err_msg' -t 'e' -s 'Failed'");
    }
    die "$err_msg\n";
}

## subroutine to register an output with EMF
## takes output file and dataset type
sub output_emf{
    my ($outFile, $dsType) = @_;

    ## test if file exists
    if ( -e $outFile){
	## register output
	if ($use_emf eq "t"){
	    system("\$EMF_CLIENT -k \$EMF_JOBKEY -F $outFile -T '$dsType'");
	} else {
	    print "EMF not enabled, therefore not registering output: $outFile \n";
	}
    } else {
	print "File $outFile doesn't exist\n";
    }
}
