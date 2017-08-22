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

sub IncrementSection
{
  my ($level) = @_;
  # increment count for current level
  $section_counts[$level]++;
  # reset all sublevels
  for my $i (($level + 1)..$max_sectionlevels)
  {
    $section_counts[$i] = 0;
  }
}
sub CurSectionNumber
{
  my $label = $section_counts[1];
  for my $i (2..$max_sectionlevels)
  {
    last unless $section_counts[$i];
    $label .= '.' . $section_counts[$i];
  }
  return "Chapter $label." unless $label =~ /\./;
  return $label;
}

my %figures;
my $figureNum = 1;

my %tables;
my $tableNum = 1;

while (<$input>)
{
  next if (/^Title: / || /^Author: / || /^CSS: /);

  my $label = undef;

  # chapter and section headings
  if (/^(#+)\s+(.+?)\s+\1$/)
  {
    my $hashes = $1;
    my $level = length $hashes;
    my $title = $2;
    
    IncrementSection($level);
    $label = CurSectionNumber();
    if ($level == 1)
    {
      # reset figure and table numbers
      $figureNum = 1;
      $tableNum = 1;
    }
    $output .= "$hashes $label $title $hashes\n";
    if ($title =~ /\[(.*)\]/)
    {
      $sections{$1} = $label;
    }
  }

  # figures
  elsif (/^!\[(.+)\]\[(.+)\]$/)
  {
    $label = "Figure $section_counts[1]\\-$figureNum";
    $output .= "![$label: $1][$2]\n";
    $figures{$2} = $label;
    $figureNum += 1;
  }
  
  # tables
  elsif (/^\[(.+)\]\[(.+_table)\]$/)
  {
    $label = "Table $section_counts[1]\\-$tableNum";
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
  $output =~ s/\[Chapter\](\(#$sectionId\))/[$label]$1/g;
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

print <<END;
latex input: mmd-memoir-header
Title: Emissions Modeling Framework User's Guide
CSS: base.css
XHTML XSLT: xhtml-toc-h1.xslt
LaTeX Mode: memoir  
latex input: mmd-memoir-begin-doc
latex footer: mmd-memoir-footer
END
print $output;
