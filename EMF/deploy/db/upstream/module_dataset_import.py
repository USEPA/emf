#!/usr/bin/env python

import argparse
import os
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser(description='Import datasets associated with a module.')
parser.add_argument('-x', '--xml', help='module export XML file', required=True)
parser.add_argument('-d', '--data', help='server directory where the exported datasets are located', required=True)
parser.add_argument('-c', '--client', help='local directory where the EMF client is installed', required=True)
parser.add_argument('-t', '--tomcat', help='Tomcat server address with port (if needed)', default='http://localhost:8080')
parser.add_argument('-k', '--jobkey', help='import job key code', required=True)
args = parser.parse_args()

classpath = ''
for file in os.listdir(os.path.join(args.client, 'lib')):
  if file.endswith('.jar'):
    classpath += os.path.join(args.client, 'lib', file) + ':'
classpath += os.path.join(args.client, 'emf-client.jar')

tree = ET.parse(args.xml)
root = tree.getroot()

for dataset in root.findall(".//*[@property='moduleDatasetInfo']//*[@class='java.util.HashMap']"):
  datasetInfo = {}
  for entry in dataset:
    datasetInfo[entry[0].text] = entry[1].text
  
  print('Importing dataset ' + datasetInfo['datasetName'])
  cmd = ['java', '-classpath', classpath,
         'gov.epa.emissions.framework.client.EMFCmdClient', args.tomcat + '/emf/services', '-d',
         '-k', args.jobkey, '-F', os.path.join(args.data, datasetInfo['exportName']),
         '-T', "'" + datasetInfo['datasetTypeName'] + "'",
         '-N', "'" + datasetInfo['datasetName'] + "'",
         '-V', datasetInfo['version']]
  os.system(' '.join(cmd))
