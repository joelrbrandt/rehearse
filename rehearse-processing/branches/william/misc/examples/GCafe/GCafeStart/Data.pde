class Data {
      
  String[] columnNames;
  float[][] columns;
  float[] minYears;
  
  float getValue(int column, int year) {
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
  
  int maxYear(int column) {
    return 2008;
  }
  
  float minValue(int column) {
    float result = Float.MAX_VALUE;
    for (int i = 0; i < columns[column].length; ++i) {
      if (!Float.isNaN(columns[column][i])) {
        result = min(result, columns[column][i]);
      }
    }
    return result;
  }
  
  float maxValue(int column) {
    float result = Float.MIN_VALUE;
    for (int i = 0; i < columns[column].length; ++i) {
      if (!Float.isNaN(columns[column][i])) {
        result = max(result, columns[column][i]);
      }
    }
    return result;
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
