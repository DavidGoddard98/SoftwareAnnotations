package plantuml.statemachine.generation;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;


import net.sourceforge.plantuml.text.AbstractTextDiagramProvider;

public class StateDiagramTextProvider extends AbstractTextDiagramProvider {

	public StateDiagramTextProvider() {
		setEditorType(ITextEditor.class);
		System.out.println("in here at least");
	}

	@Override
	protected String getDiagramText(final IEditorPart editorPart, final IEditorInput editorInput, final ISelection selection, final Map<String, Object> markerAttributes) {
		final StringBuilder lines = getDiagramTextLinesMA(editorPart, editorInput, selection, markerAttributes);
		return (lines != null ? getDiagramText(lines) : null);
	}
	
	@Override
	public boolean supportsPath(IPath path) {
		return false;
	}
	
	//FSM///////////////////////////////////////////////////////////////////////

	private StateTextDiagramHelper stateTextDiagramHelper = null;
	
	public StateTextDiagramHelper getStateTextDiagramHelper() {
		if (stateTextDiagramHelper == null) {
			stateTextDiagramHelper = new StateTextDiagramHelper();
		}
		return stateTextDiagramHelper;
	}
	
	private StateMachineGenerator osmGenerator = null;
	
	public StateMachineGenerator getOSMGenerator() {
		if (osmGenerator == null) {
			osmGenerator = new StateMachineGenerator();
		}
		return osmGenerator;
	}


	protected StringBuilder getDiagramTextLinesMA(IEditorPart editorPart, IEditorInput editorInput, ISelection selection, Map<String, Object> markerAttributes) {
		final ITextEditor textEditor = (ITextEditor) editorPart;
		IDocument document = textEditor.getDocumentProvider().getDocument(editorInput);
		final int selectionStart = ((ITextSelection) (selection != null ? selection : textEditor.getSelectionProvider().getSelection())).getOffset();
		//FSM/////////////////////////////////////////////////////////////////
		
		IResource root = StateTextDiagramHelper.getRoot(editorInput);
		getStateTextDiagramHelper().removeHighlights(root);
		
		return getOSMGenerator().getDiagramTextLines(document, selectionStart, markerAttributes, editorInput);
//		
	}
	
	
}
