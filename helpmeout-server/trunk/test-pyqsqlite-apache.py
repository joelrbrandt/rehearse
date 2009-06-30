# test whether we can create/write to read from a pysqlite database
# from a JSON-RPC request
#
from mod_python import apache, util
from pysqlite2 import dbapi2 as sqlite
from jsonrpc import handleCGI, ServiceMethod

def connect():
    con = sqlite.connect("/project/helpmeout-server/db/helpmeout.sqlite")
    con.execute('create table if not exists errs(errmsg,diff)')
    return con

@ServiceMethod
def echo(msg):
    return msg

#query the database
@ServiceMethod
def query():
    con = connect()
    cur = con.cursor()
    res = con.execute("select * from errs")
    if res==None:
        return ['error']
    arr = [d[0] for d in res]
    if len(arr)==0:
        return ['nothing found']
    return arr
