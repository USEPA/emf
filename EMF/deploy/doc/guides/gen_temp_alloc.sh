#!/bin/bash

parse_proj.pl temporal_allocation.txt > rendered/temporal_allocation.txt; cd rendered; ~/Library/Application\ Support/MultiMarkdown/bin/mmd-xslt temporal_allocation.txt; cd ..
