package edu.stanford.hci.processing;

import javax.swing.JTextArea;

import bsh.EvalError;
import bsh.Interpreter;

public class ExecutionTask implements Runnable {

	private Interpreter interpreter;
	private String source;
	private JTextArea output;
	
	public ExecutionTask(Interpreter interpreter, String source, 
			JTextArea output) {
		this.interpreter = interpreter;
		this.source = source;
		this.output = output;
	}
	
	public void run() {
		output.setText("");
		try {	
			Object obj = interpreter.eval(source);
			if (obj != null)
				output.append(obj.toString());
			
		} catch (EvalError e) {
			output.append(e.toString());
			e.printStackTrace();
		}
		output.append("\n + Line numbers executed:");
		// we're not using this class anymore, won't bother fixing this.
//		for (Integer i : interpreter.getLineNumberSet()) {
//			output.append(" " + i);
//		}
	}

}
