#!/bin/bash

BASEDIR=.
VERSION="3.1"

$BASEDIR/pandoc-2.2.1/bin/pandoc --standalone --filter $BASEDIR/pandoc-crossref/pandoc-crossref -M chapters=true -M numberSections=true -M sectionsDepth=-1 -M linkReferences=true -M nameInLink=true --metadata title="Emissions Modeling Framework v$VERSION User's Guide" --metadata subtitle="Last updated: `date "+%B %e, %Y"`" --toc --toc-depth=2 --css base.css -o ./output/index.html ch1_introduction.md ch2_client.md ch3_datasets.md ch4_dataset_qa.md temporal_allocation.md inventory_projection.md troubleshooting.md server_admin.md

