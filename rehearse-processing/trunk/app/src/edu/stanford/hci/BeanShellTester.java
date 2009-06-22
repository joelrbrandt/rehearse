package edu.stanford.hci;

import java.awt.Point;

import bsh.*;

public class BeanShellTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Point p = new Point(2,3);
		p.x = 3;
		p.x += 1.0f;
		System.out.println("p.x: " + p.x);
		Interpreter i = new Interpreter();
		try {
			i.eval("class Foo { static Bar b; }");
		} catch (EvalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bar b = new Bar();
		Foo f = new Foo();
		Foo.b = b;
		
	}
	
	static class Foo {
		static Bar b;
	}

	static class Bar {
		static Foo f;
	}
}



