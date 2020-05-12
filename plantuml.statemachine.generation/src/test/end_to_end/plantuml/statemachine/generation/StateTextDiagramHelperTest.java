package plantuml.statemachine.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utils.StateReference;

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
	
	public void createNewStateTextDiagramHelper() {
		HashSet<String> plantMarkerKey = new HashSet<String>();
		stateTextDiagramHelper = new StateTextDiagramHelper(stateDiagram, "", 0, plantMarkerKey);
	}
	
	
	public void cleanEnvironment() throws CoreException {
		IMarker[] markers = root.findMarkers("FSM.State.Highlight", true, IResource.DEPTH_INFINITE);
		for (IMarker m : markers) {
			m.delete();
		}
		markers = root.findMarkers("FSM.Transition.Highlight_1", true, IResource.DEPTH_INFINITE);
		for (IMarker m : markers) {
			m.delete();
		}
		markers = root.findMarkers("FSM.MARKER", true, IResource.DEPTH_INFINITE);
		for (IMarker m : markers) {
			m.delete();
		}
		
	}
	
	///////////////////////////////////////STATES///////////////////////////////////////

	
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
	
///////////////////////////////KEY CREATING AND MAINTENANCE////////////////////////////////////////////////
	
	
	
	@Test
	public void checkUniqueKeyIsCreated() throws CoreException {
		StateReference stateReference = new StateReference("theLine", "editorLine", 1, 0, 8); 
		createNewStateTextDiagramHelper();
		int numKeys = stateTextDiagramHelper.plantMarkerKey.size();
		stateTextDiagramHelper.createKey(stateReference);
		assertEquals(numKeys + 1, stateTextDiagramHelper.plantMarkerKey.size());
	}
	
	@Test
	public void checkUniqueMarkerIsCreated() throws CoreException {
		cleanEnvironment();
		StateReference stateReference = new StateReference("theLine", "editorLine", 1, 0, 8); 
		createNewStateTextDiagramHelper();
		IMarker[] markers = root.findMarkers("FSM.MARKER", true, IResource.DEPTH_INFINITE);
		int numMarkers = markers.length;
		stateTextDiagramHelper.createKey(stateReference);
		assertEquals(numMarkers + 1, root.findMarkers("FSM.MARKER", true, IResource.DEPTH_INFINITE).length);
	}
	
	@Test
	public void checkKeyIsNotCreatedIfKeyExists() throws CoreException {
		StateReference stateReference = new StateReference("theLine", "editorLine", 1, 0, 8); 
		createNewStateTextDiagramHelper();
		int numKeys = stateTextDiagramHelper.plantMarkerKey.size();
		stateTextDiagramHelper.createKey(stateReference);
		stateTextDiagramHelper.createKey(stateReference);
		assertEquals(numKeys + 1, stateTextDiagramHelper.plantMarkerKey.size());
	}
	
	@Test
	public void checkMarkerIsNotCreatedIfKeyExists() throws CoreException {
		cleanEnvironment();
		StateReference stateReference = new StateReference("theLine", "editorLine", 1, 0, 8); 
		createNewStateTextDiagramHelper();
		IMarker[] markers = root.findMarkers("FSM.MARKER", true, IResource.DEPTH_INFINITE);
		int numMarkers = markers.length;
		stateTextDiagramHelper.createKey(stateReference);
		stateTextDiagramHelper.createKey(stateReference);
		assertEquals(numMarkers + 1, root.findMarkers("FSM.MARKER", true, IResource.DEPTH_INFINITE).length);
	}
	
	@Test
	public void checkKeyIsUpdatedIfLineNumberChanges() throws CoreException {
		cleanEnvironment();

		StateReference stateReference = new StateReference("theLine", "editorLine", 1, 0, 8); 
		createNewStateTextDiagramHelper();
		stateTextDiagramHelper.createKey(stateReference);
		stateReference = new StateReference("theLine", "editorLine", 2, 0, 8); 
		String key = stateDiagram.className + stateReference.editorLine + String.valueOf(2 + 1) + String.valueOf(0) + String.valueOf(8);
		stateTextDiagramHelper.createKey(stateReference);
		assertTrue(stateTextDiagramHelper.plantMarkerKey.contains(key));
	}
	
	@Test
	public void checkMarkerIsUpdatedIfLineNumberChanges() throws CoreException {
		cleanEnvironment();

		StateReference stateReference = new StateReference("theLine", "editorLine", 1, 0, 8); 
		createNewStateTextDiagramHelper();
		stateTextDiagramHelper.createKey(stateReference);
		stateReference = new StateReference("theLine", "editorLine", 2, 0, 8); 
		stateTextDiagramHelper.createKey(stateReference);
		IMarker[] markers = root.findMarkers("FSM.MARKER", true, IResource.DEPTH_INFINITE);
		int lineNum = 0;
		for (IMarker m : markers) {
			lineNum = m.getAttribute(IMarker.LINE_NUMBER, 0) - 1;
		}
		
		assertEquals(stateReference.lineNum, lineNum);
	}
	
	@Test
	public void checkKeyIsUpdatedIfLineChanges() throws CoreException {
		cleanEnvironment();
		StateReference stateReference = new StateReference("theLine", "editorLine", 1, 0, 8); 
		createNewStateTextDiagramHelper();
		stateTextDiagramHelper.createKey(stateReference);
		stateReference = new StateReference("changed theLine", "editorLine", 1, 3, 7); 
		String key = stateDiagram.className + stateReference.editorLine + String.valueOf(1 + 1) + String.valueOf(3) + String.valueOf(7);
		stateTextDiagramHelper.createKey(stateReference);
		assertTrue(stateTextDiagramHelper.plantMarkerKey.contains(key));
	}
	
	@Test
	public void checkMarkerIsUpdatedIfLineChanges() throws CoreException {
		cleanEnvironment();

		StateReference stateReference = new StateReference("theLine", "editorLine", 1, 0, 8); 
		createNewStateTextDiagramHelper();
		stateTextDiagramHelper.createKey(stateReference);
		stateReference = new StateReference("theLine", "editorLine", 2, 0, 8); 
		stateTextDiagramHelper.createKey(stateReference);
		IMarker[] markers = root.findMarkers("FSM.MARKER", true, IResource.DEPTH_INFINITE);
		String theLine = "";
		for (IMarker m : markers) {
			theLine = m.getAttribute(IMarker.MESSAGE, "");
		}
		
		assertEquals(stateReference.editorLine, theLine);
	}
	
	
	//////////////////////////////////////DATA STORES//////////////////////////////////////////////////////////////////
	
	@Test 
	public void checkAppendToListsAppendsTransitionIfGivenTransition() {
		stateDiagram.clearStorage();
		String line = "ExampleState -> AnotherState";
		String editorLine = "FSM: ExampleState -> AnotherState";
		int lineNum = 1;
		int multiLineEnd = -1;
		int start = 50;
		
	}
	
	@Test
	public void checkAppendToListsAppendsStateIfGivenState() {
		
	}
	
	@Test
	public void checkAppendToListsAppendsReferenceToAListIFStateSeenBefore() {
		
	}
	
	@Test
	public void checkAppendToListsCreatesNewListIfNewStateFound() {
		
	}
	
	@Test
	public void checkAppendToListsCorrectlyIdentifiesBothStatesGivenTransition() {
		
	}
	
	@Test
	public void checkAppendToListsCorrectlyIdentifiesStateGivenState() {
		
	}
	
}
