#!/user/bin/python

# Simple JSON-RPC echo test
# connect to service, call echo method
from jsonrpc import ServiceProxy,JSONRPCException
s = ServiceProxy("http://rehearse.stanford.edu/helpmeout/echo.py")
print 'Calling echo()...'
try:
    print s.echo("hello, world!")
except JSONRPCException, e:
    print repr(e.error)  

