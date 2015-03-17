# Introduction #
It may be necessary to integrate an external library into the Rehearse build. You'll have to add the library to:
  1. The Eclipse project
  1. The make, run and dist scripts for each platform
  1. Windows: config.xml for launcher; OS X: Info.plist in Processing.app

First, decide where the jar files will live. For jars that have to do with the PDE, rehearse/app/lib is a good location. In the following example, assume our jar file is rehearse/app/lib/jj1.0.1.jar

## Change Eclipse .classpath file ##
Either go through the GUI: right click project name, Properties, Java Build Path, Add JARs...
Or edit .classpath and add
```
<classpathentry kind="lib" path="lib/jj1.0.1.jar"/>
```

## Change make,run,dist scripts ##
The files live in rehearse/build/[platformname](platformname.md)/.
For example:
```
rehease/build/windows/make.sh
rehease/build/windows/run.sh
rehease/build/windows/dist.sh
```

**make.sh**
Steps:
  1. copy jars into work directory
  1. add jars to classpath
  1. if you have any new source packages, add to list of files to compile

[Here's a diff that shows necessary changes](http://code.google.com/p/rehearse/source/diff?spec=svn92&r=87&format=side&path=/rehearse-processing/trunk/build/windows/make.sh&old_path=/rehearse-processing/trunk/build/windows/make.sh&old=52)

**run.sh**
Steps:
  1. add jars to classpath
[relevant diff](http://code.google.com/p/rehearse/source/diff?spec=svn92&r=91&format=side&path=/rehearse-processing/trunk/build/windows/run.sh&old_path=/rehearse-processing/trunk/build/windows/run.sh&old=52)

**dist.sh**
Steps:
  1. copy jars to right place
[relevant diff](http://code.google.com/p/rehearse/source/diff?path=/rehearse-processing/trunk/build/windows/dist.sh&format=side&r=88)

## Windows: change config.xml for windows launcher ##
Steps:
  1. add classpath entries in build/windows/launcher/config.xml
[relevant diff](http://code.google.com/p/rehearse/source/diff?spec=svn92&r=92&format=side&path=/rehearse-processing/trunk/build/windows/launcher/config.xml)

## OS X: change Info.plist in Processing.app ##
Steps:
  1. add jar to string tag in classpath node
[relevant diff](http://code.google.com/p/rehearse/source/diff?spec=svn98&r=98&format=side&path=/rehearse-processing/trunk/build/macosx/dist/Processing.app/Contents/Info.plist)