#!/bin/csh

pandoc -s -N --template=./templates/mytemplate.tex --filter ./filter/comments.py --variable mainfont="Times New Roman" --variable sansfont="Helvetica" --variable monofont="Menlo" --variable fontsize=12pt --variable version=2.14 -fmarkdown-implicit_figures -fmarkdown-raw_tex --variable title="COST User's Guide" --variable subtitle="08/10/2017" --toc --variable geometry:margin=1in --latex-engine=xelatex -s -o ./PDF/COST_USER_GUIDE.08-10-2017.pdf ch1_introduction.md ch2_installing_cost.md ch3_control_measure_manager.md ch4_control_strategy_manager.md ch5_control_strategy_exercises.md ch6_example_sql.md ch7_references.md acronyms.md

