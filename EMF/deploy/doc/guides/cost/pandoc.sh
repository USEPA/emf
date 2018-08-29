#!/bin/bash

# activate virtualenv before running

BASEDIR=.
VERSION="3.2"

$BASEDIR/pandoc-2.2.1/bin/pandoc --standalone --filter pandoc-fignos --filter pandoc-tablenos --filter ./filter/comments.py --number-sections -M xnos-number-sections=On --metadata title="CoST v$VERSION User's Guide" --metadata subtitle="Last updated: `date "+%B %e, %Y"`" --toc --toc-depth=3 --css base.css -o ./output/index.html ch1_introduction.md ch2_installing_cost.md ch3_control_measure_manager.md ch4_control_strategy_manager.md ch5_control_strategy_exercises.md ch6_example_sql.md ch7_references.md acronyms.md

$BASEDIR/pandoc-2.2.1/bin/pandoc --standalone --filter pandoc-fignos --filter pandoc-tablenos --filter ./filter/comments.py --number-sections -V title="CoST v$VERSION User's Guide" -V subtitle="Last updated: `date "+%B %e, %Y"`" -V version=$VERSION --toc --toc-depth=3 -V fontfamily=palatino,beramono -V fontsize=11pt -V linestretch=1.25 -V geometry:margin=1in -V colorlinks --template ./templates/template.2.2.1.latex -o ./output/CoST_Users_Guide.pdf ch1_introduction.md ch2_installing_cost.md ch3_control_measure_manager.md ch4_control_strategy_manager.md ch5_control_strategy_exercises.md ch6_example_sql.md ch7_references.md acronyms.md templates/metadata.yaml
