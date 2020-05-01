package net.sourceforge.plantuml.text;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import net.sourceforge.plantuml.eclipse.utils.DiagramTextProvider;


public class AnotherTextProvider extends AbstractTextDiagramProvider {
	
	public AnotherTextProvider() {
		System.out.println("in one");
		setEditorType(ITextEditor.class);
	}
	
	protected String getDiagramText(final IEditorPart editorPart, final IEditorInput editorInput, final ISelection selection, final Map<String, Object> markerAttributes) {
		System.out.println("dsdsadvbngvnvbnvbnbvnvnbvc");
		final StringBuilder lines = getDiagramTextLines(editorPart, editorInput, selection, markerAttributes);
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
	
	


}
