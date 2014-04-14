#! /usr/bin/env python

import sys

if len(sys.argv) < 2:
	print("Usage: " + sys.argv[0] + " PATH_TO_FILE")
	sys.exit(-1)

inputhandle = open(sys.argv[1], "r")
outputhandle = open(sys.argv[1]+"_out", "w")

for line in inputhandle:
	pos = line.find("\t")
	outputhandle.write(line[pos + 1:])
