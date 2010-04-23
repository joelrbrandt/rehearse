package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.MainClassAdapter;

import processing.app.EditorConsole;
import processing.app.debug.RunnerException;
import processing.app.preproc.PdeTokenTypes;
import processing.app.syntax.JEditTextArea;
import antlr.Token;
import antlr.TokenStreamException;
import edu.stanford.hci.helpmeout.HelpMeOut;
import edu.stanford.hci.helpmeout.HelpMeOutLog;
import edu.stanford.hci.helpmeout.PdeMatchProcessor;
import edu.stanford.hci.helpmeout.HelpMeOutPreferences.Usage;
import edu.stanford.hci.processing.RehearseBase;
import edu.stanford.hci.processing.editor.RehearseEditor;

public class SeedDatabaseAuto extends UISpecTestCase {

	private Window window;

	protected void setUp() throws Exception {
		setAdapter(new MainClassAdapter(RehearseBase.class, new String[0]));
		window = getMainWindow();
	}

	public void testRun() throws IOException, TokenStreamException {
		assertTrue(window != null);
		final RehearseEditor editor = (RehearseEditor)window.getAwtContainer();
		EditorConsole.setEditor(editor);
		HelpMeOut.getInstance().updatePreferences(Usage.QUERY_AND_SUBMIT, false);//change for a dryrun


		seedDataBaseWithSyntaxFixes(editor);
	}

	private  void seedDataBaseWithSyntaxFixes(RehearseEditor editor) throws IOException, TokenStreamException {
		List<String> errors = new ArrayList<String>();
		List<String> pdeFiles = getPdeList();
		int errorsPerFile = 10;
		for(String fileToOpen:pdeFiles) { //pdeFiles.subList(0,max)

			//String fileToOpen = pdeFiles.get((int)(Math.random()*pdeFiles.size()));
			String content = loadFile(fileToOpen);
			
			//create n errors per file
			for(int i=0; i<errorsPerFile; i++) {
				//load the file and tokenize it
				String messedUp = modifyOneToken(content);//deleteOneToken(content);//modifyOneToken(content);//deleteOneToken(content);

				JEditTextArea textarea = editor.getTextArea();
				textarea.setText(messedUp);
				try {
					editor.getSketch().compile();
					System.out.println("sorry, messed up code still compiled. skipping.");
				} catch (RunnerException e) {
					//great, compilation broke as expected
					HelpMeOut.getInstance().processBroken(e.getMessage(),messedUp);
					//if we have an error, then re-run so we save it correctly
					
					//and submit the fixed version
					HelpMeOut.getInstance().processNoError(content);
				}
			}
		}
		System.out.println("---------------------------------");
		errors = HelpMeOutLog.getInstance().query(HelpMeOutLog.COMPILE_BROKEN);
		System.out.println("Total errors: "+errors.size());
		

		System.out.println("---------------------------------");
		//only look at error msg
		ArrayList<String> uniqueErrors = new ArrayList<String>();
		for (String error:errors) {
			String[]fields = error.split("\t");
			uniqueErrors.add(fields[3]);
		}
		Set<String> set = new HashSet<String>(uniqueErrors);
		uniqueErrors = new ArrayList<String>(set);
		System.out.println("Total unique errors: "+uniqueErrors.size());
		System.out.println("---------------------------------");

		for (String error:uniqueErrors) {
			System.out.println(error);
		}


		//}
	}

	private String modifyOneToken(String content)  throws TokenStreamException {
		PdeMatchProcessor proc = new PdeMatchProcessor();
		List<Token> tokens = proc.getUnfilteredTokenArray(content);
		
		//find a random non-ws, non-comment token
		int ttype1 = PdeTokenTypes.WS;
		int index1=0;
		while ((ttype1 == PdeTokenTypes.WS) || (ttype1 == PdeTokenTypes.SL_COMMENT) || (ttype1 == PdeTokenTypes.ML_COMMENT)) {
			index1 = (int)(Math.random()*(tokens.size()-1));
			ttype1 = tokens.get(index1).getType();
		}
		//find a random non-ws, non-comment token
		int ttype2 = PdeTokenTypes.WS;
		int index2=0;
		while ((index2==index1) || (ttype2 == PdeTokenTypes.WS) || (ttype2 == PdeTokenTypes.SL_COMMENT) || (ttype2 == PdeTokenTypes.ML_COMMENT)) {
			index2 = (int)(Math.random()*(tokens.size()-1));
			ttype2 = tokens.get(index2).getType();
		}
		
		tokens.get(index1).setText(tokens.get(index2).getText());
		String messedUp = proc.tokenArrayToString(tokens);
		return messedUp;
	}
	
	

