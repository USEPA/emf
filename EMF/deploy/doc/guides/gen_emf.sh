#!/bin/bash

cat ch1_introduction.txt ch2_client.txt ch3_datasets.txt ch4_dataset_qa.txt troubleshooting.txt server_admin.txt > rendered/all_chapters.txt
parse_all_txt.pl rendered/all_chapters.txt > rendered/guide.txt
cd rendered
~/Library/Application\ Support/MultiMarkdown/bin/mmd-xslt guide.txt 
cd ..
