#!/bin/bash

cat ch1_introduction.txt ch2_installing_cost.txt ch3_control_measure_manager.txt ch4_control_strategy_manager.txt ch5_control_strategy_exercises.txt ch6_example_sql.txt ch7_references.txt acronyms.txt > rendered/all_chapters.txt
parse_all_txt.pl rendered/all_chapters.txt > rendered/cost_guide.txt
cd rendered
~/Library/Application\ Support/MultiMarkdown/bin/mmd-xslt cost_guide.txt 
cd ..
