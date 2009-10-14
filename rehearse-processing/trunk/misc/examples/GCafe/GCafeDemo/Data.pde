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

class Data {
      
  String[] columnNames;
  float[][] columns;
  float[] minYears;
  
  float entryAt(int year, int column) {
    return columns[column][year - 1940];
  }
  
  int minYear(int column) {
    for (int i = columns[column].length - 1; i >= 0; i--) {
      if (Float.isNaN(columns[column][i])) {
        return 1940 + i + 1;
      }
    }
    return 1940;
  }
  
  Data() {
    println("reading data");
    String[] lines = loadStrings("stats.txt");
    columnNames = split(lines[0], '\t');
    columns = new float[columnNames.length][lines.length-1];
    println(columnNames);
    for (int i = 1; i < lines.length; ++i) {
      String[] thisLine = split(lines[i], '\t');
      for (int j=0; j < thisLine.length; ++j) {
        int yearIndex = lines.length - 1 - i;
        if (thisLine[j].equals("")) {
           columns[j][yearIndex] = Float.NaN;
           // println("" + i + " " + j + ": parsed empty");
        }
        else {
           columns[j][yearIndex] = Float.parseFloat(thisLine[j]);
           // println("" + i + " " + j + ": parsed " + columns[j][i-1]);
        }
      }
    }
  } 
  
}
