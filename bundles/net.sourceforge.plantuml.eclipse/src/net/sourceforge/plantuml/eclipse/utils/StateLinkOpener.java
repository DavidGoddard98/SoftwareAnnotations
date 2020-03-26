package net.sourceforge.plantuml.eclipse.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
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
			IResource resource = workspace.getRoot();
	        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	    	IMarker[] markers = resource.findMarkers(IMarker.BOOKMARK, true, IResource.DEPTH_INFINITE);
	    	
	    	for (IMarker m : markers) {
	    		String path = (String)(m.getAttribute(IMarker.SOURCE_ID));
	    		String[] tmp = path.split("/");
	    	  	String file = tmp[tmp.length-1];
	    	  	file = file.substring(0, file.length() - 5);
	    	  	
	    	  	int markerLine = (int)m.getAttribute(IMarker.LINE_NUMBER);
		    	int linkLineNum = Integer.parseInt(linkToArray[2]) + 1;
		
	    	  	if (markerLine == linkLineNum && file.equals(fileName)) {
	    	  		IDE.openEditor(page, m);
	    		  	break;
	    	  	}
		    }
		} catch (CoreException e) {
			System.out.println("Error linking back from diagram");
	} 
	    
	    

	    
		
	}

	@Override
	public void openLink(LinkData link) {
		
		linkBackState(link.href);
		
	}
}