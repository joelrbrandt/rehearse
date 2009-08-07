#hand-coded error fixes from Shiffman's appendix
import re
compilerFixes = [['''
void setup() {
	int val=5
}
''','''
void setup() {
	int val=5;
}
'''],['''
void setup() {
  if (x<5 {
    ellipse(0,0,10,10);
  }
}
''','''
void setup() {
  if (x<5) {
    ellipse(0,0,10,10);
  }
}
'''],['''
void setup() {
  for (int i=0, i<5, i++) {
    println(i);
  }
}
''','''
void setup() {
  for (int i=0; i<5; i++) {
    println(i);
  }
}
'''],['''
void setup() {
  for(int i=0; i<10; i++) {
    if(i<5) {
      line(0,i,i,0);
      println("i is greater than 5");
  }
}
''','''
void setup() {
  for(int i=0; i<10; i++) {
    if(i<5) {
      line(0,i,i,0);
      println("i is greater than 5");
    }
  }
}
'''],['''
void setup() {
  myVar = 10;
}
''','''
void setup() {
  int myVar = 10;
}
'''],['''
void setup() {
  if(mousePressed) {
    int myVar = 10;
  }
  ellipse(myVar,10,10,10);
}
''','''
void setup() {
  int myVar = 0;
  if(mousePressed) {
    myVar = 10;
  }
  ellipse(myVar,10,10,10);
}
'''],['''
void setup() {
  int myVar = 10;
}
void draw() {
  ellipse(myVar,10,10,10);
}
''','''
int myVar = 0;
void setup() {
  myVar = 10;
}
void draw() {
  ellipse(myVar,10,10,10);
}
'''],['''
void setup() {
  myVar[0] = 10;
}
''','''
void setup() {
  int myVar[] = new int[5];
  myVar[0] = 10;
}
'''],['''
void setup() {
  int myVar;
  line(0, myVar, 0, 0);
}
''','''
void setup() {
  int myVar = 10;
  line(0, myVar, 0, 0);
}
'''],['''
void setup() {
  int [] myArray;
  myArray[0] = 10;
}
''','''
void setup() {
  int [] myArray = new int[3];
  myArray[0] = 10;
}
'''],['''
void setup() {
  intt myVar = 10;
}
''','''
void setup() {
  int myVar = 10;
}
'''],['''
void setup() {
  Thing myThing = new Thing();
}
''','''
class Thing {
  Thing(){}
}
void setup() {
  Thing myThing = new Thing();
}
'''],['''
void setup() {
  Capture video = new Capture(this,320,240,30);
}
''','''
import processing.video.*;
void setup() {
  Capture video = new Capture(this,320,240,30);
}
'''],['''
void setup() {
  ellipse(100,100,50);
}
''','''
void setup() {
  ellipse(100,100,50,50);
}
'''],['''
void setup() {
  ellipse(100,100,50,"Wrong type of argument");
}
''','''
void setup() {
  ellipse(100,100,50,50);
}
'''],['''
void setup() {
  elipse(100,100,50,50);
}
''','''
void setup() {
  ellipse(100,100,50,50);
}
'''],['''
void setup() {
  functionCompletelyMadeUp();
}
''','''
void setup() {
  myFunction();
}
void myFunction(){}
'''],['''
import processing.video.*;
void setup() {
  Capture video = new Capture (this,320,240,30);
  video.turnPurple();
}
''','''
import processing.video.*;
void setup() {
  Capture video = new Capture (this,320,240,30);
  //video.turnPurple(); turn purple doesn't exist
}
''']]

runtimeFixes = [['''
int [] myArray = new int[10];
void setup() {
  myArray[-1]=0;
}
''','''
int [] myArray = new int[10];
void setup() {
  myArray[1]=0;
}
'''],['''
int [] myArray = new int[10];
void setup() {
  myArray[10]=0;
}
''','''
int [] myArray = new int[10];
void setup() {
  myArray[9]=0; //int[10] goes from 0..9
}
'''],['''
int[] myArray = new int[100];
void setup(){}
void draw(){
  myArray[mouseX] = 0;
}
''','''
int[] myArray = new int[100];
void setup(){}
void draw(){
  int index = constrain(mouseX,0,myArray.length-1);
  myArray[index] = 0;
}
'''],['''
int[] myArray = new int[100];
void setup(){
  for(int i=0; i<200; i++) {
    myArray[i] = 0;
  }
}
''','''
int[] myArray = new int[100];
void setup(){
  for(int i=0; i<myArray.length; i++) {
    myArray[i] = 0;
  }
}
'''],['''
int[] myArray = new int[100];
void setup(){
  for(int i=0; i<200; i++) {
    myArray[i] = 0;
  }
}
''','''
int[] myArray = new int[100];
void setup(){
  for(int i=0; i<200; i++) {
    if(i<myArray.length) {
      myArray[i] = 0;
    }
  }
}
'''],['''
class Thing {
  public Thing() { super(); }
  public void display(){}
}
Thing thing;
void setup() {
}
void draw() {
  thing.display();
}
''','''
class Thing {
  public Thing() { super(); }
  public void display(){}
}
Thing thing;
void setup() {
  thing = new Thing();
}
void draw() {
  thing.display();
}
'''],['''
class Thing {
  public Thing() { super(); }
  public void display(){}
}
Thing thing;
void setup() {
  Thing thing = new Thing();
}
void draw() {
  thing.display();
}
''','''
class Thing {
  public Thing() { super(); }
  public void display(){}
}
Thing thing;
void setup() {
  thing = new Thing();
}
void draw() {
  thing.display();
}
'''],['''
class Thing {
  public Thing() { super(); }
  public void display(){}
}
Thing[] things = new Thing[10];
void setup(){
}
void draw(){
  for (int i=0; i<10; i++) {
    things[i].display();
  }
}
''','''
class Thing {
  public Thing() { super(); }
  public void display(){}
}
Thing[] things = new Thing[10];
void setup(){
  for (int i=0; i<10; i++) {
    things[i] = new Thing();
  }
}
void draw(){
  for (int i=0; i<10; i++) {
    things[i].display();
  }
}
'''],['''
int [] myArray;
void setup() {
  myArray[0] = 5;
}
''','''
int [] myArray = new int[3];
void setup() {
  myArray[0] = 5;
}
''']]

#for f in compilerFixes:
#  print 'fixes.add(new Fix("%s","%s"));' % (f[0].replace("\n","\\n").replace('"','\\"'),f[1].replace("\n","\\n").replace('"','\\"'))

for f in runtimeFixes:
   print 'fixes.add(new Fix("%s","%s"));' % (f[0].replace("\n","\\n").replace('"','\\"'),f[1].replace("\n","\\n").replace('"','\\"'))
   