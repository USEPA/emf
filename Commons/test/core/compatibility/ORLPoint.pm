package Format::ORLPoint;

use strict;

use Format;
our @ISA = ("Format");

use constant {
    FIPS    => 0,
    PLANT   => 1,
    POINT   => 2,
    STACK   => 3,
    SEGMENT => 4,
    NAME    => 5,
    SCC     => 6,
    ERPTYPE => 7,
    SRCTYPE => 8,
    STKHGT  => 9,
    STKDIAM => 10,
    STKTEMP => 11,
    STKFLOW => 12,
    STKVEL  => 13,
    SIC     => 14,
    MACT    => 15,
    NAICS   => 16,
    CTYPE   => 17,
    XLOC    => 18,
    YLOC    => 19,
    UTMZ    => 20,
    POLL    => 21,
    ANN     => 22,
    AVG     => 23,
    CEFF    => 24,
    REFF    => 25,
    CPRI    => 26,
    CSEC    => 27,
    NUM_FIELDS => 28,
};

sub new {
    my $self = Format->new();
    bless($self);
    return $self;
}

sub checkFields {
    my ($self, $fields) = @_;
    
    return 0 unless $self->checkNumFields(scalar(@$fields), NUM_FIELDS);
    
    my $status = 1;
    $status = $self->checkFIPS   ($fields->[FIPS])    && $status;
    $status = $self->checkPntChr ($fields->[PLANT])   && $status;
    $status = $self->checkPntChr ($fields->[POINT])   && $status;
    $status = $self->checkPntChr ($fields->[STACK])   && $status;
    $status = $self->checkPntChr ($fields->[SEGMENT]) && $status;
    $status = $self->checkPlant  ($fields->[NAME])    && $status;
    $status = $self->checkSCC    ($fields->[SCC])     && $status;
    $status = $self->checkErpType($fields->[ERPTYPE]) && $status;
    $status = $self->checkSrcType($fields->[SRCTYPE]) && $status;
    $status = $self->checkNum    ($fields->[STKHGT])  && $status;
    $status = $self->checkNum    ($fields->[STKDIAM]) && $status;
    $status = $self->checkNum    ($fields->[STKTEMP]) && $status;
    $status = $self->checkNum    ($fields->[STKFLOW]) && $status;
    $status = $self->checkNum    ($fields->[STKVEL])  && $status;
    $status = $self->checkSIC    ($fields->[SIC])     && $status;
    $status = $self->checkMACT   ($fields->[MACT])    && $status;
    $status = $self->checkNAICS  ($fields->[NAICS])   && $status;
    $status = $self->checkCType  ($fields->[CTYPE])   && $status;
    $status = $self->checkNum    ($fields->[XLOC])    && $status;
    $status = $self->checkNum    ($fields->[YLOC])    && $status;
    $status = $self->checkNum    ($fields->[UTMZ])    && $status;
    $status = $self->checkPoll   ($fields->[POLL])    && $status;
    $status = $self->checkNum    ($fields->[ANN])     && $status;
    $status = $self->checkNum    ($fields->[AVG])     && $status;
    $status = $self->checkNum    ($fields->[CEFF])    && $status;
    $status = $self->checkNum    ($fields->[REFF])    && $status;
    $status = $self->checkNum    ($fields->[CPRI])    && $status;
    $status = $self->checkNum    ($fields->[CSEC])    && $status;
    return $status;
}

sub makeKey {
    my ($self, $fields) = @_;
    
    return join(' ', $fields->[FIPS], $fields->[PLANT], $fields->[POINT],
                     $fields->[STACK], $fields->[SEGMENT], $fields->[SCC],
                     $fields->[POLL]);
}

sub compareFields {
    my ($self, $fields1, $fields2) = @_;
    
    return 0 unless $self->compareNumFields(scalar(@$fields1), scalar(@$fields2));
    
    my $status = 1;
    $status = $self->compareStr($fields1->[FIPS],    $fields2->[FIPS])    && $status;
    $status = $self->compareStr($fields1->[PLANT],   $fields2->[PLANT])   && $status;
    $status = $self->compareStr($fields1->[POINT],   $fields2->[POINT])   && $status;
    $status = $self->compareStr($fields1->[STACK],   $fields2->[STACK])   && $status;
    $status = $self->compareStr($fields1->[SEGMENT], $fields2->[SEGMENT]) && $status;
    $status = $self->compareStr($fields1->[NAME],    $fields2->[NAME])    && $status;
    $status = $self->compareStr($fields1->[SCC],     $fields2->[SCC])     && $status;
    $status = $self->compareStr($fields1->[ERPTYPE], $fields2->[ERPTYPE]) && $status;
    $status = $self->compareStr($fields1->[SRCTYPE], $fields2->[SRCTYPE]) && $status;
    $status = $self->compareNum($fields1->[STKHGT],  $fields2->[STKHGT])  && $status;
    $status = $self->compareNum($fields1->[STKDIAM], $fields2->[STKDIAM]) && $status;
    $status = $self->compareNum($fields1->[STKTEMP], $fields2->[STKTEMP]) && $status;
    $status = $self->compareNum($fields1->[STKFLOW], $fields2->[STKFLOW]) && $status;
    $status = $self->compareNum($fields1->[STKVEL],  $fields2->[STKVEL])  && $status;
    $status = $self->compareStr($fields1->[SIC],     $fields2->[SIC])     && $status;
    $status = $self->compareStr($fields1->[MACT],    $fields2->[MACT])    && $status;
    $status = $self->compareStr($fields1->[NAICS],   $fields2->[NAICS])   && $status;
    $status = $self->compareStr($fields1->[CTYPE],   $fields2->[CTYPE])   && $status;
    $status = $self->compareNum($fields1->[XLOC],    $fields2->[XLOC])    && $status;
    $status = $self->compareNum($fields1->[YLOC],    $fields2->[YLOC])    && $status;
    $status = $self->compareStr($fields1->[UTMZ],    $fields2->[UTMZ])    && $status;
    $status = $self->compareStr($fields1->[POLL],    $fields2->[POLL])    && $status;
    $status = $self->compareNum($fields1->[ANN],     $fields2->[ANN])     && $status;
    $status = $self->compareNum($fields1->[AVG],     $fields2->[AVG])     && $status;
    $status = $self->compareNum($fields1->[CEFF],    $fields2->[CEFF])    && $status;
    $status = $self->compareNum($fields1->[REFF],    $fields2->[REFF])    && $status;
    $status = $self->compareNum($fields1->[CPRI],    $fields2->[CPRI])    && $status;
    $status = $self->compareNum($fields1->[CSEC],    $fields2->[CSEC])    && $status;
    return $status;
}

1;
