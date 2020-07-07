
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import utils.StateReference;

public class StateTextDiagramHelperUnitTest {
	
	StateTextDiagramHelper stateTextDiagramHelper = new StateTextDiagramHelper();
	
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

	
	
	//////////////////////////////////TRANSITIONS///////////////////////////////////////////////////
	
	
	@Test
	public void checkColorTransition() {

		String transition = "ExampleState -> AnotherExampleState";
		String expected = "ExampleState -[thickness=5,#Lime]> AnotherExampleState";
		assertEquals(expected, stateTextDiagramHelper.forwardTransitionLink(transition));
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
	

	@Test
	public void checkStateDescriptorRemovesFSMLower() {
		String fsmLine = "//fsm: State1 -> State2";
		String expected = "State1 -> State2";
		
		assertEquals(expected, StateTextDiagramHelper.stateDescriptor(fsmLine));
	
	}
	
	@Test
	public void checkStateDescriptorRemovesFSMUpper() {
		String fsmLine = "//FSM: State1 -> State2";
		String expected = "State1 -> State2";
		
		assertEquals(expected, StateTextDiagramHelper.stateDescriptor(fsmLine));
	
	}
	
	@Test
	public void checkStateDescriptorReturnsNullIfNotFSM() {
		String fsmLine = "State1 -> State2";
		
		assertNull(StateTextDiagramHelper.stateDescriptor(fsmLine));
	}
	
	
	
	
	
}