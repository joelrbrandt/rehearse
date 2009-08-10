# -*- coding: utf-8 -*-
"""
    Token Line Formatter - outputs token stream lines
    literals,names,and keywords are abstracted.
    
    Based on other.py NullFormatter, RawTokenFormatter which was
    :copyright: 2006-2007 by Georg Brandl, Armin Ronacher.
    :license: BSD, see LICENSE for more details.
"""

from pygments.formatter import Formatter
from pygments.token import Token
from pygments.token import STANDARD_TYPES
from pygments.token import is_token_subtype

__all__ = ['TokenLineFormatter']

class TokenLineFormatter(Formatter):
    def __init__(self,language="processing"):
        if(language=="processing"):
            functions = ['delay','draw','exit','loop','noLoop','popStyle','pushStyle','redraw','setup','size','cursor','frameRate','noCursor','binary','boolean','byte','char','float','hex','int','str','unbinary','unhex','join','match','matchAll','nf','nfc','nfp','nfs','split','splitTokens','trim',
            'append','arrayCopy','concat','expand','reverse','shorten','sort','splice','subset',
            'for','while','switch',
            'arc','ellipse','line','point','quad','rect','triangle',
            'bezier','bezierDetail','bezierPoint','bezierTangent','curve','curveDetail','curvePoint','curveTangent','curveTightness',
            'box','sphere','sphereDetail',
            'ellipseMode','noSmooth','rectMode','smooth','strokeCap','strokeJoin','strokeWeight',
            'beginShape','beginVertex','curveVertex','endShape','texture','textureMode','vertex',
            'loadShape','shape','shapeMode',
            'mouseClicked','mouseDragged','mouseMoved','mousePressed','mouseReleased',
            'keyPressed','keyReleased','keyTyped',
            'createInput','loadBytes','loadStrings','open','selectFolder','selectInput',
            'link','param','status',
            'day','hour','millis','minute','month','second','year',
            'print','println',
            'save','saveFrame',
            'beginRaw','beginRecord','createOutput','createReader','createWriter','endRaw','endRecord','saveBytes','saveStream','saveStrings','selectOutput',
            'applyMatrix','popMatrix','printMatrix','pushMatrix','resetMatrix','rotate','rotateX','rotateY','rotateZ','scale','translate',
            'ambientLight','directionalLight','lightFalloff','lightSpecular','lights','noLights','normal','pointLight','spotLight',
            'beginCamera','camera','endCamera','frustum','ortho','perspective','printCamera','printProjection',
            'modelX','modelY','modelZ','screenX','screenY','screenZ',
            'ambient','emissive','shininess','specular',
            'background','colorMode','fill','noFill','noStroke','stroke','alpha','blendColor','blue','brightness','color','green','hue','lerpColor','red','saturation',
            'createImage','image','imageMode','loadImage','noTint','requestImage','tint',
            'blend','copy','filter','get','loadPixels','set','updatePixels',
            'createGraphics','hint','createFont','loadFont','text','textFont','textAlign','textLeading','textMode','textSize','textWidth',
            'textAscent','textDescent',
            'abs','ceil','constrain','dist','exp','floor','lerp','log','mag','map','max','min','norm','pow','round','sq','sqrt',
            'acos','asin','atan','atan2','cos','degrees','radians','sin','tan','noise','noiseDetail','noiseSpeed','random','randomSeed'
            ]

            types = ['String','PShape','PImage','PGraphics','PFont','PVector','PrintWriter']

            # consts = ['HALF_PI','PI','QUARTER_PI','TWO_PI']

            variables = ['focused','frameCount','frameRate','height','online','screen','width',
            'mouseButton','mousePressed','mouseX','mouseY','pmouseX','pmouseY',
            'key','keyCode','keyPressed','pixels',
            ]
            self.keepIds = functions + types + variables
            
        elif language == "arduino":
            functions = ['abs','acos','asin','atan','atan2','ceil','constrain','cos','degrees','exp','floor','log','map','max','min','radians','random','randomSeed','round','sin','sq','sqrt','tan',
            'bitRead','bitWrite','bitSet','bitClear','bit','highByte','lowByte',
            'analogReference','analogRead','analogWrite','attachInterrupt','detachInterrupt','delay','delayMicroseconds','digitalWrite','digitalRead','interrupts','millis','micros','noInterrupts','pinMode','pulseIn','shiftOut',
            'begin','read','print','println','available','flush','setup','loop']

            types = ['Serial','boolean','byte','char','float','int','long','word']
            self.keepIds = functions+types
        else:
            self.keepIds = []
            
    """
    Output the text as a token stream, but preserving newlines
    """
    def format(self,tokensource,outfile):

        for ttype, value in tokensource:
            #if it's a comment or a literal abstract it
            if is_token_subtyte(ttype,Token.Comment) \
            or is_token_subtype(ttype,Token.Literal):
                outfile.write(STANDARD_TYPES[ttype])
            
            #if it's an identifier, keep if we know about it, otherwise abstract
            elif is_token_subtype(ttype,Token.Name) :
                # if it's a standard API identifier, keep
                if(self.keepIds.count(value)>0):
                    outfile.write(value)
                # else abstract
                else:
                    outfile.write(STANDARD_TYPES[ttype])
            
            #else, write it but discard whitespace
            else:
                outfile.write(value.strip("\t "))


