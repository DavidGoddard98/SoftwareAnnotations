package testing;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import linkOpener.StateLinkOpener;
import plantuml.statemachine.generation.StateDiagram;
import plantuml.statemachine.generation.StateMachineGenerator;
import plantuml.statemachine.generation.StateTextDiagramHelper;

public class StateTextDiagramHelperTest {
	
	static IDocument document;
	static IEditorInput input;
	static IEditorPart editor;
	static String className;
	static StateMachineGenerator stateMachineGen;
	static StateTextDiagramHelper stateTextDiagramHelper;
	static StateDiagram stateDiagram;
	static IResource root;
	
	@Before
	public void createEnvironment() {
		//		IWorkbench workspace = PlatformUI.getWorkbench();

		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot wsRoot = workspace.getRoot();
		className = "ExampleClass.java";
		
		IPath location = new Path("/ExampleProject/src/ExampleClass.java");
		IFile file = wsRoot.getFile(location);
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		editor = page.getActiveEditor();
		input = editor.getEditorInput();
		
		stateMachineGen = new StateMachineGenerator();
		HashSet<String> plantMarkerKey = new HashSet<String>();

		
		
	
		try {
			IDocumentProvider provider = new TextFileDocumentProvider();
			provider.connect(file);
			document = provider.getDocument(file);
			FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
			IEditorInput editorInput = editor.getEditorInput();
			root = StateTextDiagramHelper.getRoot(editorInput);
			IPath path = ((IFileEditorInput) editorInput).getFile().getFullPath();

			stateDiagram = new StateDiagram(finder, document, root, path);
			stateTextDiagramHelper = new StateTextDiagramHelper(stateDiagram, "", 0, plantMarkerKey);

			System.out.println(document.get());
		} catch (CoreException e) {
			e.printStackTrace();
		} 
	}
	
	@Test 
	public void checkRootFound() {
		
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
		String stringToNegate = "a == b";
		String expected = "a != b";
		assertEquals(expected, stateTextDiagramHelper.negateCondition(stringToNegate));
	}
	
	@Test
	public void checkNegateInequality() {
		String stringToNegate = "a != b";
		String expected = "a == b";
		assertEquals(expected, stateTextDiagramHelper.negateCondition(stringToNegate));
	}
	
	@Test
	public void checkNegateGreaterEqualsTo() {
		String stringToNegate = "a >= b";
		String expected = "a < b";
		assertEquals(expected, stateTextDiagramHelper.negateCondition(stringToNegate));
	}
	
	@Test
	public void checkNegateLessEqualsTo() {
		String stringToNegate = "a <= b";
		String expected = "a > b";
		assertEquals(expected, stateTextDiagramHelper.negateCondition(stringToNegate));
	}
	
	@Test
	public void checkNegateGreaterThan() {
		String stringToNegate = "a > b";
		String expected = "a <= b";
		assertEquals(expected, stateTextDiagramHelper.negateCondition(stringToNegate));
	}
	
	@Test
	public void checkNegateLessThan() {
		String stringToNegate = "a < b";
		String expected = "a >= b";
		assertEquals(expected, stateTextDiagramHelper.negateCondition(stringToNegate));
	}
	
	///////////////////////////////////////////Miselanious Tests///////////////////////////////////////////
	
	@Test
	public void checkHighlightsAreRemovedIfJustState() throws CoreException {
		int selStart = 87;
		stateMachineGen.getDiagramTextLines(document, selStart, input);
		IMarker[] markers = root.findMarkers("FSM.State.Highlight", true, IResource.DEPTH_INFINITE);
		System.out.println(markers.length);
		
		stateTextDiagramHelper.removeHighlights(root);
		markers = root.findMarkers("FSM.State.Highlight", true, IResource.DEPTH_INFINITE);

		assertTrue("State markers removed", markers.length == 0);
	
	}
	
	@Test
	public void checkHighlightsAreRemovedIfJustTransition() throws CoreException {
		int selStart = 119;
		stateMachineGen.getDiagramTextLines(document, selStart, input);
		IMarker[] markers = root.findMarkers("FSM.Transition.Highlight_1", true, IResource.DEPTH_INFINITE);
		System.out.println(markers.length);
		
		stateTextDiagramHelper.removeHighlights(root);
		markers = root.findMarkers("FSM.Transition.Highlight_1", true, IResource.DEPTH_INFINITE);
		assertTrue("State markers removed", markers.length == 0);
	}
	
	@Test
	public void checkHighlightsAreRemovedIfStateAndTransition() throws CoreException {
		int selStart = 87;
		stateMachineGen.getDiagramTextLines(document, selStart, input);
		selStart = 119;
		stateMachineGen.getDiagramTextLines(document, selStart, input);
		
		IMarker[] markers = root.findMarkers("FSM.Transition.Highlight_1", true, IResource.DEPTH_INFINITE);
		System.out.println(markers.length);
		
		stateTextDiagramHelper.removeHighlights(root);
		markers = root.findMarkers("FSM.Transition.Highlight_1", true, IResource.DEPTH_INFINITE);
		
		assertTrue("State markers removed", markers.length == 0);

	}
	
	
	
	///////////////////////////////TEST DATA STORES////////////////////////////////////////////////
	
}