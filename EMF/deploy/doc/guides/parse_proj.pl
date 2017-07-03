#!/usr/bin/perl

use strict;
use warnings 'FATAL' => 'all';

die "Usage: $0 <filename>\n" unless scalar(@ARGV) == 1;
my $input;
open ($input, '<', $ARGV[0]) or die "Could not open $ARGV[0]";

my $output;

my %sections;
my $max_sectionlevels = 6;
my @section_counts = (0) x ($max_sectionlevels + 1);

sub CurSectionNumber
{
  my $label = $section_counts[2];
  for my $i (3..($max_sectionlevels + 1))
  {
    $label .= '.' . $section_counts[$i] if $section_counts[$i];
  }
  return $label;
}

my %figures;
my $figureNum = 1;

my %tables;
my $tableNum = 1;

while (<$input>)
{
  my $label = undef;
  
  # document title
  if (/^Title: (.+)$/)
  {
    $output .= "Title: $1\n";
  }

  # chapter and section headings
  elsif (/^(#+)\s+(.+?)\s+\1$/)
  {
    my $hashes = $1;
    my $level = length $hashes;
    my $title = $2;
    my $label;
    
    if ($level == 1)
    {
      $label = '';
    }
    else
    {
      # increment our current level
      $section_counts[$level]++;
      # reset lower levels
      for my $i (($level + 1)..$max_sectionlevels)
      {
        $section_counts[$i] = 0;
      }
      $label = CurSectionNumber();
    }
    if ($label)
    {
      $output .= "$hashes $label $title $hashes\n";
      if ($title =~ /\[(.*)\]/)
      {
        $sections{$1} = $label;
      }
    }
  }

  # figures
  elsif (/^!\[(.+)\]\[(.+)\]$/)
  {
    $label = "Figure $figureNum";
    $output .= "![$label: $1][$2]\n";
    $figures{$2} = $label;
    $figureNum += 1;
  }
  
  # tables
  elsif (/^\[(.+)\]\[(.+_table)\]$/)
  {
    $label = "Table $tableNum";
    $output .= "[$label: $1][$2]\n";
    $tables{$2} = $label;
    $tableNum += 1;
  }

  else
  {
    $output .= $_;
  }
}

for my $sectionId (keys %sections)
{
  my $label = $sections{$sectionId};
  $output =~ s/\[Section\](\(#$sectionId\))/[Section $label]$1/g;
}

for my $figureId (keys %figures)
{
  my $label = $figures{$figureId};
  $output =~ s/\[Figure\](\(#$figureId\))/[$label]$1/g;
}

for my $tableId (keys %tables)
{
  my $label = $tables{$tableId};
  $output =~ s/\[Table\](\(#$tableId\))/[$label]$1/g;
}

print $output;
