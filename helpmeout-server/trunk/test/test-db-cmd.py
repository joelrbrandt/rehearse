#!/usr/bin/python

# test pysqlite db
# run from command line to test whether we can create/write to
# read from a database

from pysqlite2 import dbapi2 as sqlite

def connect():
    con = sqlite.connect("/project/helpmeout-server/db/helpmeout.sqlite")
    con.execute('create table if not exists errs(errmsg,diff)')
    return con
con = connect()
cur = con.cursor()
cur.execute("insert into errs values ('test','test')")
con.commit()
res = con.execute("select * from errs")
print [r for r in res]
