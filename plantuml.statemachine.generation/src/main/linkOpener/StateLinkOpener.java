package linkOpener;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import net.sourceforge.plantuml.eclipse.utils.ILinkOpener;
import net.sourceforge.plantuml.eclipse.utils.LinkData;

public class StateLinkOpener implements ILinkOpener {
	
	@Override
	public int supportsLink(LinkData link) {	
		
		String href = link.href;
		
		if (href.contains("FSM")) { 
			return CUSTOM_SUPPORT;
		}
		return NO_SUPPORT;
	}

	
	
	/**
	 * If a user clicks on a state/transition this function deals with linking it back to the relevant position in the text
	 
	 * NOTE: 'link' will be one of 3 things;
	 * 1) "state " + stateName + "[["+stateDiagram.className+"#FSM#state#"+ lineNum +"]]"
	 * 2) "state " + stateName + "[["+stateDiagram.className+"#FSM#"+stateReference.editorLine+"#"+stateReference.lineNum+"]]"
	 * 3) "aLine + " : " + "[["+stateDiagram.className+"#FSM#transition#"+lineNum+" "+originalLine+"]]"
	
	 * This string is then parsed over and split for the necessary information to match it with a FSM.MARKER.
	 * Once found a new marker is created which points to the first char of this FSM.MARKER and then this marker is opened.
	 * This means an editor is opened and the cursor is selected at that char position. Therefore when getDiagramTextLines() is called either;
	 * the transition,
	 * the transitionState and all references to it,
	 * or the state and all references to it,
	 * are highlighted - with this also being reflected in the diagram.
	 * @param link - see above
	 */
	public static void linkBackState(String link) {
		try {
			
			String linkToArray[] = link.split("#");
		    String fileName = linkToArray[0];
	    	int linkLineNum = Integer.parseInt(linkToArray[3]) + 1;

		    IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot wsRoot = workspace.getRoot();
			IResource resource = workspace.getRoot();
	        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	        
	        //find the FSM.MARKERS for the editor that relates to the diagram currently opened
	    	IMarker[] markers = resource.findMarkers("FSM.MARKER", true, IResource.DEPTH_INFINITE);
	    	System.out.println();
	    	System.out.println();
	    	System.out.println();
	    	System.out.println("NUM MARKERS" + markers.length);
	    	//Loops through FSM markers and checks to see if any relate to the link that was clicked
	    	for (IMarker m : markers) {
	    		
	    		//Extract information from the markers 
	    		String path = (String)(m.getAttribute(IMarker.SOURCE_ID));
	    		String[] tmp = path.split("/"); //obtain the className of the marker
	    	  	String file = tmp[tmp.length-1];
	    	  	int charStart = (int)m.getAttribute(IMarker.CHAR_START); 
	    	  	int markerLine = (int)m.getAttribute(IMarker.LINE_NUMBER);
		    	String message = (String)m.getAttribute(IMarker.MESSAGE);
		    	
	    		IResource root = wsRoot.findMember(path); // needed to create a new marker to open on in the active editor.
	    		
	    		
		    	//Open on transitionState
	    	  	if (markerLine == linkLineNum && file.equals(fileName) && linkToArray[2].equals(message)) {
    	  				    	  		
	    	  		IMarker marker = createCursorMarker(root, markerLine, path, charStart);

	    	  		IDE.openEditor(page, marker);
	    	  		marker.delete();
    	  			break;
	    	  	}
	    	  	//Open on 'normal' states or just transitions
	    	  	if (markerLine == linkLineNum && file.equals(fileName)  && !linkToArray[2].contains("transitionState") && !message.contains("transitionState")) {
	    	  		IMarker marker = createCursorMarker(root, markerLine, path, charStart);
	    	  		IDE.openEditor(page, marker);
	    	  		marker.delete();
    	  			break;
	    	  	}
	    	  	
		    }
	    	
	    	
		} catch (CoreException e) {
			System.out.println("Error linking back from diagram");
		}
	}
	
	/**
	 * Creates a new marker on the editor which points to the first char of the matched marker. 
	 * @param root - An IResource of the editor containing the diagramText
	 * @param markerLine - ...
	 * @param path - The path of the editor
	 * @param charStart - the firstChar of the matched marker
	 * @return - IMarker - the marker to be opened
	 */
	private static IMarker createCursorMarker(IResource root, int markerLine, String path, int charStart) throws CoreException {
		IMarker marker = root.createMarker("FSM.MARKER");
		marker.setAttribute(IMarker.MESSAGE, "");
		marker.setAttribute(IMarker.LINE_NUMBER, markerLine);
		marker.setAttribute(IMarker.SOURCE_ID, path.toString());
		marker.setAttribute(IMarker.CHAR_START, charStart);
		marker.setAttribute(IMarker.CHAR_END, charStart);
		return marker;
	}


	@Override
	public void openLink(LinkData link) {
		
		linkBackState(link.href);
		
	}
}