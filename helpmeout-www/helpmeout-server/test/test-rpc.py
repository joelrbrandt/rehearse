#!/user/bin/python

# Simple JSON-RPC echo test
# connect to service, call echo method
from jsonrpc import ServiceProxy,JSONRPCException
s = ServiceProxy("http://rehearse.stanford.edu/helpmeout/server.py")
print 'Calling echo()...'
try:
    print s.echo("hello, world!")
    print s.storeexception('a','b','c')
except JSONRPCException, e:
    print repr(e.error)  

