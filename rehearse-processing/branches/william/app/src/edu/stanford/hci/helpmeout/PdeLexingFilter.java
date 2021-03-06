package edu.stanford.hci.helpmeout;

import java.util.ArrayList;
import java.util.List;

import processing.app.preproc.PdeLexer;
import antlr.TokenStream;

public class PdeLexingFilter extends TokenStreamModifyingFilter {

  public PdeLexingFilter(TokenStream arg0) {
    super(arg0);
  //construct a filter - see PreProcessor.java and our server code
    //#if it's a name, literal or comment abstract it, otherwise write it
    //skip " " and "\t" whitespace
    //discardIfMatches(PdeLexer.WS,"[\t ]*");
    //discard(PdeLexer.SL_COMMENT);
    //discard(PdeLexer.ML_COMMENT); 
    
    modify(PdeLexer.NUM_INT, "NI");
    modify(PdeLexer.NUM_FLOAT, "NF");
    modify(PdeLexer.NUM_DOUBLE, "ND");
    modify(PdeLexer.NUM_LONG,"NL");
    modify(PdeLexer.STRING_LITERAL, "LS");
    modify(PdeLexer.CHAR_LITERAL, "LC");
    
    // we want to keep all the built-in identifiers in place
    List<String> keepIdentifiers = new ArrayList<String>();
    //autogenerated by python script
    keepIdentifiers.add("delay");
    keepIdentifiers.add("draw");
    keepIdentifiers.add("exit");
    keepIdentifiers.add("loop");
    keepIdentifiers.add("noLoop");
    keepIdentifiers.add("popStyle");
    keepIdentifiers.add("pushStyle");
    keepIdentifiers.add("redraw");
    keepIdentifiers.add("setup");
    keepIdentifiers.add("size");
    keepIdentifiers.add("cursor");
    keepIdentifiers.add("frameRate");
    keepIdentifiers.add("noCursor");
    keepIdentifiers.add("binary");
    keepIdentifiers.add("boolean");
    keepIdentifiers.add("byte");
    keepIdentifiers.add("char");
    keepIdentifiers.add("float");
    keepIdentifiers.add("hex");
    keepIdentifiers.add("int");
    keepIdentifiers.add("str");
    keepIdentifiers.add("unbinary");
    keepIdentifiers.add("unhex");
    keepIdentifiers.add("join");
    keepIdentifiers.add("match");
    keepIdentifiers.add("matchAll");
    keepIdentifiers.add("nf");
    keepIdentifiers.add("nfc");
    keepIdentifiers.add("nfp");
    keepIdentifiers.add("nfs");
    keepIdentifiers.add("split");
    keepIdentifiers.add("splitTokens");
    keepIdentifiers.add("trim");
    keepIdentifiers.add("append");
    keepIdentifiers.add("arrayCopy");
    keepIdentifiers.add("concat");
    keepIdentifiers.add("expand");
    keepIdentifiers.add("reverse");
    keepIdentifiers.add("shorten");
    keepIdentifiers.add("sort");
    keepIdentifiers.add("splice");
    keepIdentifiers.add("subset");
    keepIdentifiers.add("for");
    keepIdentifiers.add("while");
    keepIdentifiers.add("switch");
    keepIdentifiers.add("arc");
    keepIdentifiers.add("ellipse");
    keepIdentifiers.add("line");
    keepIdentifiers.add("point");
    keepIdentifiers.add("quad");
    keepIdentifiers.add("rect");
    keepIdentifiers.add("triangle");
    keepIdentifiers.add("bezier");
    keepIdentifiers.add("bezierDetail");
    keepIdentifiers.add("bezierPoint");
    keepIdentifiers.add("bezierTangent");
    keepIdentifiers.add("curve");
    keepIdentifiers.add("curveDetail");
    keepIdentifiers.add("curvePoint");
    keepIdentifiers.add("curveTangent");
    keepIdentifiers.add("curveTightness");
    keepIdentifiers.add("box");
    keepIdentifiers.add("sphere");
    keepIdentifiers.add("sphereDetail");
    keepIdentifiers.add("ellipseMode");
    keepIdentifiers.add("noSmooth");
    keepIdentifiers.add("rectMode");
    keepIdentifiers.add("smooth");
    keepIdentifiers.add("strokeCap");
    keepIdentifiers.add("strokeJoin");
    keepIdentifiers.add("strokeWeight");
    keepIdentifiers.add("beginShape");
    keepIdentifiers.add("beginVertex");
    keepIdentifiers.add("curveVertex");
    keepIdentifiers.add("endShape");
    keepIdentifiers.add("texture");
    keepIdentifiers.add("textureMode");
    keepIdentifiers.add("vertex");
    keepIdentifiers.add("loadShape");
    keepIdentifiers.add("shape");
    keepIdentifiers.add("shapeMode");
    keepIdentifiers.add("mouseClicked");
    keepIdentifiers.add("mouseDragged");
    keepIdentifiers.add("mouseMoved");
    keepIdentifiers.add("mousePressed");
    keepIdentifiers.add("mouseReleased");
    keepIdentifiers.add("keyPressed");
    keepIdentifiers.add("keyReleased");
    keepIdentifiers.add("keyTyped");
    keepIdentifiers.add("createInput");
    keepIdentifiers.add("loadBytes");
    keepIdentifiers.add("loadStrings");
    keepIdentifiers.add("open");
    keepIdentifiers.add("selectFolder");
    keepIdentifiers.add("selectInput");
    keepIdentifiers.add("link");
    keepIdentifiers.add("param");
    keepIdentifiers.add("status");
    keepIdentifiers.add("day");
    keepIdentifiers.add("hour");
    keepIdentifiers.add("millis");
    keepIdentifiers.add("minute");
    keepIdentifiers.add("month");
    keepIdentifiers.add("second");
    keepIdentifiers.add("year");
    keepIdentifiers.add("print");
    keepIdentifiers.add("println");
    keepIdentifiers.add("save");
    keepIdentifiers.add("saveFrame");
    keepIdentifiers.add("beginRaw");
    keepIdentifiers.add("beginRecord");
    keepIdentifiers.add("createOutput");
    keepIdentifiers.add("createReader");
    keepIdentifiers.add("createWriter");
    keepIdentifiers.add("endRaw");
    keepIdentifiers.add("endRecord");
    keepIdentifiers.add("saveBytes");
    keepIdentifiers.add("saveStream");
    keepIdentifiers.add("saveStrings");
    keepIdentifiers.add("selectOutput");
    keepIdentifiers.add("applyMatrix");
    keepIdentifiers.add("popMatrix");
    keepIdentifiers.add("printMatrix");
    keepIdentifiers.add("pushMatrix");
    keepIdentifiers.add("resetMatrix");
    keepIdentifiers.add("rotate");
    keepIdentifiers.add("rotateX");
    keepIdentifiers.add("rotateY");
    keepIdentifiers.add("rotateZ");
    keepIdentifiers.add("scale");
    keepIdentifiers.add("translate");
    keepIdentifiers.add("ambientLight");
    keepIdentifiers.add("directionalLight");
    keepIdentifiers.add("lightFalloff");
    keepIdentifiers.add("lightSpecular");
    keepIdentifiers.add("lights");
    keepIdentifiers.add("noLights");
    keepIdentifiers.add("normal");
    keepIdentifiers.add("pointLight");
    keepIdentifiers.add("spotLight");
    keepIdentifiers.add("beginCamera");
    keepIdentifiers.add("camera");
    keepIdentifiers.add("endCamera");
    keepIdentifiers.add("frustum");
    keepIdentifiers.add("ortho");
    keepIdentifiers.add("perspective");
    keepIdentifiers.add("printCamera");
    keepIdentifiers.add("printProjection");
    keepIdentifiers.add("modelX");
    keepIdentifiers.add("modelY");
    keepIdentifiers.add("modelZ");
    keepIdentifiers.add("screenX");
    keepIdentifiers.add("screenY");
    keepIdentifiers.add("screenZ");
    keepIdentifiers.add("ambient");
    keepIdentifiers.add("emissive");
    keepIdentifiers.add("shininess");
    keepIdentifiers.add("specular");
    keepIdentifiers.add("background");
    keepIdentifiers.add("colorMode");
    keepIdentifiers.add("fill");
    keepIdentifiers.add("noFill");
    keepIdentifiers.add("noStroke");
    keepIdentifiers.add("stroke");
    keepIdentifiers.add("alpha");
    keepIdentifiers.add("blendColor");
    keepIdentifiers.add("blue");
    keepIdentifiers.add("brightness");
    keepIdentifiers.add("color");
    keepIdentifiers.add("green");
    keepIdentifiers.add("hue");
    keepIdentifiers.add("lerpColor");
    keepIdentifiers.add("red");
    keepIdentifiers.add("saturation");
    keepIdentifiers.add("createImage");
    keepIdentifiers.add("image");
    keepIdentifiers.add("imageMode");
    keepIdentifiers.add("loadImage");
    keepIdentifiers.add("noTint");
    keepIdentifiers.add("requestImage");
    keepIdentifiers.add("tint");
    keepIdentifiers.add("blend");
    keepIdentifiers.add("copy");
    keepIdentifiers.add("filter");
    keepIdentifiers.add("get");
    keepIdentifiers.add("loadPixels");
    keepIdentifiers.add("set");
    keepIdentifiers.add("updatePixels");
    keepIdentifiers.add("createGraphics");
    keepIdentifiers.add("hint");
    keepIdentifiers.add("createFont");
    keepIdentifiers.add("loadFont");
    keepIdentifiers.add("text");
    keepIdentifiers.add("textFont");
    keepIdentifiers.add("textAlign");
    keepIdentifiers.add("textLeading");
    keepIdentifiers.add("textMode");
    keepIdentifiers.add("textSize");
    keepIdentifiers.add("textWidth");
    keepIdentifiers.add("textAscent");
    keepIdentifiers.add("textDescent");
    keepIdentifiers.add("abs");
    keepIdentifiers.add("ceil");
    keepIdentifiers.add("constrain");
    keepIdentifiers.add("dist");
    keepIdentifiers.add("exp");
    keepIdentifiers.add("floor");
    keepIdentifiers.add("lerp");
    keepIdentifiers.add("log");
    keepIdentifiers.add("mag");
    keepIdentifiers.add("map");
    keepIdentifiers.add("max");
    keepIdentifiers.add("min");
    keepIdentifiers.add("norm");
    keepIdentifiers.add("pow");
    keepIdentifiers.add("round");
    keepIdentifiers.add("sq");
    keepIdentifiers.add("sqrt");
    keepIdentifiers.add("acos");
    keepIdentifiers.add("asin");
    keepIdentifiers.add("atan");
    keepIdentifiers.add("atan2");
    keepIdentifiers.add("cos");
    keepIdentifiers.add("degrees");
    keepIdentifiers.add("radians");
    keepIdentifiers.add("sin");
    keepIdentifiers.add("tan");
    keepIdentifiers.add("noise");
    keepIdentifiers.add("noiseDetail");
    keepIdentifiers.add("noiseSpeed");
    keepIdentifiers.add("random");
    keepIdentifiers.add("randomSeed");
    keepIdentifiers.add("String");
    keepIdentifiers.add("PShape");
    keepIdentifiers.add("PImage");
    keepIdentifiers.add("PGraphics");
    keepIdentifiers.add("PFont");
    keepIdentifiers.add("PVector");
    keepIdentifiers.add("PrintWriter");
    keepIdentifiers.add("HALF_PI");
    keepIdentifiers.add("PI");
    keepIdentifiers.add("QUARTER_PI");
    keepIdentifiers.add("TWO_PI");
    keepIdentifiers.add("focused");
    keepIdentifiers.add("frameCount");
    keepIdentifiers.add("frameRate");
    keepIdentifiers.add("height");
    keepIdentifiers.add("online");
    keepIdentifiers.add("screen");
    keepIdentifiers.add("width");
    keepIdentifiers.add("mouseButton");
    keepIdentifiers.add("mousePressed");
    keepIdentifiers.add("mouseX");
    keepIdentifiers.add("mouseY");
    keepIdentifiers.add("pmouseX");
    keepIdentifiers.add("pmouseY");
    keepIdentifiers.add("key");
    keepIdentifiers.add("keyCode");
    keepIdentifiers.add("keyPressed");
    keepIdentifiers.add("pixels");

    
    modifyIfNotInList(PdeLexer.IDENT, "ID",keepIdentifiers);
    
    //TODO: add all the other stuff
    
    
  }

}
