package Format::ORLNonroad;

use strict;

use Format;
our @ISA = ("Format");

use constant {
    FIPS => 0,
    SCC  => 1,
    POLL => 2,
    ANN  => 3,
    AVG  => 4,
    CEFF => 5,
    REFF => 6,
    RPEN => 7,
    NUM_FIELDS => 8,
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
    $status = $self->checkFIPS($fields->[FIPS]) && $status;
    $status = $self->checkSCC ($fields->[SCC])  && $status;
    $status = $self->checkPoll($fields->[POLL]) && $status;
    $status = $self->checkNum ($fields->[ANN])  && $status;
    $status = $self->checkNum ($fields->[AVG])  && $status;
    $status = $self->checkNum ($fields->[CEFF]) && $status;
    $status = $self->checkNum ($fields->[REFF]) && $status;
    $status = $self->checkNum ($fields->[RPEN]) && $status;
    return $status;
}

sub makeKey {
    my ($self, $fields) = @_;
    
    return join(' ', $fields->[FIPS], $fields->[SCC], $fields->[POLL]);
}

sub compareFields {
    my ($self, $fields1, $fields2) = @_;
    
    return 0 unless $self->compareNumFields(scalar(@$fields1), scalar(@$fields2));
    
    my $status = 1;
    $status = $self->compareStr($fields1->[FIPS], $fields2->[FIPS]) && $status;
    $status = $self->compareStr($fields1->[SCC],  $fields2->[SCC])  && $status;
    $status = $self->compareStr($fields1->[POLL], $fields2->[POLL]) && $status;
    $status = $self->compareNum($fields1->[ANN],  $fields2->[ANN])  && $status;
    $status = $self->compareNum($fields1->[AVG],  $fields2->[AVG])  && $status;
    $status = $self->compareNum($fields1->[CEFF], $fields2->[CEFF]) && $status;
    $status = $self->compareNum($fields1->[REFF], $fields2->[REFF]) && $status;
    $status = $self->compareNum($fields1->[RPEN], $fields2->[RPEN]) && $status;
    return $status;
}

1;
