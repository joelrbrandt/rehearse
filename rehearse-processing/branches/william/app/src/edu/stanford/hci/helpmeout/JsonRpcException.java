package edu.stanford.hci.helpmeout;

// this code is (c) kerbtier
// @see http://code.google.com/p/jj1/
// package com.googlecode.jj1;
// moved into this package for convenience reasons so we don't have to change our entire build script

public class JsonRpcException extends RuntimeException {
  public JsonRpcException(String error) {
    super(error);
  }

  public JsonRpcException(String error, Exception e) {
    super(error, e);
  }
  
  //added, see http://code.google.com/p/jj1/issues/detail?id=2
  public JsonRpcException(Throwable e) {
    super(e);
  }
}
