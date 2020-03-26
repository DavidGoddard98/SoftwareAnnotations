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
		try {
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
	
	

	@Override
	public void openLink(LinkData link) {
		try {
			IJavaElement javaElement = getJavaElement(link);
			JavaUI.openInEditor(javaElement);
		} catch (PartInitException e) {
		} catch (JavaModelException e) {
		}
	}
}
