package testing;
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
import org.eclipse.jface.text.BadLocationException;
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
import org.junit.BeforeClass;
import org.junit.Test;

import linkOpener.StateLinkOpener;
import plantuml.statemachine.generation.StateDiagram;
import plantuml.statemachine.generation.StateMachineGenerator;
import plantuml.statemachine.generation.StateTextDiagramHelper;

public class StateLinkOpenerTest {
	static IDocument document;
	static IEditorInput input;
	static IEditorPart editor;
	static String className;
	static StateMachineGenerator stateMachineGen;
	static StateTextDiagramHelper stateTextDiagramHelper;
	static StateDiagram stateDiagram;
	static IResource root;
	static IEditorInput editorInput;
	
	@BeforeClass
	public static void createEnvironment() {
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
			editorInput = editor.getEditorInput();
			root = StateTextDiagramHelper.getRoot(editorInput);
			IPath path = ((IFileEditorInput) editorInput).getFile().getFullPath();

			stateDiagram = new StateDiagram(finder, document, root, path);
			stateTextDiagramHelper = new StateTextDiagramHelper(stateDiagram, "", 0, plantMarkerKey);

			System.out.println(document.get());
		} catch (CoreException e) {
			e.printStackTrace();
		} 
	}
	
	/*
	 * @Test public void checkFsmLink() { String link =
	 * "ExampleClass.java#FSM#state#11"; StateLinkOpener.o
	 * 
	 * }
	 */
	
	//Simulates clicking on text, needed to initialize data stores required for creating links. 
	public void clickOnText() {
		int selStart = 87;
		StringBuilder result = stateMachineGen.getDiagramTextLines(document, selStart, input);
	}
	
	@Test
	public void checkNavigatedToLineWhenStateClicked() throws BadLocationException {
		
		clickOnText();
		
		//Click the component
		String link = "ExampleClass.java#FSM#state#11";
		StateLinkOpener.linkBackState(link);
		
		ITextSelection sel=  (ITextSelection) ((ITextEditor) editor).getSelectionProvider().getSelection();
		int lineNum = document.getLineOfOffset(sel.getOffset());
		assertTrue("clicking a state component links you to code", lineNum == 11);
	}
	
	@Test
	public void checkMarkerCreatedWhenStateClicked() throws BadLocationException, CoreException {		
		clickOnText();
		
		//Click the component
		String link = "ExampleClass.java#FSM#state#11";
		StateLinkOpener.linkBackState(link);
		
		ITextSelection sel=  (ITextSelection) ((ITextEditor) editor).getSelectionProvider().getSelection();
		int lineNum = document.getLineOfOffset(sel.getOffset());
		System.out.println("linenum here" + lineNum);

		int offset = document.getLineOffset(lineNum);
		StringBuilder result = stateMachineGen.getDiagramTextLines(document, offset, input);

		boolean lineHighlighted = false;

		IMarker[] markers = root.findMarkers("FSM.State.Highlight", true, IResource.DEPTH_INFINITE);
		System.out.println(markers.length);
		for (IMarker m : markers) {
			System.out.println(m.getAttribute(IMarker.LINE_NUMBER, 0));
			if (m.getAttribute(IMarker.LINE_NUMBER, 0) == lineNum) {
				lineHighlighted = true;
				break;
			}
		}
		
		assertTrue("clicking a state component links you to code", lineHighlighted);
	}
	
	@Test
	public void checkColorLineWhenStateClicked() throws BadLocationException, CoreException {		
		String expected = "state AThirdState #Cyan";
		
		clickOnText();
		
		//Click the component
		String link = "ExampleClass.java#FSM#state#11";
		StateLinkOpener.linkBackState(link);
		
		ITextSelection sel=  (ITextSelection) ((ITextEditor) editor).getSelectionProvider().getSelection();
		//markers are inputted with lineNum + 1, therefore take one off here
		int lineNum = document.getLineOfOffset(sel.getOffset()) - 1;
		int offset = document.getLineOffset(lineNum);
		StringBuilder result = stateMachineGen.getDiagramTextLines(document, offset, input);

		assertTrue("clicking a state component links you to code", result.toString().contains(expected));
	}
//	
//	@Test
//	public void checkColorLineWhenTransitionClicked() {
//		
//	}
	//210
	
	@Test
	public void checkNavigatedToLineWhenTransitionClicked() throws BadLocationException {
		
		clickOnText();
		
		//Click the component
		String link = "ExampleClass.java#FSM#transition#6";
		StateLinkOpener.linkBackState(link);
		
		ITextSelection sel=  (ITextSelection) ((ITextEditor) editor).getSelectionProvider().getSelection();
		int lineNum = document.getLineOfOffset(sel.getOffset());
		assertTrue("clicking a state component links you to code", lineNum == 6);
	}
	
	@Test
	public void checkMarkerCreatedWhenTransitionClicked() throws BadLocationException, CoreException {		
		clickOnText();
		
		//Click the component
		String link = "ExampleClass.java#FSM#transition#6";
		StateLinkOpener.linkBackState(link);
		
		ITextSelection sel=  (ITextSelection) ((ITextEditor) editor).getSelectionProvider().getSelection();
		int lineNum = document.getLineOfOffset(sel.getOffset());
		System.out.println("linenum here" + lineNum);

		int offset = document.getLineOffset(lineNum);
		StringBuilder result = stateMachineGen.getDiagramTextLines(document, offset, input);

		boolean lineHighlighted = false;

		IMarker[] markers = root.findMarkers("FSM.Transition.Highlight_1", true, IResource.DEPTH_INFINITE);
		System.out.println(markers.length);
		for (IMarker m : markers) {
			System.out.println(m.getAttribute(IMarker.LINE_NUMBER, 0));
			if (m.getAttribute(IMarker.LINE_NUMBER, 0) == lineNum) {
				lineHighlighted = true;
				break;
			}
		}
		
		assertTrue("clicking a state component links you to code", lineHighlighted);
	}
	
	@Test
	public void checkColorLineWhenTransitionClicked() throws BadLocationException, CoreException {		
		String expected = "ExampleState -[thickness=5,#Lime]down-> AnotherExampleState : [[ExampleClass.java#FSM#transition#6 [anEvent ]]]";
		
		clickOnText();
		
		//Click the component
		String link = "ExampleClass.java#FSM#transition#6";
		StateLinkOpener.linkBackState(link);
		
		ITextSelection sel=  (ITextSelection) ((ITextEditor) editor).getSelectionProvider().getSelection();
		//markers are inputted with lineNum + 1, therefore take one off here
		int lineNum = document.getLineOfOffset(sel.getOffset());
		int offset = document.getLineOffset(lineNum);
		StringBuilder result = stateMachineGen.getDiagramTextLines(document, offset, input);

		assertTrue("clicking a state component links you to code", result.toString().contains(expected));
	}
	
}