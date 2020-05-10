package testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import plantuml.statemachine.generation.StateMachineGenerator;
import plantuml.statemachine.generation.StateTextDiagramHelper;

public class StateTextDiagramHelperTest {
	static IDocument document;
	static IEditorInput input;
	static String className;
	static StateMachineGenerator stateMachineGen;
	static StateTextDiagramHelper stateTextDiagramHelper;
	@BeforeClass
	public static void createEnvironment() {
		//		IWorkbench workspace = PlatformUI.getWorkbench();

		stateMachineGen = new StateMachineGenerator();
		stateTextDiagramHelper = new StateTextDiagramHelper();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot wsRoot = workspace.getRoot();
		className = "ExampleClass.java";
		
		IPath location = new Path("/ExampleProject/src/ExampleClass.java");
		IFile file = wsRoot.getFile(location);
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart editor = page.getActiveEditor();
		input = editor.getEditorInput();
//		
		try {
			IDocumentProvider provider = new TextFileDocumentProvider();
			provider.connect(file);
			document = provider.getDocument(file);
			System.out.println(document.get());
		} catch (CoreException e) {
			e.printStackTrace();
		} 
	}
	
	
	///////////////////////////////////////STATES///////////////////////////////////////
	@Test
	public void checkColorState() {

		String expected = "state ExampleState #Cyan";
		String stateName = "ExampleState";
		assertEquals(expected, stateTextDiagramHelper.forwardStateLink(stateName));
	}
	
//	public void checkColorTransition() {
//		StateTextDiagramHelper stateTextDiagramHelper = new StateTextDiagramHelper();
//
//		String transition = "ExampleState -> AnotherExampleState";
//		String expected = "ExampleState -[thickness=5,#Lime]> AnotherExampleState";
//		assertEquals(expected, stateTextDiagramHelper.backwardsTransitionLink(transition))
//	}
	
	@Test
	public void checkClickingLineColorsState() {
		
		//a section within the lineNumber
		int selStart = 87;
		StringBuilder result = stateMachineGen.getDiagramTextLines(document, selStart, input);
		String expected = "state ExampleState #Cyan";
		assertTrue("state turns blue", result.toString().contains(expected));
		
	}
	
	@Test
	public void checkStateLinkCreated() {
		
		//a section within the lineNumber
		int selStart = 87;
		StringBuilder result = stateMachineGen.getDiagramTextLines(document, selStart, input);
		String expected = "state ExampleState[[ExampleClass.java#FSM#state#3]]";
		assertTrue("state link appended in []", result.toString().contains(expected));
		
	}
	
	//////////////////////////////////TRANSITIONS///////////////////////////////////////////////////
	
	
	@Test
	public void checkColorTransition() {

		String transition = "ExampleState -> AnotherExampleState";
		String expected = "ExampleState -[thickness=5,#Lime]> AnotherExampleState";
		assertEquals(expected, stateTextDiagramHelper.forwardTransitionLink(transition));
	}
	
	@Test
	public void checkClickingLineColorsTransition() {
		
		//a section within the lineNumber of that transition
		int selStart = 119;
		StringBuilder result = stateMachineGen.getDiagramTextLines(document, selStart, input);
		String expected = "ExampleState -[thickness=5,#Lime]down-> AnotherExampleState : [[ExampleClass.java#FSM#transition#6 [anEvent ]]]";
		assertTrue("transition turns green", result.toString().contains(expected));
	}
	
	@Test
	public void checkTransitionLinkCreated() {
		
		//a selection within uml but not on transition line
		int selStart = 70;
		StringBuilder result = stateMachineGen.getDiagramTextLines(document, selStart, input);
		String expected = "ExampleState -down-> AnotherExampleState : [[ExampleClass.java#FSM#transition#6 [anEvent ]]]";
		assertTrue("transition link created", result.toString().contains(expected));
	}
	
	@Test
	public void checkTransitionLinkCreatedAndColor() {
		
		//a section within the lineNumber of that transition
		int selStart = 119;
		StringBuilder result = stateMachineGen.getDiagramTextLines(document, selStart, input);
		String expected = "ExampleState -[thickness=5,#Lime]down-> AnotherExampleState : [[ExampleClass.java#FSM#transition#6 [anEvent ]]]";
		assertTrue("transition link created", result.toString().contains(expected));
	}
	
	///////////////////////////////NEGATION METHODS////////////////////////////////////////////////
	
	@Test
	public void checkNegateEquality() {
		
	}
	
	@Test
	public void checkNegateInequality() {
		
	}
	
	@Test
	public void checkNegateGreaterEqualsTo() {
		
	}
	
	@Test
	public void checkNegateLessEqualsTo() {
		
	}
	
	@Test
	public void checkNegateLessThan() {
		
	}
	
	@Test
	public void checkNegateGreaterThan() {
		
	}
	
	///////////////////////////////TEST DATA STORES////////////////////////////////////////////////
	
}
