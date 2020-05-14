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
	
	public StringBuilder clickScenarioTwoStatesOneGuard() { 
		int selStart = 414;
		return stateMachineGen.getDiagramTextLines(document, selStart, input); 
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
		int selStart = 889; //cursor position in text editor
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
	
	
//	@Test
//	public void checkTwoStatesBothInvisible() { //FAILS
//		initializeResult();
//		result.append("[*] -> ExampleState" + "\n");
//		result.append("ExampleState -down-> AnotherState : No event found" + "\n");
//		result.append("ExampleState -down-> [*] : No event found" + "\n");
//		result.append("state ExampleState[[ExampleClass.java#FSM#state#71]]" + "\n");
//		result.append("state AnotherState[[ExampleClass.java#FSM#state#72]]" + "\n");
//		//Just last state should be visible
//		assertEquals(result.toString(), clickExample2_BothInvisible().toString());
//	}
	
	
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
	public void checkTwoStatesBothInisibleWithGuard() {
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
	
	public StringBuilder clickExample2_BothVisibleWithAdditionalActionAfterGuard() {
		int selStart = 2685;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test 
	public void checkTwoStatesBothVisibleWithAdditionalActionAfterGuard() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AnotherState -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#147 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] :  / call(); additionalAction();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#148]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#145]]" + "\n");
		//Just last state should be visible
		assertEquals(result.toString(), clickExample2_BothVisibleWithAdditionalActionAfterGuard().toString());
	}
	
	public StringBuilder clickExample2_SecondInvisibleWithAdditionalActionAfterGuard() {
		int selStart = 2686;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkTwoStatesSecondInvisibleWithAdditionalActionAfterGuard() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AnotherState -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#147 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] :  / call(); additionalAction();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#148]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#145]]" + "\n");
		//Just last state should be visible
		assertEquals(result.toString(), clickExample2_SecondInvisibleWithAdditionalActionAfterGuard().toString());
	}

	
	
	/////////////////////////////THREE STATES/////////////////////////////////////
	public StringBuilder clickExample3_AllVisible() {
		int selStart = 2949;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesAllVisible() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState :  / call();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#161]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#159]]" + "\n");
		result.append("AnotherState -down-> AThirdState : No event found" + "\n");
		result.append("AThirdState -down-> [*] :  / call();" + "\n");
		result.append("state AThirdState[[ExampleClass.java#FSM#state#163]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_AllVisible().toString());
	}
	
	public StringBuilder clickExample3_JustFirstVisible() {
		int selStart = 3179;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesJustFirstVisible() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AThirdState -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AThirdState :  / call();" + "\n");
		result.append("ExampleState -down-> [*] :  / call();" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#171]]" + "\n");
		result.append("state AThirdState[[ExampleClass.java#FSM#state#174]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_JustFirstVisible().toString());
	}
	
	public StringBuilder clickExample3_JustSecondVisible() {
		int selStart = 3421;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesJustSecondVisible() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState : No event found" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#182]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#181]]" + "\n");
		result.append("AThirdState -down-> [*] : No event found" + "\n");
		result.append("AnotherState -down-> AThirdState : No event found" + "\n");
		result.append("AnotherState -down-> [*] : No event found" + "\n");
		result.append("state AThirdState[[ExampleClass.java#FSM#state#184]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_JustSecondVisible().toString());
	}
	
	public StringBuilder clickExample3_JustThirdVisible() {
		int selStart = 3664;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesJustThirdVisible() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AThirdState : No event found" + "\n");
		result.append("AThirdState -down-> [*] :  / call();" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#191]]" + "\n");
		result.append("state AThirdState[[ExampleClass.java#FSM#state#193]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_JustThirdVisible().toString());
	}
	
	
	///////////////////THREE STATES WITH GUARD///////////////////////////////////
		
	
	public StringBuilder clickExample3_AllVisibleWithGuardOnSecondState() {
		int selStart = 3840;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesAllVisibleWithGuardOnSecondState() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#205 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> AThirdstate : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> AThirdstate :  / call();" + "\n");
		result.append("AThirdstate -down-> [*] :  / call();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#210]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#206]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#203]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_AllVisibleWithGuardOnSecondState().toString());
	}
	
	
	public StringBuilder clickExample3_SecondInvisibleWithGuardOnSecondState() {
		int selStart = 4105;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesSecondInvisibleWithGuardOnSecondState() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AThirdstate :  / call();" + "\n");
		result.append("AThirdstate -down-> [*] :  / call();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#226]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#220]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_SecondInvisibleWithGuardOnSecondState().toString());
	}
	
	public StringBuilder clickExample3_ThirdInvisibleWithGuardOnSecondState() {
		int selStart = 4358;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesThirdInvisibleWithGuardOnSecondState() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AThirdstate -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#238 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> AThirdstate : [!(ExampleGuard) ] / call();" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> AThirdstate :  / call();" + "\n");
		result.append("AnotherState -down-> [*] :  / call();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#242]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#239]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#236]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_ThirdInvisibleWithGuardOnSecondState().toString());
	}
	
	public StringBuilder clickExample3_JustFirstVisibleWithGuardOnSecondState() {
		int selStart = 4607;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesJustFirstVisibleWithGuardOnSecondState() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AnotherState -down-> [*] : No event found" + "\n");
		result.append("AThirdstate -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#253 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> AThirdstate : [!(ExampleGuard) ] / call();" + "\n");
		result.append("ExampleState -down-> [*] :  / call();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#256]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#254]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#251]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_JustFirstVisibleWithGuardOnSecondState().toString());
	}
	
	//Two states same guard
	public StringBuilder clickExample3_AllVisibleWithSameGuardOnSecondAndThirdState() {
		int selStart = 4890;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesAllVisibleWithSameGuardOnSecondAndThirdState() {
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#268 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> AThirdstate :  / call();" + "\n");
		result.append("AThirdstate -down-> [*] :  / call();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#271]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#269]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#266]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_AllVisibleWithSameGuardOnSecondAndThirdState().toString());
	}
	
	public StringBuilder clickExample3_SecondInvisibleWithSameGuardOnSecondAndThirdState() {
		int selStart = 5154;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	
//	@Test
//	public void checkThreeStatesSecondInvisibleWithSameGuardOnSecondAndThirdState() { //SHOULD DEFO FAIL
//		initializeResult();
//		result.append("[*] -> ExampleState" + "\n");
//		result.append("ExampleState -down-> AThirdstate : [ExampleGuard ] / call();" + "\n");
//		result.append("ExampleState -down-> [*] : [!ExampleGuard ] / call();" + "\n");
//		result.append("AThirdstate -down-> [*] :  / call();" + "\n");
//		result.append("state AThirdstate[[ExampleClass.java#FSM#state#286]]" + "\n");
//		result.append("state ExampleState[[ExampleClass.java#FSM#state#282]]" + "\n");
//		
//
//		//Just last state should be visible
//		assertEquals(result.toString(), clickExample3_SecondInvisibleWithSameGuardOnSecondAndThirdState().toString());
//	}
//	
	
	public StringBuilder clickExample3_ThirdInvisibleWithSameGuardOnSecondAndThirdState() {
		int selStart = 5407;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesThirdInvisibleWithSameGuardOnSecondAndThirdState() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AThirdstate -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#299 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> AThirdstate :  / call();" + "\n");
		result.append("AnotherState -down-> [*] :  / call();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#302]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#300]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#297]]" + "\n");
		

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_ThirdInvisibleWithSameGuardOnSecondAndThirdState().toString());
	}
	
	//Two seperate guards
	public StringBuilder clickExample3_AllVisibleWithDifferentGuardOnSecondAndThirdState() {
		int selStart = 5707;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesAllVisibleWithDifferentGuardOnSecondAndThirdState() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#315 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#320 [AnotherExampleGuard && !(ExampleGuard) ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) && !(AnotherExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#320 [AnotherExampleGuard ] / call();]]" + "\n");
		result.append("AnotherState -down-> [*] : [!(AnotherExampleGuard) ] / call();" + "\n");
		result.append("AThirdstate -down-> [*] :  / call();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#321]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#316]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#313]]" + "\n");

		

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_AllVisibleWithDifferentGuardOnSecondAndThirdState().toString());
	}
	
	public StringBuilder clickExample3_SecondInvisibleWithDifferentGuardOnSecondAndThirdState() {
		int selStart = 6009;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesSecondInvisibleWithDifferentGuardOnSecondAndThirdState() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AnotherState -down-> [*] : !(AnotherExampleGuard) " + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#333 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#337 [AnotherExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(AnotherExampleGuard) ] / call();" + "\n");
		result.append("AThirdstate -down-> [*] :  / call();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#338]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#334]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#331]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_SecondInvisibleWithDifferentGuardOnSecondAndThirdState().toString());
	}
	
	public StringBuilder clickExample3_ThirdInvisibleWithDifferentGuardOnSecondAndThirdState() {
		int selStart = 6366;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesThirdInvisibleWithDifferentGuardOnSecondAndThirdState() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AThirdstate -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#349 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#354 [AnotherExampleGuard && !(ExampleGuard) ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#354 [AnotherExampleGuard ] / call();]]" + "\n");
		result.append("AnotherState -down-> [*] :  / call();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#355]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#350]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#347]]" + "\n");


		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_ThirdInvisibleWithDifferentGuardOnSecondAndThirdState().toString());
	}
	
	public StringBuilder clickExample3_AllInVisibleWithDifferentGuardOnSecondAndThirdState() {
		int selStart = 6533;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesAllInvisibleWithDifferentGuardOnSecondAndThirdState() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AnotherState -down-> [*] : No event found" + "\n");
		result.append("AThirdstate -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#364 [ExampleGuard ]]]" + "\n");
		result.append("ExampleState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#368 [AnotherExampleGuard && !(ExampleGuard) ]]]" + "\n");
		result.append("ExampleState -down-> [*] : No event found" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#369]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#365]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#363]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_AllInVisibleWithDifferentGuardOnSecondAndThirdState().toString());
	}
	
	//Nested guard

	public StringBuilder clickExample3_AllVisibleNestedGuard() {
		int selStart = 6882;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesAllVisibleNestedGuard() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#381 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#385 [AnotherExampleGuard ] / call();]]" + "\n");
		result.append("AnotherState -down-> [*] : [!(AnotherExampleGuard) ] / call();" + "\n");
		result.append("AThirdstate -down-> [*] :  / call();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#386]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#382]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#379]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_AllVisibleNestedGuard().toString());
	}
	
	public StringBuilder clickExample3_SecondInvisibleNestedGuard() {
		int selStart = 7186;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesSecondInvisibleNestedGuard() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AnotherState -down-> [*] : !(AnotherExampleGuard) " + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#399 [ExampleGuard && !(AnotherExampleGuard) ] / call();]]" + "\n");
		result.append("ExampleState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#402 [ExampleGuard && AnotherExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(AnotherExampleGuard) ] / call();" + "\n");
		result.append("AThirdstate -down-> [*] :  / call();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#403]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#400]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#397]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_SecondInvisibleNestedGuard().toString());
	}
	
	public StringBuilder clickExample3_ThirdInvisibleNestedGuard() {
		int selStart = 7476;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesThirdInvisibleNestedGuard() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AThirdstate -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#416 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#420 [AnotherExampleGuard ] / call();]]" + "\n");
		result.append("AnotherState -down-> [*] :  / call();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#421]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#417]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#414]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_ThirdInvisibleNestedGuard().toString());
	}
	
	public StringBuilder clickExample3_AllInvisibleNestedGuard() {
		int selStart = 7723;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesAllInvisibleNestedGuard() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AnotherState -down-> [*] : No event found" + "\n");
		result.append("AThirdstate -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#432 [ExampleGuard && !(AnotherExampleGuard) ]]]" + "\n");
		result.append("ExampleState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#435 [ExampleGuard && AnotherExampleGuard ]]]" + "\n");
		result.append("ExampleState -down-> [*] : No event found" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#436]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#433]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#431]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_AllInvisibleNestedGuard().toString());
	}
	
	public StringBuilder clickExample3_AllVisibleWithAdditionalActionAtEndOfFirstGuard() {
		int selStart = 8071;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesAllVisibleAdditionalActionAtEndOfFirstGuard() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#451 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#454 [AnotherExampleGuard ] / call();]]" + "\n");
		result.append("AnotherState -down-> [*] : [!(AnotherExampleGuard) ] / call(); additionalAction();" + "\n");
		result.append("AThirdstate -down-> [*] :  / call(); additionalAction();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#455]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#452]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#449]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_AllVisibleWithAdditionalActionAtEndOfFirstGuard().toString());
	}
	
	public StringBuilder clickExample3_SecondInvisibleWithAdditionalActionAtEndOfFirstGuard() {
		int selStart = 8484;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesSecondInvisibleWithAdditionalActionAtEndOfFirstGuard() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#471 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#473 [AnotherExampleGuard ]]]" + "\n");
		result.append("AnotherState -down-> [*] : [!(AnotherExampleGuard) ] / additionalAction();" + "\n");
		result.append("AThirdstate -down-> [*] :  / call(); additionalAction();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#474]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#472]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#469]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_SecondInvisibleWithAdditionalActionAtEndOfFirstGuard().toString());
	}
	
	public StringBuilder clickExample3_ThirdInvisibleWithAdditionalActionAtEndOfFirstGuard() {
		int selStart = 8836;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesThirdInvisibleWithAdditionalActionAtEndOfFirstGuard() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AThirdstate -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#490 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#493 [AnotherExampleGuard ] / call();]]" + "\n");
		result.append("AnotherState -down-> [*] :  / call(); additionalAction();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#494]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#491]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#488]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_ThirdInvisibleWithAdditionalActionAtEndOfFirstGuard().toString());
	}
	
	public StringBuilder clickExample3_BothInvisibleWithAdditionalActionAtEndOfFirstGuard() {
		int selStart = 9186;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkThreeStatesBothInvisibleWithAdditionalActionAtEndOfFirstGuard() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AThirdstate -down-> [*] : No event found" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#509 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> AThirdstate : [[ExampleClass.java#FSM#transition#511 [AnotherExampleGuard ]]]" + "\n");
		result.append("AnotherState -down-> [*] :  / additionalAction();" + "\n");
		result.append("state AThirdstate[[ExampleClass.java#FSM#state#512]]" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#510]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#507]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample3_BothInvisibleWithAdditionalActionAtEndOfFirstGuard().toString());
	}
	

	
	//Remove command
	
	public StringBuilder clickExampleRemoveState() {
		int selStart = 9554;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkStateIsRemovedWithRemoveCommand() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#526]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExampleRemoveState().toString());
	}
	
	public StringBuilder clickExampleRemoveTransition() {
		int selStart = 9842;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkTransitionIsRemovedWithRemoveCommand() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState -down-> AThirdState : [[ExampleClass.java#FSM#transition#548 [AnotherExampleGuard && !(ExampleGuard) ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) && !(AnotherExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> AThirdState : [[ExampleClass.java#FSM#transition#548 [AnotherExampleGuard ] / call();]]" + "\n");
		result.append("AnotherState -down-> [*] : [!(AnotherExampleGuard) ] / call();" + "\n");
		result.append("AThirdState -down-> [*] :  / call();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#544]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#541]]" + "\n");
		result.append("state AThirdState[[ExampleClass.java#FSM#state#549]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExampleRemoveTransition().toString());
	}
	//Exit conditions
	
	public StringBuilder clickExampleExitCondition() {
		int selStart = 10313;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkStateTransitionsToExitStateIfExitConditionMet() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("AnotherState -down-> [*] : a.pause(true)" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#565 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> Unconditional : [!(ExampleGuard) ] / call();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#566]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#563]]" + "\n");
		result.append("state Unconditional[[ExampleClass.java#FSM#state#571]]" + "\n");
		result.append("Unconditional -down-> AThirdState : [[ExampleClass.java#FSM#transition#573 [AnotherExampleGuard ]]]" + "\n");
		result.append("Unconditional -down-> [*] : [!(AnotherExampleGuard) ]" + "\n");
		result.append("AThirdState -down-> [*] :  / call();" + "\n");
		result.append("state AThirdState[[ExampleClass.java#FSM#state#574]]" + "\n");
		//Just last state should be visible
		assertEquals(result.toString(), clickExampleExitCondition().toString());
	}
	
	//PlantUML
	
	public StringBuilder clickExamplePlantUMLState() {
		int selStart = 10665;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkPlantUMLStateCommandCreatesState() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#589]]" + "\n");
		result.append("ExampleState : desc" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#590 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> [*] :  / call();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#591]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#587]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExamplePlantUMLState().toString());
	}
	
	public StringBuilder clickOnPlantUMLStateLine() {
		int selStart = 10684;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkClickingPlantUMLLineColorsState() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("ExampleState : desc" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#589]]" + "\n");
		result.append("state ExampleState #Cyan" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#590 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> [*] :  / call();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#591]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#587]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickOnPlantUMLStateLine().toString());
	}
	
	public StringBuilder clickExamplePlantUMLTransition() {
		int selStart = 10922;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkPlantUMLTransitionCommandCreatesTransition() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#transitionStateAnotherState#605]]" + "\n");
		result.append("ExampleState -> AnotherState : [[ExampleClass.java#FSM#transition#605 ExampleState -> AnotherState]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#transitionStateExampleState#605]]" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#606 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> [*] :  / call();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#607]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#603]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExamplePlantUMLTransition().toString());
	}
	
	public StringBuilder clickOnPlantUMLTransitionLine() {
		int selStart = 10938;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkClickingPlantUMLCommandColorsTransition() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#transitionStateAnotherState#605]]" + "\n");
		result.append("ExampleState -[thickness=5,#Lime]> AnotherState : [[ExampleClass.java#FSM#transition#605 ExampleState -> AnotherState]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#transitionStateExampleState#605]]" + "\n");
		result.append("ExampleState -down-> AnotherState : [[ExampleClass.java#FSM#transition#606 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> [*] : [!(ExampleGuard) ] / call();" + "\n");
		result.append("AnotherState -down-> [*] :  / call();" + "\n");
		result.append("state AnotherState[[ExampleClass.java#FSM#state#607]]" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#603]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickOnPlantUMLTransitionLine().toString());
	}
	
	//if else
	public StringBuilder clickExample_If_Else_AllVisible() {
		int selStart = 11242;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkIfElseAllVisible() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("IfState -> [*] :  / call();" + "\n");
		result.append("ExampleState -down-> IfState : [[ExampleClass.java#FSM#transition#624 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> ElseState : [!(ExampleGuard) ] / call();" + "\n");
		result.append("ElseState -down-> [*] :  / call();" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#622]]" + "\n");
		result.append("state ElseState[[ExampleClass.java#FSM#state#628]]" + "\n");
		result.append("state IfState[[ExampleClass.java#FSM#state#625]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample_If_Else_AllVisible().toString());
	}
	
	public StringBuilder clickExample_If_Else_IfVisible() {
		int selStart = 11581;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkIfElse_JustIfVisible() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("IfState -> [*] : No event found" + "\n");
		result.append("ExampleState -down-> IfState : [[ExampleClass.java#FSM#transition#642 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> ElseState : [!(ExampleGuard) ] / call();" + "\n");
		result.append("ElseState -down-> [*] : No event found" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#640]]" + "\n");
		result.append("state ElseState[[ExampleClass.java#FSM#state#646]]" + "\n");
		result.append("state IfState[[ExampleClass.java#FSM#state#643]]" + "\n");

		//Just last state should be visible
		assertEquals(result.toString(), clickExample_If_Else_IfVisible().toString());
	}
	
	//if else if else
	
	public StringBuilder clickExample_If_ElseIf_Else_AllVisible() {
		int selStart = 11928;
		return stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkIf_ElseIf_Else_AllVisible() { 
		initializeResult();
		result.append("[*] -> ExampleState" + "\n");
		result.append("IfState -> [*] : No event found" + "\n");
		result.append("ElseIfState -> [*] : No event found" + "\n");
		result.append("ExampleState -down-> IfState : [[ExampleClass.java#FSM#transition#659 [ExampleGuard ] / call();]]" + "\n");
		result.append("ExampleState -down-> ElseIfState : [[ExampleClass.java#FSM#transition#662 [AnotherGuard && !(ExampleGuard) ] / call();]]" + "\n");
		result.append("ExampleState -down-> ElseState : [!(ExampleGuard) && !(AnotherGuard) ] / call();" + "\n");
		result.append("ElseState -down-> [*] : No event found" + "\n");
		result.append("state ExampleState[[ExampleClass.java#FSM#state#657]]" + "\n");
		result.append("state ElseState[[ExampleClass.java#FSM#state#666]]" + "\n");
		result.append("state IfState[[ExampleClass.java#FSM#state#660]]" + "\n");
		result.append("state ElseIfState[[ExampleClass.java#FSM#state#663]]" + "\n");


		//Just last state should be visible
		assertEquals(result.toString(), clickExample_If_ElseIf_Else_AllVisible().toString());
	}
	
		
	//while loop
		//exit conditions...
		//self loop - further work?
	//switch statement
		
}
