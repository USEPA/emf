#!/bin/bash

cat ch1_introduction.md ch2_installing_cost.md ch3_control_measure_manager.md ch4_control_strategy_manager.md ch5_control_strategy_exercises.md ch6_example_sql.md ch7_references.md acronyms.md > rendered/all_chapters.md
./parse_all_txt.pl rendered/all_chapters.md > rendered/cost_guide.md
cd rendered
multimarkdown cost_guide.md
cd ..
