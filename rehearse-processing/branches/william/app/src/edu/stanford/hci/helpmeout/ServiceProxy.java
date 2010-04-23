package edu.stanford.hci.helpmeout;

//this code is (c) kerbtier
//@see http://code.google.com/p/jj1/
//package com.googlecode.jj1;
//moved into this package for convenience reasons so we don't have to change our entire build script

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.stringtree.json.ExceptionErrorListener;
import org.stringtree.json.JSONReader;
import org.stringtree.json.JSONValidatingReader;
import org.stringtree.json.JSONValidatingWriter;
import org.stringtree.json.JSONWriter;

public class ServiceProxy {
  private static int gid = 100;
  private String url;
  private String name;

  public ServiceProxy(String url, String name) {
    this.name = name;
    this.url = url;
  }

  public ServiceProxy(String url) {
    this(url, null);
  }

  public ServiceProxy get(String property) {
    return new ServiceProxy(url, name == null ? property : name + "." + property);
  }

  public Object call(String mName, Object... parameters) throws JsonRpcException {
    return get(mName).execute(parameters);
  }

  public Object execute(Object... parameters) throws JsonRpcException {
    try {
      Map<String, Object> values = new HashMap<String, Object>();
      values.put("method", name);
      values.put("params", parameters);
      values.put("id", "" + gid++);

      String content = null;
      ;
      try {
        JSONWriter writer = new JSONValidatingWriter(new ExceptionErrorListener());
        content = writer.write(values);
      } catch (NullPointerException e) {
        throw new JsonRpcException("cannot encode object to json", e);
      }

      URLConnection connection = new URL(url).openConnection();
      connection.setRequestProperty("method", "POST");
      connection.setUseCaches(false);
      connection.setDoInput(true);
      connection.setDoOutput(true);

      OutputStream out = connection.getOutputStream();
      out.write(content.getBytes("utf-8"));
      out.close();

      connection.connect();

      InputStream in = connection.getInputStream();
      BufferedReader i = new BufferedReader(new InputStreamReader(in, "utf-8"));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = i.readLine()) != null) {
        sb.append(line);
        sb.append("\n");
      }
      in.close();

      Map<String, Object> result = null;
      try {
        JSONReader reader = new JSONValidatingReader(new ExceptionErrorListener());
        result = (Map<String, Object>) reader.read(sb.toString());
      } catch (Exception e) {
        throw new JsonRpcException("cannot decode json", e);
      }
      
      if (result.get("error") != null) {
        throw new JsonRpcException(result.get("error").toString());
      }

      return result.get("result");
    } catch (JsonRpcException e) {
      throw e;
    } catch (Exception e) {
      //e.printStackTrace();
      throw new JsonRpcException(e); //changed, @see http://code.google.com/p/jj1/issues/detail?id=2
    }
  }
}
