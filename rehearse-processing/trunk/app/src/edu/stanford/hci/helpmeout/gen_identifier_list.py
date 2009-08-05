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

consts = ['HALF_PI','PI','QUARTER_PI','TWO_PI']

vars = ['focused','frameCount','frameRate','height','online','screen','width',
'mouseButton','mousePressed','mouseX','mouseY','pmouseX','pmouseY',
'key','keyCode','keyPressed','pixels',
]

for s in functions+types+consts+vars:
    print 'keepIdentifiers.add("'+s+'");'