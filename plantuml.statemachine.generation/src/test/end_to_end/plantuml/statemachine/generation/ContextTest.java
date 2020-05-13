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
	public StringBuilder clickExample1_Visible() { 
		int selStart = 414;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
	}
	
	@Test
	public void checkOneState() {
		initializeResult();
		
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> [*] :  / call();" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#20]]" + "\n");
		assertEquals(result.toString(), clickExample1_Visible().toString());
	}
	
	public StringBuilder clickExample1_Invisible() { 
		int selStart = 515;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
	}
	
	@Test
	public void checkOneStateInvisible() {
		initializeResult();
		
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> [*] : No event found" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#26]]" + "\n");
		assertEquals(result.toString(), clickExample1_Invisible().toString());
	}
	
	//Actions
	public StringBuilder clickExample1_WithAction() { 
		int selStart = 687;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
	}
	
	@Test
	public void checkOneStateWithAction() {
		initializeResult();
		
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> [*] :  / call(); additionalAction();" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#35]]" + "\n");
		assertEquals(result.toString(), clickExample1_WithAction().toString());
	}
	
	///////////////////////////////////TWO_STATES///////////////////////////////
	public StringBuilder clickExample2_BothVisible() {
		int selStart = 889;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
	}
	
	@Test
	public void checkTwoStatesBothVisible() {
		initializeResult();
		
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState :  / call();" + "\n");
		result.append("AnotherState -down-> [*] :  / call();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#47]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#45]]" + "\n");
		assertEquals(result.toString(), clickExample2_BothVisible().toString());
	}
	
	public StringBuilder clickExample2_JustFirstVisible() {
		int selStart = 1072;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
	}

	@Test
	public void checkTwoStatesJustFirstVisible() {
		initializeResult();
		
		result.append("[*] -> ExampleState" + "\n");
		result.append("AnotherState -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState :  / call();" + "\n");
		result.append("ExampleState -down-> [*] :  / call();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#57]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#55]]" + "\n");
		assertEquals(result.toString(), clickExample2_JustFirstVisible().toString());
	}
	
	public StringBuilder clickExample2_JustSecondVisible() {
		int selStart = 1278;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
	}
	
	@Test
	public void checkTwoStatesJustSecondVisible() {	
		initializeResult();
		
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState : No event found" + "\n");
		result.append("AnotherState -down-> [*] :  / call();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#65]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#64]]" + "\n");
		assertEquals(result.toString(), clickExample2_JustSecondVisible().toString());
	}
	
	public StringBuilder clickExample2_BothInvisible() {
		int selStart = 1415;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
	}
	
	
	@Test
	public void checkTwoStatesBothInvisible() { //FAILS
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState : No event found" + "\n");
		result.append("ExampleState -down-> [*] : No event found" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#71]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#72]]" + "\n");
		//Just last state should be visible
		assertEquals(result.toString(), clickExample2_BothInvisible().toString());
	}
	
	
	/////////////////////////////WITH GUARD////////////////////////////////////////////////
	
	public StringBuilder clickExample2_BothVisibleWithGuard() {
		int selStart = 1599;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkTwoStatesBothVisibleWithGuard() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#85 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> [*] :  / call();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#86]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#83]]" + "\n");
		//Just last state should be visible
		assertEquals(result.toString(), clickExample2_BothVisibleWithGuard().toString());
	}
	
	
	
	public StringBuilder clickExample2_FirstVisibleWithGuard() {
		int selStart = 1765;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkTwoStatesFirstVisibleWithGuard() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AnotherState -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#97 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] :  / call();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#98]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#95]]" + "\n");
		//Just last state should be visible
		assertEquals(result.toString(), clickExample2_FirstVisibleWithGuard().toString());
	}
	
	public StringBuilder clickExample2_SecondVisibleWithGuard() {
		int selStart = 1963;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkTwoStatesSecondVisibleWithGuard() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#107 [ExampleGuard ]]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ]" + "\n");
		result.append("AnotherState -down-> [*] :  / call();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#108]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#106]]" + "\n");
		//Just last state should be visible
		assertEquals(result.toString(), clickExample2_SecondVisibleWithGuard().toString());
	}
	
	
	public StringBuilder clickExample2_BothInvisibleWithGuard() {
		int selStart = 2237;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkTwoStatesBothInVisibleWithGuard() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AnotherState -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#116 [ExampleGuard ]]]" + "\n");
		result.append("ExampleState -down-> [*] : No event found" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#117]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#115]]" + "\n");
		//Just last state should be visible
		assertEquals(result.toString(), clickExample2_BothInvisibleWithGuard().toString());
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
	
	//Two states same guard
	public StringBuilder clickExample3_AllVisibleWithSameGuardOnSecondAndThirdState() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	public StringBuilder clickExample3_SecondInvisibleWithSameGuardOnSecondAndThirdState() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	public StringBuilder clickExample3_ThirdInvisibleWithSameGuardOnSecondAndThirdState() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	//Two seperate guards
	public StringBuilder clickExample3_AllVisibleWithDifferentGuardOnSecondAndThirdState() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	public StringBuilder clickExample3_SecondInvisibleWithDifferentGuardOnSecondAndThirdState() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	public StringBuilder clickExample3_ThirdInvisibleWithDifferentGuardOnSecondAndThirdState() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	public StringBuilder clickExample3_AllInVisibleWithDifferentGuardOnSecondAndThirdState() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	//Nested guard

	public StringBuilder clickExample3_AllVisibleNestedGuard() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	public StringBuilder clickExample3_SecondInvisibleNestedGuard() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	public StringBuilder clickExample3_ThirdInvisibleNestedGuard() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	public StringBuilder clickExample3_AllInVisibleNestedGuard() {
		int selStart = 1837;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	
	////////////////////////////Unconditional state in /////////////////
	
	
	//Remove command
	
	//Exit conditions
	
	//PlantUML
	
	//comments on the end of lines?
}
