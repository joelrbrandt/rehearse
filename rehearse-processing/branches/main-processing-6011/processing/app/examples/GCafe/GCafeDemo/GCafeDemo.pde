Data data;
int currYear;
int minYear;
int currColumn;

void setup() {
  data = new Data();
  size(400, 400);
  background(0);
  
  currColumn = USA_WINE_PROD;
  currYear = minYear = data.minYear(currColumn);
}

void draw() {
  if (currYear == 2008) return;
  fill(0, 125, 255);
  stroke(255, 255, 255);
  rect(5 + 30 * (currYear - minYear), height, 25, 
      -height * data.entryAt(currYear, currColumn) / 1000000000.0);
  currYear++;
  delay(50);
}

