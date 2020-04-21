package net.sourceforge.plantuml.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;

//Stores relevant information about the latest diagram rendered. Is reinitialized each time the region containing the diagram descriptions
//is changed
public class StateDiagram extends StateTextDiagramHelper {
	protected HashMap<String, ArrayList<StateReference>> stateLinkers; //stores the information of all the descriptive lines including charStart, charEnd, linenums ect.
	HashMap<String, ArrayList<StateReference>>transitionStateReferences; //filters the map above and only stores the information of states only referenced in transitions

	protected HashMap<String, LinkedHashSet<String>> textualDiagram; //a map of strings that will eventually make up the string sent to plantuml
	protected ArrayList<String> addedTransitions; //a list of transitions already added - prevents duplicates occuring in diagram
	protected ArrayList<String> actualStates; //states that are not only referenced in transitions..i.e 'State8 : a state'
	
	protected final FindReplaceDocumentAdapter finder;
	protected final IDocument document;
	protected final IResource root;
	protected final IPath path;
	protected Region lastRegion;
	
	protected String className;
	protected int colorCounter; //used to progressively iterate through the above arrays.
	
	StateDiagram(FindReplaceDocumentAdapter finder, IDocument document, IResource root, IPath path) {
		this.stateLinkers = new HashMap<String, ArrayList<StateReference>>();
		this.transitionStateReferences  = new HashMap<String, ArrayList<StateReference>>();
		this.textualDiagram = new HashMap<String, LinkedHashSet<String>> (); 
		this.addedTransitions =  new ArrayList<String>();
		this.actualStates = new ArrayList<String>();
		
		
		this.lastRegion = null;
		this.finder = finder;
		this.document = document;
		this.root = root;
		this.path = path;
		
		this.className = path.toFile().getName();
		this.colorCounter = 0;
	}
	
}

