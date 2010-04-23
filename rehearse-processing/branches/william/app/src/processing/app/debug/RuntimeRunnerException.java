package processing.app.debug;

public class RuntimeRunnerException extends RunnerException {

  public RuntimeRunnerException(String message, int file, int line, int column,
                                boolean showStackTrace) {
    super(message, file, line, column, showStackTrace);
    // TODO Auto-generated constructor stub
  }


  public RuntimeRunnerException(String message, int file, int line, int column) {
    super(message, file, line, column);
    // TODO Auto-generated constructor stub
  }


  public RuntimeRunnerException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }

}
