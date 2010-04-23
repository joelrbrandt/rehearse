package bsh;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import processing.core.PApplet;

public class BSHConstructorCastExpression extends SimpleNode {
	public Type castToType = null;

	public BSHConstructorCastExpression(int id) {
		super(id);
	}

	BSHArguments getArgsNode() {
		return (BSHArguments)jjtGetChild(0);
	}

	public Object evalNode( CallStack callstack, Interpreter interpreter ) throws EvalError {
		Object result = Primitive.NULL;
		
		BSHArguments a = getArgsNode();
		// System.out.println(a);

		Object[] evaledArgs = new Object[a.children.length];
		for (int i = 0; i < evaledArgs.length; ++i) {
			if (a.children[i] instanceof SimpleNode) {
				evaledArgs[i] = ((SimpleNode) a.children[i]).eval(callstack, interpreter);
				// System.out.println(evaledArgs[i]);
			} else {
				throw new EvalError("ConstructorCast Evaluation: Error evaluating argument " + (i+1), this, callstack);
			}
		}

		Class[] types = new Class[evaledArgs.length];
		Object[] values = new Object[evaledArgs.length];

		for (int i = 0; i < evaledArgs.length; ++i) {
			if (evaledArgs[i] instanceof Primitive) {
				types[i] = ((Primitive) evaledArgs[i]).getType();
				values[i] = ((Primitive) evaledArgs[i]).getValue();
			} else {
				types[i] = evaledArgs[i].getClass();
				values[i] = evaledArgs[i];
			}
		}


		try {
			if (castToType instanceof Class) {
				String typeName = ((Class) castToType).getName();
				String capitalizedTypeName = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);

				String methodName = "parse" + capitalizedTypeName;

				Method m = PApplet.class.getMethod(methodName, types);
				if (m != null) {
					result = m.invoke(this, values);
					// result = new Primitive(resultVal);
				}

			} else {
				throw new EvalError("ConstructorCast Evaluation: castToType wasn't a class", this, callstack);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new EvalError("ConstructorCast Evaluation: types of arguments did not match a parser", this, callstack);
		}

		// if we got a primitive object that's not wrapped in a Primitive, wrap it
		// wrap just returns the object if the object isn't a Primitive
		result = Primitive.wrap(result, result.getClass());
		
		return result;
	}

}
