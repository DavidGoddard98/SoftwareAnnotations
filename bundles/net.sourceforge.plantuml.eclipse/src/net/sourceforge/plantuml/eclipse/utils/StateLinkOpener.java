package net.sourceforge.plantuml.eclipse.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;



public class StateLinkOpener implements ILinkOpener {
	
	@Override
	public int supportsLink(LinkData link) {	
		
		String href = link.href;
		
		if (href.contains("FSM")) {
			return STATE_SUPPORT;
		}
		return NO_SUPPORT;
	}

	
	private void linkBackState(String link) {
		try {
			String linkToArray[] = link.split("#");
		    String fileName = linkToArray[0];
		    
		    IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot wsRoot = workspace.getRoot();
			IResource resource = workspace.getRoot();
	        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	    	IMarker[] markers = resource.findMarkers("FSM.MARKER", true, IResource.DEPTH_INFINITE);
	    	System.out.println("linktoarray2" + linkToArray[2]);
	    	for (IMarker m : markers) {
	    		String path = (String)(m.getAttribute(IMarker.SOURCE_ID));
	
	    		IResource root = wsRoot.findMember(path);
	    		int charStart = (int)m.getAttribute(IMarker.CHAR_START);
	    		
	    		String[] tmp = path.split("/");
	    	  	String file = tmp[tmp.length-1];
	    	  	
	    	  	int markerLine = (int)m.getAttribute(IMarker.LINE_NUMBER);
		    	int linkLineNum = Integer.parseInt(linkToArray[3]) + 1;
		    	String message = (String)m.getAttribute(IMarker.MESSAGE);
		    	System.out.println("Message:" + message);
	    	  	if (markerLine == linkLineNum && file.equals(fileName) && linkToArray[2].equals(message)) {
	    	  		System.out.println(linkToArray[2] +  "== " + message);
    	  				    	  		
	    	  		
	    	  		IMarker marker = createCursorMarker(root, markerLine, path, charStart);

	    	  		IDE.openEditor(page, marker);
	    	  		marker.delete();
    	  			break;
	    	  	}
	    	  	if (markerLine == linkLineNum && file.equals(fileName)  && !linkToArray[2].contains("transitionState") && !message.contains("transitionState")) {
	    	  		System.out.println(linkToArray[2] +  "!= transitionState" );
	    	  		IMarker marker = createCursorMarker(root, markerLine, path, charStart);
	    	  		IDE.openEditor(page, marker);
	    	  		marker.delete();
    	  			break;
	    	  	}
	    	  	
	    	  	//!linkToArray[2].equals("transitionState")
		    }
	    	
	    	
		} catch (CoreException e) {
			System.out.println("Error linking back from diagram");
		}
	}
		
	private IMarker createCursorMarker(IResource root, int markerLine, String path, int charStart) throws CoreException {
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