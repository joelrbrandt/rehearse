#!/usr/bin/env python
# test script to generate a html diff table from two string arrays
import difflib

before = ['//a comment\n','int x[]=new int[];\n','//something else\n']
after = ['//a comment\n','int x[]=new int[5];\n','//something else\n','totally different\n']

d = difflib.HtmlDiff()
# make_table does not include necessary CSS styles- have to copy manually
html = d.make_file(before,after,"Before (Broken)","After (Fixed)",context=True,numlines=0)
f= open("out.html","w")
f.write(html)
f.close()
