package bsh;

import java.lang.reflect.*;

public class BSHColorMethodInvocation extends SimpleNode {
	public BSHColorMethodInvocation(int id) {
		super(id);
	}

	BSHArguments getArgsNode() {
		return (BSHArguments) jjtGetChild(0);
	}

	public Object evalNode (CallStack callstack, Interpreter interpreter) throws EvalError {
		Primitive result = Primitive.NULL;

		BSHArguments a = getArgsNode();
		// System.out.println(a);
		Primitive[] p = new Primitive[a.children.length];
		for (int i = 0; i < p.length; ++i) {
			if (a.children[i] instanceof SimpleNode) {
				Object o = ((SimpleNode) a.children[i]).eval(callstack, interpreter);
				if (!(o instanceof Primitive)) {
					o = Primitive.wrap(o, o.getClass()); // if o is a primitive type, this call will wrap it in a Primitive. Otherwise, it returns o
				}
				if (o instanceof Primitive) {
					p[i] = (Primitive) o;
				} else {
					throw new EvalError("Argument " + (i+1) + " of color constructor did not eval to a Primitive", this, callstack);
				}
				// System.out.println(o);
			} else {
				throw new EvalError("Error evaluating argument " + (i+1) + " of color constructor", this, callstack);
			}
		}

		Class[] types = new Class[p.length];
		Object[] values = new Object[p.length];
		
		
		
		for (int i = 0; i < p.length; ++i) {
			types[i] = p[i].getType();
			values[i] = p[i].getValue();
			/* used to convert doubles to floats, decided to fix the parser instead to be more consistent with processing
			if (types[i] == Double.TYPE && values[i] instanceof Double) {
				types[i] = Float.TYPE;
				values[i] = new Float(((Double) values[i]).floatValue());
			}
			*/
		}


		// for all the color constructors, either all params have to be a float or all params have to be an int
		// so, if we've got both ints and floats, we want to convert them all to ints
		
		boolean hasInt = false;
		boolean hasFloat = false;
		boolean hasSomethingElse = false;
		for (Class c : types) {
			if (c.equals(Float.TYPE))
				hasFloat = true;
			else if (c.equals(Integer.TYPE))
				hasInt = true;
			else
				hasSomethingElse = true;
		}
		
		if (hasInt && hasFloat && !hasSomethingElse) {
			for (int i = 0; i < p.length; ++i) {
				if (types[i].equals(Integer.TYPE)) {
					types[i] = Float.TYPE;
					values[i] = (float) ((Integer) values[i]); //ugh
				}
			}			
		}
		
		try {
			Method m = this.getClass().getMethod("color", types);
			if (m != null) {
				Object resultVal = m.invoke(this, values);
				result = new Primitive(resultVal);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new EvalError("Error evaluating color constructor: types of arguments did not match a constructor", this, callstack);
		}

		return result;
	}


	// JRB: these are copied from PApplet.java. They are only modified
	// slightly -- any dependence on a 'Graphics' object (g) was removed
	// they were also made static -- no idea why they AREN'T static in
	// PApplet

	//////////////////////////////////////////////////////////////
	// COLOR FUNCTIONS

	public static final int color(int gray) {
		if (gray > 255) gray = 255; else if (gray < 0) gray = 0;

		return 0xff000000 | (gray << 16) | (gray << 8) | gray;
	}


	public static final int color(float fgray) {
		int gray = (int) fgray;
		if (gray > 255) gray = 255; else if (gray < 0) gray = 0;

		return 0xff000000 | (gray << 16) | (gray << 8) | gray;
	}


	/**
	 * As of 0116 this also takes color(#FF8800, alpha)
	 */
	public static final int color(int gray, int alpha) {
		if (alpha > 255) alpha = 255; else if (alpha < 0) alpha = 0;
		if (gray > 255) {
			// then assume this is actually a #FF8800
			return (alpha << 24) | (gray & 0xFFFFFF);
		} else {
			//if (gray > 255) gray = 255; else if (gray < 0) gray = 0;
			return (alpha << 24) | (gray << 16) | (gray << 8) | gray;
		}
	}


	public static final int color(float fgray, float falpha) {
		int gray = (int) fgray;
		int alpha = (int) falpha;
		if (gray > 255) gray = 255; else if (gray < 0) gray = 0;
		if (alpha > 255) alpha = 255; else if (alpha < 0) alpha = 0;

		return 0xff000000 | (gray << 16) | (gray << 8) | gray;
	}


	public static final int color(int x, int y, int z) {
		if (x > 255) x = 255; else if (x < 0) x = 0;
		if (y > 255) y = 255; else if (y < 0) y = 0;
		if (z > 255) z = 255; else if (z < 0) z = 0;

		return 0xff000000 | (x << 16) | (y << 8) | z;
	}


	public static final int color(float x, float y, float z) {
		if (x > 255) x = 255; else if (x < 0) x = 0;
		if (y > 255) y = 255; else if (y < 0) y = 0;
		if (z > 255) z = 255; else if (z < 0) z = 0;

		return 0xff000000 | ((int)x << 16) | ((int)y << 8) | (int)z;
	}


	public static final int color(int x, int y, int z, int a) {
		if (a > 255) a = 255; else if (a < 0) a = 0;
		if (x > 255) x = 255; else if (x < 0) x = 0;
		if (y > 255) y = 255; else if (y < 0) y = 0;
		if (z > 255) z = 255; else if (z < 0) z = 0;

		return (a << 24) | (x << 16) | (y << 8) | z;
	}


	public static final int color(float x, float y, float z, float a) {
		if (a > 255) a = 255; else if (a < 0) a = 0;
		if (x > 255) x = 255; else if (x < 0) x = 0;
		if (y > 255) y = 255; else if (y < 0) y = 0;
		if (z > 255) z = 255; else if (z < 0) z = 0;

		return ((int)a << 24) | ((int)x << 16) | ((int)y << 8) | (int)z;
	}

	// End code copied from PApplet

}
