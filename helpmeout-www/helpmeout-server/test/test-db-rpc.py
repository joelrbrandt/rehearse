#!/user/bin/python

# test whether we can create/write to read from a pysqlite database
# from a JSON-RPC request

from jsonrpc import ServiceProxy,JSONRPCException
s = ServiceProxy("http://rehearse.stanford.edu/helpmeout/server-dev.py")

try:
    print("querying...")
    #print s.dumpdb()
    print s.errorvote(48,1)
except JSONRPCException, e:
    print repr(e.error)  
