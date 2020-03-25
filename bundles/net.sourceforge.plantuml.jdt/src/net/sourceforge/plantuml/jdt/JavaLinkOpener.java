package net.sourceforge.plantuml.jdt;

import java.io.File;
import java.net.URI;

import java.net.URISyntaxException;
import java.util.ArrayList;
import  java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.actions.OpenTypeAction;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorPipelineService;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;
import org.eclipse.ui.texteditor.ITextEditor;

import net.sourceforge.plantuml.eclipse.utils.ILinkOpener;
import net.sourceforge.plantuml.eclipse.utils.LinkData;

public class JavaLinkOpener implements ILinkOpener {

	public final static String JAVA_LINK_PREFIX = "java:";
	
	@Override
	public int supportsLink(LinkData link) {
		System.out.println("java");
		try {
			String href = link.href;
			if (href.contains("FSM")) {
				return STATE_SUPPORT;
			}
			URI uri = new URI(link.href);
			if ("java".equals(uri.getScheme()) && getJavaElement(link) != null) {
				return CUSTOM_SUPPORT;
			}
			
		} catch (URISyntaxException e) {
		}
		return NO_SUPPORT;
	}

	protected IJavaElement getJavaElement(LinkData link) {
		try {
			URI uri = new URI(link.href);
			String className = uri.getPath();
			if (className != null) {
				if (className.startsWith("/")) {
					className = className.substring(1);
				}
			} else {
				className = uri.getSchemeSpecificPart();
			}
			IType type = OpenTypeAction.findTypeInWorkspace(className, false);
			String fragment = uri.getFragment();
			if (fragment != null) {
				for (IJavaElement child : type.getChildren()) {
					if (fragment.equals(child.getElementName())) {
						return child;
					}
				}
			}
			return type;
		} catch (Exception e) {
			return null;
		}
	}
	
	private void linkBackState(String link) {
		//transition

		System.out.println(link);
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
		    			  if (markerLine == array[i])
		    				  markerSet.add(m);
		    		  }
		    	  }
		    	 
		      }
		      IDE.openEditor(page, markerSet.get(0));

		      
		      IMarker test = markerSet.get(0);
		      IResource markedResource = test.getResource();
		      ISelection selection = new StructuredSelection(markerSet);

		      
//		      
//		      IWorkbenchPart activePart = page.getActivePart();
//		      CommonViewer navigator = (CommonViewer) activePart;
//		      navigator.setSelection(selection, true);
	      } catch (CoreException e) {
	    	  
	      }
	    

	    }
		
	}
	
	

	@Override
	public void openLink(LinkData link) {
		try {
			linkBackState(link.href);
			IJavaElement javaElement = getJavaElement(link);
			JavaUI.openInEditor(javaElement);
		} catch (PartInitException e) {
		} catch (JavaModelException e) {
		}
	}
}
