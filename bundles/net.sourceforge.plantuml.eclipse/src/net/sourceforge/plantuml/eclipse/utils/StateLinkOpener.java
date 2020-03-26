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
		//transition

	    String tmpArray[] = link.split("#");
	    String fileName = tmpArray[0];
	    
	    IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResource resource = workspace.getRoot();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	    //transition
	    if (tmpArray[2].equals("transition")) {

	      int transLineNum = Integer.parseInt(tmpArray[3]) + 1;
	      //do something with lineNum and class name...

	      try {
	    	  IMarker[] markers = resource.findMarkers(IMarker.BOOKMARK, true, IResource.DEPTH_INFINITE);
		      for (IMarker m : markers) {
		    	  String path = (String)(m.getAttribute(IMarker.SOURCE_ID));
		    	  String[] tmp = path.split("/");
		    	  String file = tmp[tmp.length-1];
		    	  file = file.substring(0, file.length() - 5);
		    	  int markerLine = (int)m.getAttribute(IMarker.LINE_NUMBER);
		    	  if (markerLine == transLineNum && file.equals(fileName)) {
		    		  IDE.openEditor(page, m);
		    		  break;
		    	  }
		      }
	      } catch (CoreException e) {
	    	  
	      }
	      
	   
	      
	    }

	    //state
	    if (tmpArray[2].equals("state")) {
	      String stateName = tmpArray[3];
	      String lineNums[] = tmpArray[4].split(",");
	      int[] array = Arrays.asList(lineNums).stream().mapToInt(Integer::parseInt).toArray();
	      List<IMarker> markerSet = new ArrayList<IMarker>();
	      try {
	    	  IMarker[] markers = resource.findMarkers(IMarker.BOOKMARK, true, IResource.DEPTH_INFINITE);
		      for (IMarker m : markers) {
		    	  String path = (String)(m.getAttribute(IMarker.SOURCE_ID));
		    	  String[] tmp = path.split("/");
		    	  String file = tmp[tmp.length-1];
		    	  file = file.substring(0, file.length() - 5);
		    	  
		    	  int markerLine = (int)m.getAttribute(IMarker.LINE_NUMBER);
		    	  if (file.equals(fileName)) {
		    		  for (int i = 0; i<array.length; i++) {
		    			  if (markerLine == array[i]) {
		    			      IDE.openEditor(page, m);
		    			      try {
			    			      int charStart = (int)m.getAttribute(IMarker.CHAR_START);
			    			      int charEnd = (int)m.getAttribute(IMarker.CHAR_END);
			    			    		  
			    			    		  
			    			      System.out.println("stae making marker");	  
			    				  IMarker marker = resource.createMarker("FSM.MARKER");
				    			  marker.setAttribute(IMarker.LINE_NUMBER, markerLine);
				    			  marker.setAttribute(IMarker.SOURCE_ID, path);
				    			    //if (region.getOffset() != 0) {
				    			  marker.setAttribute(IMarker.CHAR_START,charStart);
				    			  marker.setAttribute(IMarker.CHAR_END,charEnd);
		    			      } catch (NullPointerException e) {
		    			    	  System.out.println("NULL");
		    			      }
		    			  }
		    		  }
		    	  }
		    	 
		      }

		      
		    
	      } catch (CoreException e) {
	    	  
	      }
	    

	    }
		
	}

	@Override
	public void openLink(LinkData link) {
		
		linkBackState(link.href);
		
	}
}