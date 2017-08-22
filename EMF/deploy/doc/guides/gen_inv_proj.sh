#!/bin/bash

parse_proj.pl inventory_projection.txt > rendered/inventory_projection.txt; cd rendered; ~/Library/Application\ Support/MultiMarkdown/bin/mmd-xslt inventory_projection.txt; cd ..
