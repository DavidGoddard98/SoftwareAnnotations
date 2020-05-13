package plantuml.statemachine.generation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utils.Node;
import utils.StateTree;

public class ContextTest {
	
	static IDocument document;
	static IEditorInput input;
	static IEditorPart editor;
	static String className;
	static StateMachineGenerator stateMachineGen;
	static StateTextDiagramHelper stateTextDiagramHelper;
	static StateDiagram stateDiagram;
	static IResource root;
	static StringBuilder result;
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
		
		HashSet<String> plantMarkerKey = new HashSet<String>();

		try {
			IDocumentProvider provider = new TextFileDocumentProvider();
			provider.connect(file);
			document = provider.getDocument(file);
			FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
			IEditorInput editorInput = editor.getEditorInput();
			root = StateTextDiagramHelper.getRoot(editorInput);
			IPath path = ((IFileEditorInput) editorInput).getFile().getFullPath();

			stateTextDiagramHelper = new StateTextDiagramHelper(stateDiagram, "", 0, plantMarkerKey);
			StateTree theTree = null;
			stateMachineGen = new StateMachineGenerator(theTree);

			System.out.println(document.get());
		} catch (CoreException e) {
			e.printStackTrace();
		} 
	}

	@BeforeClass
	public static void initializeResult() {
		result = new StringBuilder();
		result.append("hide empty description" + "\n");
		result.append("skinparam maxmessagesize 200" + "\n");

	}
	
	
	//////////////////////////////////SINGLE STATE//////////////////////////////
	public StringBuilder clickExample_oneStateVisible() { 
		int selStart = 386;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
	}
	
	@Test
	public void checkOneState() {
		initializeResult();
		
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> [*] : No event found" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#16]]" + "\n");
		assertEquals(result.toString(), clickExample_oneStateVisible().toString());
	}
	
	public StringBuilder clickExample_oneStateInvisible() { 
		int selStart = 486;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
	}
	
	@Test
	public void checkOneStateInvisible() {
		initializeResult();
		
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> [*] : No event found" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#50]]" + "\n");
		assertEquals(result.toString(), clickExample_oneStateInvisible().toString());
	}
	
	///////////////////////////////////TWO_STATES///////////////////////////////
	public StringBuilder clickExample_twoStatesBothVisible() {
		int selStart = 635;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
	}
	
	@Test
	public void checkTwoStatesBothVisible() {
		initializeResult();
		
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState : No event found" + "\n");
		result.append("AnotherState -down-> [*] : No event found" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#24]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#22]]" + "\n");
		assertEquals(result.toString(), clickExample_twoStatesBothVisible().toString());
	}
	
	public StringBuilder clickExample_twoStatesJustFirstVisible() {
		int selStart = 789;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
	}

	@Test
	public void checkTwoStatesJustFirstVisible() {
		initializeResult();
		
		result.append("[*] -> ExampleState" + "\n");
		result.append("AnotherState -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : No event found" + "\n");
		result.append("ExampleState -down-> [*] : No event found" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#32]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#30]]" + "\n");
		assertEquals(result.toString(), clickExample_twoStatesJustFirstVisible().toString());
	}
	
	public StringBuilder clickExample_twoStatesJustSecondVisible() {
		int selStart = 968;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
	}
	
	@Test
	public void checkTwoStatesJustSecondVisible() {
		initializeResult();
		
		result.append("[*] -> AnotherState" + "\n");
		result.append("AnotherState -down-> [*] : No event found" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#24]]" + "\n");
		assertEquals(result.toString(), clickExample_twoStatesJustSecondVisible().toString());
	}
	
	public StringBuilder clickExample_twoStatesBothInvisible() {
		int selStart = 1105;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
	}
	
	
	@Test
	public void checkTwoStatesBothInvisible() {
		initializeResult();
		result.append("[*] -> AnotherState" + "\n");
		result.append("AnotherState -down-> [*] : No event found" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#24]]" + "\n");
		//Just last state should be visible
		assertEquals(result.toString(), clickExample_twoStatesBothInvisible().toString());
	}
	
	
	/////////////////////////////WITH GUARD////////////////////////////////////////////////
	
	public StringBuilder clickExample_twoStatesBothVisibleWithGuard() {
		int selStart = 1329;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkTwoStatesBothVisibleWithGuard() {
		initializeResult();
		result.append("[*] -> AnotherState" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#64 [ExampleGuard ]]]" + "\n");
		result.append("ExampleState -down-> [*] : No event found" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#65]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#62]]" + "\n");
		//Just last state should be visible
		assertEquals(result.toString(), clickExample_twoStatesBothInvisible().toString());
	}
	
	
	
	public StringBuilder clickExample_twoStatesFirstVisibleWithGuard() {
		int selStart = 1439;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkTwoStatesFirstVisibleWithGuard() {
		initializeResult();
		result.append("[*] -> AnotherState" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#82 [ExampleGuard ]]]" + "\n");
		result.append("ExampleState -down-> [*] : No event found" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#83]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#81]]" + "\n");
		//Just last state should be visible
		assertEquals(result.toString(), clickExample_twoStatesBothInvisible().toString());
	}
	
	public StringBuilder clickExample_twoStatesSecondVisibleWithGuard() {
		int selStart = 1668;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkTwoStatesSecondVisibleWithGuard() {
		initializeResult();
		result.append("[*] -> AnotherState" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#91 [ExampleGuard ]]]" + "\n");
		result.append("ExampleState -down-> [*] : No event found" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#92]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#90]]" + "\n");
		//Just last state should be visible
		assertEquals(result.toString(), clickExample_twoStatesBothInvisible().toString());
	}
	
	
	public StringBuilder clickExample2_BothInvisibleWithGuard() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkTwoStatesBothInVisibleWithGuard() {
		initializeResult();
		result.append("[*] -> AnotherState" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#91 [ExampleGuard ]]]" + "\n");
		result.append("ExampleState -down-> [*] : No event found" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#92]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#90]]" + "\n");
		//Just last state should be visible
		assertEquals(result.toString(), clickExample_twoStatesBothInvisible().toString());
	}
	
	/////////////////////////////THREE STATES/////////////////////////////////////
	
	public StringBuilder clickExample3_AllVisibleWithGuardOnSecondState() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	public StringBuilder clickExample3_SecondInvisibleWithGuardOnSecondState() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	public StringBuilder clickExample3_ThirdInvisibleWithGuardOnSecondState() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	public StringBuilder clickExample3_JustFirstVisibleWithGuardOnSecondState() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	public StringBuilder clickExample3_WithGuardOnSecondState() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}

	
	
}
