#!/usr/bin/python

# Simple JSON-RPC echo service
# pass a string in, get it back out

from jsonrpc import ServiceMethod

class MyService(object):
    @ServiceMethod
    def echo(self, msg):
        return msg

service = MyService()
