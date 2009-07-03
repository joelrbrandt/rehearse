#!/usr/bin/python

# test pysqlite db
# run from command line to test whether we can create/write to
# read from a database

from pysqlite2 import dbapi2 as sqlite


con = sqlite.connect("/project/helpmeout-server/db/helpmeout.sqlite")
cur = con.cursor()

print "---------------------------------------------------------------"
res = con.execute("select count(*) from compilererrors")
print ("table compilererrors has %d entries" % [r[0] for r in res][0])

print "---------------------------------------------------------------"
print "last entry:"
res = con.execute("select * from compilererrors order by id desc limit 1")
print ([r for r in res])

print "---------------------------------------------------------------"
res = con.execute("select count(*) from exceptions")
print ("table exceptions has %d entries" % [r[0] for r in res][0])


print "---------------------------------------------------------------"
print "last entry:"
res = con.execute("select * from exceptions order by id desc limit 1")
print ([r for r in res])
