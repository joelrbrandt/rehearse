#!/bin/sh

CLASSPATH=lib\\pde.jar\;lib\\core.jar\;lib\\jna.jar\;lib\\ecj.jar\;lib\\antlr.jar\;java\\lib\\tools.jar\;lib\\jj1.0.1.jar\;lib\\stringtree-json-2.0.5.jar

export CLASSPATH

#cd work && ./java/bin/java processing.app.Base
cd work && ./java/bin/java edu.stanford.hci.processing.RehearseBase