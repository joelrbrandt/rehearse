//
// Data column names
//
static final int USA_WINE_CONSUME_PER_CAPITA = 1;
static final int USA_WINE_CONSUME_TOTAL = 2;
static final int CA_WINE_PROD = 3;
static final int USA_WINE_PROD = 4;
static final int CA_GRAPE_CRUSH = 5;
static final int USA_GRAPE_CRUSH = 6;
static final int CA_WINE_SHIPMENT_TOTAL = 7;
static final int CA_WINE_SHIPMENT_TO_USA = 8;
static final int USA_UNEMPLOYMENT_RATE = 9;
static final int REPUBLICAN_IN_OFFICE = 10;
static final int NATIONAL_DEBT = 11;
static final int NATIONAL_DEBT_GDP_PERCENT = 12;
static final int CPI = 13;
static final int USA_TRAFFIC_DEATHS = 14;

//
// Global variables
//
Data data;
int currColumn;
int minYear, maxYear;
float minVal, maxVal;

// "Magic" setup function -- this runs once at start of sketch
void setup() {
  data = new Data();
  currColumn = CPI;
  
  size(640, 640);
  background(32);
  
  minYear = data.minYear(currColumn);
  maxYear = data.maxYear(currColumn);
  minVal = data.minValue(currColumn);
  maxVal = data.maxValue(currColumn);
}

// "Magic" draw() function -- this is called continuously as sketch executes
void draw() {
  int gap = 2;
  int rowStep = height / (maxYear - minYear + 1);
  int rowWidth = rowStep - gap;
  int y = gap / 2;
  
  for (int currYear = minYear; currYear <= maxYear; ++currYear) {
    fill(0, 125, 255);
    stroke(255, 255, 255);
    
    int rowLength = int( (width *.98) * data.getValue(currColumn, currYear) / maxVal);
    
    rect(0, y, rowLength, rowWidth);
    y+= rowStep;
  }
}