	private String deleteOneToken(String content) throws TokenStreamException {
		PdeMatchProcessor proc = new PdeMatchProcessor();
		List<Token> tokens = proc.getUnfilteredTokenArray(content);

		//find a random non-ws, non-comment token
		int ttype = PdeTokenTypes.WS;
		int randomIndex=0;
		while ((ttype == PdeTokenTypes.WS) || (ttype == PdeTokenTypes.SL_COMMENT) || (ttype == PdeTokenTypes.ML_COMMENT)) {
			randomIndex = (int)(Math.random()*(tokens.size()-1));
			ttype = tokens.get(randomIndex).getType();
		}

		//change it to whitespace
		tokens.get(randomIndex).setType(PdeTokenTypes.WS);
		tokens.get(randomIndex).setText(" ");

		
		String messedUp = proc.tokenArrayToString(tokens);
		return messedUp;
	}
	
	
	private static String loadFile(String fileToOpen) throws IOException {

		BufferedReader in = new BufferedReader(new FileReader(fileToOpen));

		String program="";
		String str;
		while ((str = in.readLine()) != null) {
			program += str+"\n";
		}
		in.close();
		return program;
	}

	private List<String> getPdeList() {
		//autogenerated by list_pdes.py
		List<String> pdeFiles = new ArrayList<String>();

		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Arrays/Array/Array.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Arrays/Array2D/Array2D.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Arrays/ArrayObjects/ArrayObjects.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Color/Brightness/Brightness.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Color/ColorWheel/ColorWheel.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Color/Creating/Creating.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Color/Hue/Hue.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Color/LinearGradient/LinearGradient.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Color/RadialGradient/RadialGradient.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Color/Reading/Reading.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Color/Relativity/Relativity.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Color/Saturation/Saturation.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Color/WaveGradient/WaveGradient.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Control/Conditionals1/Conditionals1.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Control/Conditionals2/Conditionals2.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Control/EmbeddedIteration/EmbeddedIteration.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Control/Iteration/Iteration.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Control/LogicalOperators/LogicalOperators.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Data/CharactersStrings/CharactersStrings.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Data/DatatypeConversion/DatatypeConversion.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Data/IntegersFloats/IntegersFloats.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Data/TrueFalse/TrueFalse.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Data/Variables/Variables.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Data/VariableScope/VariableScope.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Form/Bezier/Bezier.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Form/BezierEllipse/BezierEllipse.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Form/PieChart/PieChart.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Form/PointsLines/PointsLines.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Form/ShapePrimitives/ShapePrimitives.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Form/SimpleCurves/SimpleCurves.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Form/TriangleStrip/TriangleStrip.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Form/Vertices/Vertices.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Image/Alphamask/Alphamask.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Image/BackgroundImage/BackgroundImage.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Image/CreateImage/CreateImage.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Image/LoadDisplayImage/LoadDisplayImage.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Image/Pointillism/Pointillism.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Image/RequestImage/RequestImage.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Image/Sprite/Sprite.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Image/Transparency/Transparency.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Input/Clock/Clock.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Input/Constrain/Constrain.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Input/Easing/Easing.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Input/Keyboard/Keyboard.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Input/KeyboardFunctions/KeyboardFunctions.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Input/Milliseconds/Milliseconds.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Input/Mouse1D/Mouse1D.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Input/Mouse2D/Mouse2D.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Input/MouseFunctions/MouseFunctions.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Input/MousePress/MousePress.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Input/MouseSignals/MouseSignals.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Input/StoringInput/StoringInput.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/AdditiveWave/AdditiveWave.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/Arctangent/Arctangent.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/Distance1D/Distance1D.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/Distance2D/Distance2D.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/DoubleRandom/DoubleRandom.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/Graphing2DEquation/Graphing2DEquation.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/IncrementDecrement/IncrementDecrement.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/Modulo/Modulo.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/Noise1D/Noise1D.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/Noise2D/Noise2D.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/Noise3D/Noise3D.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/NoiseWave/NoiseWave.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/OperatorPrecedence/OperatorPrecedence.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/PolarToCartesian/PolarToCartesian.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/Random/Random.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/Sine/Sine.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/SineCosine/SineCosine.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Math/SineWave/SineWave.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Objects/CompositeObjects/CompositeObjects.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Objects/Inheritance/Inheritance.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Objects/MultipleConstructors/MultipleConstructors.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Objects/Neighborhood/Neighborhood.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Objects/Objects/Objects.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Shape/DisableStyle/DisableStyle.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Shape/GetChild/GetChild.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Shape/LoadDisplayShape/LoadDisplayShape.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Shape/ScaleShape/ScaleShape.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Structure/Coordinates/Coordinates.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Structure/CreateGraphics/CreateGraphics.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Structure/Functions/Functions.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Structure/Loop/Loop.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Structure/NoLoop/NoLoop.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Structure/Recursion/Recursion.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Structure/Recursion2/Recursion2.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Structure/Redraw/Redraw.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Structure/SetupDraw/SetupDraw.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Structure/StatementsComments/StatementsComments.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Structure/WidthHeight/WidthHeight.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Transform/Arm/Arm.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Transform/Rotate/Rotate.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Transform/Scale/Scale.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Transform/Translate/Translate.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Transform/TriangleFlower/TriangleFlower.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Typography/Letters/Letters.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Typography/Words/Words.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Web/EmbeddedLinks/EmbeddedLinks.pde");
		pdeFiles.add("/Applications/Processing.app/Contents/Resources/Java/examples/Basics/Web/LoadingImages/LoadingImages.pde");

		return pdeFiles;

	}

}
