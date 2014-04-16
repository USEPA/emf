package Format;

use strict;
use Carp qw(cluck);

sub new {
    my $self = {};
    bless($self);
    $self->{'line_number'} = 0;
    return $self;
}

sub lineNumber {
    my ($self, $nline) = @_;
    $self->{'line_number'} = $nline if defined $nline;
    return $self->{'line_number'};
}

sub splitLine {
    my ($self, $line) = @_;
    
    $self->{'line_number'}++;
    
    chomp $line;
    
    # skip blank and comment lines (for now)
    return () if ($line !~ /\S/o);
    return () if ($line =~ /^#/o);
    
    # remove leading blanks, trailing comments, and trailing blanks
    $line =~ s/^\s+//o;
    $line =~ s/!.*$//o;
    $line =~ s/\s+$//o;
    
    # fill in empty fields at beginning or end of line
    $line =~ s/^(?=,)|(?<=,)$/""/go;
    
    # split line into fields
    my @fields = grep { 
        if ($_ !~ /^(,|\s)+$/o) {    # ignore fields that are only delimiters
            s/^"([^"]*)"$/$1/o;      # strip double quotes
            s/^'([^']*)'$/$1/o;      # strip single quotes
            1;
        } else {
            0;
        } } 
        split(/(
                "[^"]*" | '[^']*' |  # match double or single quoted strings
                [^,\s]+ |            # match non-delimiters
                (?<=,)(?=,)          # match empty commas
               )/ox, $line);
    
    # remove blank field at beginning that shift adds
    shift @fields;
    
#    print '"' . join('", "', @fields) ."\"\n";
    return @fields;
}

sub checkNumFields {
    my ($self, $current, $expected) = @_;
    
    if ($current != $expected) {
        warn "$current fields instead of $expected at line " . $self->lineNumber();
        return 0;
    }
    return 1;
}

sub checkFIPS {
    my ($self, $fips) = @_;
    return $self->invalidErr( 0+($fips =~ /^\d{4,5}$/o), "FIPS code $fips");
}

sub checkSCC {
    my ($self, $scc) = @_;
    return $self->invalidErr( 0+($scc =~ /^\w{1,10}$/o || $scc eq 'N/A'), 
                              "SCC $scc");
}

sub checkPoll {
    my ($self, $poll) = @_;
    return $self->invalidErr( 0+($poll =~ /^\w{1,16}$/o), 
                              "pollutant name $poll");
}

sub checkSIC {
    my ($self, $sic) = @_;
    return $self->invalidErr( 0+($sic =~ /^[\w-]{0,4}$/o || $sic eq '-9'), 
                              "SIC $sic");
}

sub checkMACT {
    my ($self, $mact) = @_;
    return $self->invalidErr( 0+($mact =~ /^[\w-]{0,6}$/o || $mact eq '-9'), 
                              "MACT code $mact");
}

sub checkErpType {
    my ($self, $erptype) = @_;
    return $self->invalidErr( 0+($erptype =~ /^\d{0,2}$/o || $erptype eq '-9'), 
                              "emission release point code $erptype");
}

sub checkSrcType {
    my ($self, $srctype) = @_;
    return $self->invalidErr( 0+($srctype =~ /^\d{0,2}$/o || $srctype eq '-9'), 
                              "source type code $srctype");
}

sub checkNAICS {
    my ($self, $naics) = @_;
    return $self->invalidErr( 0+($naics =~ /^[\w-]{0,6}$/o || $naics eq '-9'), 
                              "NAICS code $naics");
}

sub checkPntChr {
    my ($self, $char) = @_;
    return $self->invalidErr( 0+($char =~ /^.{0,15}$/o), 
                              "point source characteristic $char");
}

sub checkPlant {
    my ($self, $name) = @_;
    return $self->invalidErr( 0+($name =~ /^.{0,40}$/o), "plant name $name");
}

sub checkCType {
    my ($self, $ctype) = @_;
    return $self->invalidErr( 0+($ctype =~ /^(U|L)$/o), "coordinate type $ctype");
}   

sub checkNum {
    my ($self, $num) = @_;
    return $self->invalidErr( 0+($num + 0 != 0 || 
                              $num =~ / (\+|-)?                    # leading sign
                                        ( (\d+ \.? \d*) |          # 5, 5., 5.5
                                          (\. \d+) )               # .5
                                        ( ( (\+|-)?E | E(\+|-) )   # E, +E, E+
                                          \d+)?                    # exponent
                                      /ox), 
                              "numeric value $num");
}

sub invalidErr {
    my ($self, $condition, $message) = @_;
    warn "Invalid $message at line " . $self->lineNumber() unless $condition;
    return $condition;
}

sub compareNumFields {
    my ($self, $fields1, $fields2) = @_;
    
    if ($fields1 != $fields2) {
        warn "Number of fields $fields1 and $fields2 do not match";
        return 0;
    }
    return 1;
}

sub compareStr {
    my ($self, $string1, $string2) = @_;
    
    if ($string1 ne $string2) {
        warn "Fields $string1 and $string2 do not match";
        return 0;
    }

    return 1;
}

sub compareNum {
    my ($self, $num1, $num2) = @_;
    
    if ($num1 != $num2) {
        if ($num1 == 0) {
            warn "Values $num1 and $num2 do not match";
            return 0;
        } else {
            my $percent = ($num1 - $num2) / $num1;
            if (abs($percent) > 0.001) {
                warn "Values $num1 and $num2 differ by" . sprintf(" %.3f%%", $percent);
                return 0;
            }
        }
    }
    return 1;
}

1;
