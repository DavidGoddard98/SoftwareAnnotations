package plantuml.osm.generation;

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
	IDocument document;

	@Override
	protected String getDiagramText(final IEditorPart editorPart, final IEditorInput editorInput, final ISelection selection, final Map<String, Object> markerAttributes) {
		System.out.println("inmyplug");
		final StringBuilder lines = getDiagramTextLinesMA(editorPart, editorInput, selection, markerAttributes);
		return (lines != null ? getDiagramText(lines) : null);
	}
	
	@Override
	public boolean supportsPath(IPath path) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public String getDiagramText(final IPath path) {
		System.out.println("dsdsad");

		final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		if (file != null && file.exists()) {
			return getTextDiagramHelper().getDiagramText(file);
		}
		return null;
	}
	
	//FSM///////////////////////////////////////////////////////////////////////

	private StateTextDiagramHelper stateTextDiagramHelper = null;
	
	public StateTextDiagramHelper getStateTextDiagramHelper() {
		if (stateTextDiagramHelper == null) {
			stateTextDiagramHelper = new StateTextDiagramHelper();
		}
		return stateTextDiagramHelper;
	}
	
	private OSMGenerator osmGenerator = null;
	
	public OSMGenerator getOSMGenerator() {
		if (osmGenerator == null) {
			osmGenerator = new OSMGenerator();
		}
		return osmGenerator;
	}


	protected StringBuilder getDiagramTextLinesMA(IEditorPart editorPart, IEditorInput editorInput, ISelection selection, Map<String, Object> markerAttributes) {
		System.out.println("we here in my one ");
		final ITextEditor textEditor = (ITextEditor) editorPart;
		document = textEditor.getDocumentProvider().getDocument(editorInput);
		final int selectionStart = ((ITextSelection) (selection != null ? selection : textEditor.getSelectionProvider().getSelection())).getOffset();
		//FSM/////////////////////////////////////////////////////////////////
		
		IResource root = StateTextDiagramHelper.getRoot(editorInput);
		getStateTextDiagramHelper().removeHighlights(root);
		//String providerInfo = Activator.getDefault().getDiagramTextProviderId(this);
//		if (providerInfo.equals("net.sourceforge.plantuml.text.statemachineDiagramProvider")) { //user used @start_state_machine
//			return getStateTextDiagramHelper().getDiagramTextLines(document, selectionStart, markerAttributes, editorInput);
//		////////////////////////////////////////////////////////////////////////
//		} else if (providerInfo.equals("net.sourceforge.plantuml.text.autoGenerateOSMDiagramProvider")) {
			return getOSMGenerator().getDiagramTextLines(document, selectionStart, markerAttributes, editorInput);
//		} else
//			return null;
	}
	
	
}
