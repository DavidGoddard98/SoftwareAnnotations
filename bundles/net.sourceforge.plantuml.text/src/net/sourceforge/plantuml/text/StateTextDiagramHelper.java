
package net.sourceforge.plantuml.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import net.sourceforge.plantuml.text.StateReference;
import net.sourceforge.plantuml.text.Transition;

public class StateTextDiagramHelper  {
	
	//The strings used to declare the start and end of a stateMachine
	final String prefix = "@start_state_machine", prefixRegex = prefix;
	final String suffix = "@end_state_machine", suffixRegex = suffix;
	
	//Stores the custom made keys created in 'createKey()' which reference different '//FSM:' lines
	private static HashSet<String> plantMarkerKey = new HashSet<String>();
	
	//These two arrays are explicitly linked. 
	//The first one contains the names of the different annotation markers that are used to highlight various transitions in different colors
	//The second one contains those marker colors as a string representation and are passed on to the plantuml library to color the transitions in their relevant colors.
	final protected static String[] allTransitionHighlights = {"FSM.Transition.Highlight_1", "FSM.Transition.Highlight_2", "FSM.Transition.Highlight_3", "FSM.Transition.Highlight_4", "FSM.Transition.Highlight_5", "FSM.Transition.Highlight_6"};
	final protected static String[] transitionColors = {"#Lime", "#Gold", "#FireBrick", "#HotPink", "#DarkOrchid", "#DarkGreen"};
	
	
	protected static String stateSelected; //stores a stateName if the selectedLine is a line containing a state description
	protected static int selectedLineNum;
		
	protected static StateDiagram stateDiagram = null;
	
	
	
	public StateTextDiagramHelper() {
		
	}
	
	
	
	//This class is used to store various information about an '//FSM:' line in the editor and is only used
	//when the user suffix's the line with '{'. This means that the user wants to highlight more than 
	//just the line, i.e. some code. This object is stored in a stack until a following '}' is found. 
	class PendingState extends StateTextDiagramHelper {
		String theLine;
		String editorLine;
		int lineNum;
		
		PendingState(String theLine, String editorLine, int lineNum) {
			this.theLine = theLine;
			this.editorLine = editorLine;
			this.lineNum = lineNum;
		}
	}

	/**
	 * The only function that gets called from out of this class. Therefore the entry point.
	 * This method is essentially on a loop as it gets called any time the user clicks within the //@state_state_machine and //end_state_machine, in the editor. 
	 * @param document - ...
	 * @param selectionStart - the offset of where the cursor last selected in the document
	 * @param markerAttributes - Map used to store annotations to highlight positions of diagramText
	 * @param editorInput - the editorInput for the active editor
	 * @return
	 */
	public StringBuilder getDiagramTextLines(IDocument document, final int selectionStart,
			final Map<String, Object> markerAttributes, IEditorInput editorInput) {
		final boolean includeStart = prefix.startsWith("@"), includeEnd = suffix.startsWith("@");


		final FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
		IPath path = ((IFileEditorInput) editorInput).getFile().getFullPath();
		IResource root = getRoot(editorInput);
		
		
		
		try {
			//The following document manipulation calculates the start/end of the descriptive region
			// search backward and forward start and end
			IRegion start = finder.find(selectionStart, prefixRegex, false, true, false, true);
			if (start == null) {
				// use a slightly larger selection offset, in case the cursor is within startuml
				int altSelectionStart = Math.min(selectionStart + prefix.length(), document.getLength());
				start = finder.find(altSelectionStart, prefixRegex, false, true, false, true);
				if (start == null) {
					altSelectionStart = Math.min(selectionStart + prefixRegex.length(), document.getLength());
					start = finder.find(altSelectionStart, prefixRegex, false, true, false, true);
				}
			}
			if (start != null) {
			
				final int startOffset = start.getOffset(),
						startLine = document.getLineOfOffset(startOffset + (includeStart ? 0 : start.getLength()));
	
				final IRegion end = finder.find(startOffset + start.getLength(), suffixRegex, true, true, false, true);
				if (end.getOffset() >= selectionStart) {
	
					final int endOffset = end.getOffset() + end.getLength();
					final int maxLine = Math.min(document.getLineOfOffset(endOffset) + (includeEnd ? 1 : 0),
							document.getNumberOfLines());
					///////////////////////////////////////////////////////////////////////////////////////////

					Region diagramRegion = new Region(startOffset, endOffset);
					selectedLineNum = document.getLineOfOffset(selectionStart);
					stateSelected = "";

					if (stateDiagram == null) {
						stateDiagram = new StateDiagram(finder, document, root, path);
						instantiateStateLinkMap(startLine, includeStart, startOffset, maxLine);
						instantiateTransitionStateMap();
			
						appendTransitionStates(selectionStart);
						appendStateAndTransitions();
						stateDiagram.lastRegion = diagramRegion;

					}
					else  {
						if (!diagramRegion.equals(stateDiagram.lastRegion)) { 
							//region changed therefore must reinitialize everything before constructing diagramText
							stateDiagram = new StateDiagram(finder, document, root, path);

							instantiateStateLinkMap(startLine, includeStart, startOffset, maxLine);
							instantiateTransitionStateMap();
							isStateSelected(); //if selectedLineNum == lineNum of a state then highlight all references to state
							appendTransitionStates(selectionStart);
							appendStateAndTransitions();

							stateDiagram.lastRegion = diagramRegion; 
						} else { //region not changed so use previously saved information and rebuild the diagramText
							stateDiagram.addedTransitions = new ArrayList<String>();
							stateDiagram.textualDiagram = new HashMap<String, ArrayList<String>>();
							stateDiagram.colorCounter = 0;
							isStateSelected(); 
							appendTransitionStates(selectionStart);
							appendStateAndTransitions();
							
							return stateDiagramAsString();
						}
					} 
					 
					markerAttributes.put(IMarker.CHAR_START, start.getOffset());

					return stateDiagramAsString();
				}
			}

		} catch (final BadLocationException e) {
		} catch (CoreException e) {
		}
		return null;
		
	}
	
	
	//-----------------------------------------------CONSTRUCTING THE DIAGRAM TEXT-----------------------------------------------------------
	
	/**
	 * Called iteratively on every line in the editor and checks whether it has '//FSM:' as its prefix
	 * @param 	String - the line as seen in the editor
	 * @return	String - null if the line is not a diagram descriptor, else return the line without the '//FSM:'
	 */
	protected static String stateDescriptor(String line) {
		
	    String theLine = line.replaceAll("\\s+", "").toLowerCase();
	    
	    if (theLine.length() <= 6) 
	    	return null;
	    
	    if (theLine.substring(0,6).equals("//fsm:")) {
	      int index = line.indexOf(":") + 1;
	      
    	  if (line.contains("{")) {
    		  int anotherIndex = line.indexOf("{"); //remove the curly bracket 
    		  return line.substring(index, anotherIndex).trim();
    	  }
	      
	      return line.substring(index, line.length()).trim();
	    }
	    return null;
	  }
	
	/**
	 * Called on every output from the function stateDescriptor(). 
	 * Creates StateReference and Transition objects which store crucial info on the descriptive line. These objects are used frequently thoughout the program
	 * @param line  - the string returned from stateDescriptor
	 * @param editorLine - the line as seen in the editor
	 * @param lineNum - ...
	 * @param multiLineEnd - if the user used a '{', this specifies the lineNum where they closed it with '}', else it = -1
	 * @param start - the offset of the start of the descriptive region

	 */
	protected static void appendToLists(String line, String editorLine, int lineNum, int multiLineEnd, int start) throws BadLocationException {
		////////////////////
		IRegion markerRegion = stateDiagram.finder.find(start, line, true, true, false, false);
		///////////////////
		int charStart;
		int charEnd;
		int index;
		ArrayList<StateReference> stateReferences = new ArrayList<StateReference>();

		//TRANSITIONS
		if (line.contains("->")) {
			Transition transition = new Transition();
			
			int markerRegionOffset = markerRegion.getOffset();
			
			index = line.indexOf("-") - 1;
			String leftState = line.substring(0, index).trim();
			
			int leftCharStart = markerRegionOffset; //find the char positions of the left state
			int leftCharEnd = markerRegionOffset + leftState.length();
			
			index = line.indexOf(">") + 1;
			int anotherIndex = line.length();
			if (line.contains(":"))
				anotherIndex = line.indexOf(":") - 1;
			
			String rightState = line.substring(index, anotherIndex).trim();
			int rightCharStart = markerRegionOffset + line.indexOf(rightState) ; //find the char positions of the right state
			int rightCharEnd = rightCharStart + rightState.length();
			
			if (multiLineEnd == -1) { //if the method is called with multiLineEnd=1, it means that the user didnt specify '{' and therefore it is not a multiline desc
				transition = new Transition(leftState, rightState, leftCharStart, leftCharEnd, rightCharStart, rightCharEnd);
	
			} else { 
				transition = new Transition(leftState, rightState, leftCharStart, leftCharEnd, rightCharStart, 
						rightCharEnd, rightCharEnd, stateDiagram.document.getLineOffset(multiLineEnd));
			}
			StateReference stateReference = new StateReference(line, editorLine, lineNum, transition);
				

			
			//ADD THE TRANSITION'S STATE REFERENCES
			//Adds them to a HashMap. key = stateName, and then for each state discovered there is an arraylist of 
			//state references for that state. Clearly for transitions there will be duplicates as a transition can have up to 2 states. 
			//therefore this state reference will be added to both states.
		
			if (stateDiagram.stateLinkers.containsKey(leftState)) { //checks if a key with that state name exists 
				stateReferences = stateDiagram.stateLinkers.get(leftState); //if it does then add it...
				stateReferences.add(stateReference);
				stateDiagram.stateLinkers.put(leftState, stateReferences);
				
			} else {

				stateReferences.add(stateReference); // otherwise create a new key and entry. 
				stateDiagram.stateLinkers.put(leftState, stateReferences);
			}
			stateReferences = new ArrayList<StateReference>();

			if (stateDiagram.stateLinkers.containsKey(rightState)) {

				stateReferences = stateDiagram.stateLinkers.get(rightState);
				stateReferences.add(stateReference);
				stateDiagram.stateLinkers.put(rightState, stateReferences);
			} else {

				stateReferences.add(stateReference);
				stateDiagram.stateLinkers.put(rightState, stateReferences);
			}
		//This else deals with the statements which arn't transitions such as...
		//State1 : a state
		//state State1 
		} else if (line.contains(":") || line.trim().substring(0, 5).equals("state")) {
			//STATES
			String stateName = "";
			StateReference stateReference = new StateReference();
		
			if (line.trim().substring(0, 5).equals("state")) {
			
				line = line.trim().substring(6);
				
				if (line.contains(" ")) {
					
					index = line.indexOf(" ");
					stateName = line.substring(0, index);
				} else {
					stateName = line;
				}
				
				charStart = markerRegion.getOffset();
				if (multiLineEnd == -1) { //again, if multiLineEnd = -1, then user has not used '{' at  the end and 
					charEnd = markerRegion.getOffset() + markerRegion.getLength(); //therefore not a multiline comment

				} else {
					charEnd = stateDiagram.document.getLineOffset(multiLineEnd);
				}
				
				stateReference = new StateReference(line, editorLine, lineNum, charStart, charEnd);
				
			} else if (line.contains(":")) {
				index = line.indexOf(":") - 1;
				stateName = line.substring(0, index).trim();
				charStart = markerRegion.getOffset();
				if (multiLineEnd == -1) {
					charEnd = markerRegion.getOffset() + markerRegion.getLength();

				} else {
					charEnd = stateDiagram.document.getLineOffset(multiLineEnd);
				}				
				stateReference = new StateReference(line, editorLine, lineNum, charStart, charEnd);
				
				
				
			}
			//Similar operation to the one above in transitions.. add the created stateReferences to the hashmap
			if (stateDiagram.stateLinkers.containsKey(stateName)) {
				stateReferences = stateDiagram.stateLinkers.get(stateName);
				stateReferences.add(stateReference);
				stateDiagram.stateLinkers.put(stateName, stateReferences);
				
			} else {
				stateReferences.add(stateReference);
				stateDiagram.stateLinkers.put(stateName, stateReferences);
			}
			stateDiagram.actualStates.add(stateName);

		}
			
	}
	
	
	/**
	 * Loops through all lines of the diagram region and determines if they're descriptive lines by calling stateDescriptor().
	 * If so, the line is then passed onto appendToLists, to be initialized as a StateReference and added to the hashMap stateLinkers. 
	 * The stack is used to determine the end of a multiline reference which is started with '{'. 
	 * @param startLine - the first line of the region
	 * @param includeStart - boolean to determine whether @start_state_diagram is included
	 * @param startOffset - offset position in document of first line
	 * @param maxLine - last line of region
	 */
	protected void instantiateStateLinkMap(int startLine, boolean includeStart, int startOffset, int maxLine) throws BadLocationException {

		Stack<PendingState> pendingStates = new Stack<PendingState>();
		IDocument document = stateDiagram.document;
		
		for (int lineNum = startLine + (includeStart ? 0 : 1); lineNum < maxLine; lineNum++) {
			final String line = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum)).trim();
			String newLine = stateDescriptor(line);
			if (newLine != null){
				
				if (line.contains("{")) {
					PendingState pendingState = new PendingState(newLine, line, lineNum);
					pendingStates.push(pendingState);
				}
				else if (line.contains("}") && pendingStates.size() > 0) {
					PendingState pendingState = pendingStates.pop();
					appendToLists(pendingState.theLine, pendingState.editorLine, pendingState.lineNum, lineNum, startOffset);
				} else if (line.contains("}")) { //no opening '{' so ignore - user error
					continue;
				} else {
					appendToLists(newLine, line, lineNum, -1, startOffset);
				}
				
			}
			
		}
	}
	
	//Filters stateLinkers map to create a new hashmap containing just transitionStates and their stateReferences
	protected static void instantiateTransitionStateMap() {
		for (Map.Entry<String, ArrayList<StateReference>> entry : stateDiagram.stateLinkers.entrySet()) {


			String stateName = entry.getKey();
			ArrayList<StateReference> stateReferences = entry.getValue();
			boolean onlyTransitions = true;
			for (StateReference stateReference : stateReferences) {
				if (!stateReference.isTransition) onlyTransitions = false;
			}
			
			if (onlyTransitions) stateDiagram.transitionStateReferences.put(stateName, stateReferences);
		}
	}
	
	
	//The following functions create new diagramText lines in order to create; -----------------------------------------------
	//1) Links from the diagram back to the editor. ---------------------------------------------------------------------------
	//2) Visualizations from the editor to the diagram by coloring parts of the diagram ---------------------------------------
	
	
	/**
	 * Creates links from the diagram back to the text for transitionStates.
	 * In other words for a transition state 'StateA' it appends - state StateA [[className#FSM#transitionStateStateA#lineNum]]
	 * It also checks if the cursor point == charStart of a transitionState - if so it is treated as selected all references to that state are highlighted
	 * @param selectionStart - the cursor selection point
	 */
	protected static void appendTransitionStates(int selectionStart) throws CoreException {
		int charStart = 0;
		int charEnd = 0;
		for (Map.Entry<String, ArrayList<StateReference>> entry : stateDiagram.transitionStateReferences.entrySet()) {

			String stateName = entry.getKey(); //a transitionState
			ArrayList<StateReference> stateReferences = entry.getValue();
			boolean stateLink = false;
			
			
			for (StateReference stateReference : stateReferences) {	
				Transition transition = stateReference.transition;
			
				if (transition.leftState.equals(stateName)) { //then leftState = transitionState
					charStart = transition.leftCharStart;
				    charEnd = transition.leftCharEnd;
				}
				else {
					charStart = transition.rightCharStart; //rightState = transitionState
					charEnd = transition.rightCharEnd;
				}
				
				//ADD THE LINKS FROM THE DIAGRAM STATE NODE TO THE TEXT
				if (!stateLink && !stateName.equals("[*]")) {
					int lineNum = stateReference.lineNum;
					String theLine = stateReference.editorLine;
					StateReference leftStateRef = new StateReference(theLine, "transitionState" + stateName, lineNum, charStart, charEnd);
					appendTextualDiagram(stateName, backwardsTransitionStateLink(leftStateRef, stateName)+ "\n");
					stateLink = true;
				}
				
				
				//Transition state selected highlight references in editor and color state cyan
				if (selectionStart == charStart ) {
					displayHighlights(stateName, stateReferences);
					appendTextualDiagram(stateName, forwardStateLink(stateName) + "\n");
				}
			}
			
		}
	}
	
	/**
	 * Creates links from the diagram back to the text for 'normal' states.
	 * AND visualizations from text to diagram for both 'normal states' & transitionStates. In other words...
	 * If 'StateA' is selected, then it will append  'state StateA #cyan' (colors the node cyan in the diagram)
	 * For transitions.. If the line StateA -> StateB is selected, it will append 'StateA -[thickness=5,#Lime]> StateB'
	 * @throws CoreException
	 */
	protected static void appendStateAndTransitions() throws CoreException {
		for (Map.Entry<String, ArrayList<StateReference>> entry : stateDiagram.stateLinkers.entrySet()) {


			String stateName = entry.getKey();
			ArrayList<StateReference> stateReferences = entry.getValue();
			for (StateReference stateReference : stateReferences) {
				String line = stateReference.theLine;
				int lineNum = stateReference.lineNum;
				
				
				createKey(stateReference);
				
				if (!stateReference.isTransition) {
					//STATE
					

					if (stateName.equals(stateSelected) ) { //color that node cyan
						String colorState = forwardStateLink(stateName);
						appendTextualDiagram(stateName, line + "\n");
						appendTextualDiagram(stateName, backwardStateLink(stateName, lineNum) + "\n");
						appendTextualDiagram(stateName, colorState + "\n");
						
					} else { //create the link from the diagram to the editor
						appendTextualDiagram(stateName, backwardStateLink(stateName, lineNum) + "\n");
			    		appendTextualDiagram(stateName, line + "\n");
					}
				}
				
				else  {
					//TRANSITION							
				
					//transitionDone ?
					if (stateDiagram.addedTransitions.contains(line)) continue;

					if (selectedLineNum == lineNum) { //color that transition lime and create a link back to line in editor
						
						String colorTransition = forwardTransitionLink(line);
						appendTextualDiagram(stateName, backwardTransitionLink(colorTransition, lineNum, line) + "\n");
						displayTransitionStateTransitionMarkers(stateReference);

						
					} else { //dont color transition but create the link back
						appendTextualDiagram(stateName, backwardTransitionLink(line, lineNum, line) + "\n");
					}
					stateDiagram.addedTransitions.add(line);
				}

			}	
			
		}
	}
	
	//Link from the editor to the diagram for states
	protected static String forwardStateLink(String stateName) {
		return "state " + stateName + " #Cyan";
	}

	//Link from the editor to the diagram for transitions
	protected static String forwardTransitionLink(String selectedLine) {
		String colorTransition = "";
		int indexOfArrow = selectedLine.indexOf("-");
		String theArrow = selectedLine.substring(indexOfArrow+1, indexOfArrow +2);
		String transColor = transitionColors[stateDiagram.colorCounter];
		colorTransition = selectedLine.substring(0, indexOfArrow) +  "-[thickness=5,"+transColor+"]" + theArrow + selectedLine.substring(indexOfArrow +2, selectedLine.length());
		return colorTransition;
	}

	//create link from diagram to the editor for a state
	protected static String backwardStateLink(String stateName, int lineNum) {
		String link = "state " + stateName + "[["+stateDiagram.className+"#FSM#state#"+ lineNum +"]]";
		return link;

	}
	
	//create a link from diagram to the editor for a transitionState
	protected static String backwardsTransitionStateLink(StateReference stateReference, String stateName) throws CoreException {
		createKey(stateReference);
		String link = "state " + stateName + "[["+stateDiagram.className+"#FSM#"+stateReference.editorLine+"#"+stateReference.lineNum+"]]";
		return link;
	}

	//create a link from diagram to the editor for a transition
	protected static String backwardTransitionLink(String aLine, int lineNum, String originalLine) {
		if (aLine.contains(":")) {
			int index = aLine.indexOf(":");
			String transitionLabel = aLine.substring(index + 1, aLine.length());
			return aLine.substring(0, index -1) + " : " + "[["+stateDiagram.className+"#FSM#transition#"+lineNum+ transitionLabel +"]]";
		}
		
		return aLine + " : " + "[["+stateDiagram.className+"#FSM#transition#"+lineNum+" "+originalLine+"]]";

	}
	
	
	/**
	 * Similar to the hashmap used to store the statereferences, this does the same but instead of statereferences it stores the line as a string.
	 * Essentially it stores all of the lines used to describe the diagram and once they have all been collected it iterates through them appending
	 * them to a StringBuilder to be sent to the plant uml library. This is done so that the diagram remains consistent (the positioning of its components).
	 * I was getting various errors as the order of the lines matters. 
	 * @param stateName
	 * @param line
	 */
	protected static void appendTextualDiagram(String stateName, String line) {
		//all textual descs for this state
		ArrayList<String> stateTextualDesc = new ArrayList<String>();

		if (stateDiagram.textualDiagram.containsKey(stateName)) {
			stateTextualDesc = stateDiagram.textualDiagram.get(stateName);
			stateTextualDesc.add(line);
			stateDiagram.textualDiagram.put(stateName, stateTextualDesc);
			
		} else {
			stateTextualDesc.add(line);
			stateDiagram.textualDiagram.put(stateName, stateTextualDesc);
		}
	}
	
	//Loops through the textualDiagram map and constructs the diagramtext as a StringBuilder. This is what is returned from getDiagramTextLines()
	protected static StringBuilder stateDiagramAsString() {
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, ArrayList<String>> entry : stateDiagram.textualDiagram.entrySet()) {
			ArrayList<String> textualDesc = entry.getValue();
			for (String text : textualDesc) {
				result.append(text);
			}
		}
		return result;
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//-----------------------------------------------MARKER AND KEY CREATION-----------------------------------------------------------------------
	
	//Deals with the creation, maintenance and deletion of FSM.MARKER's. 
	//The markers are created for every state, transitionState and transition and essentially track their positions in the document. 
	//They are an essential part of linking back from the diagram to the text and are used by the class StateLinkOpener.java in 'net.sourceforge.plantuml.eclipse' package.
	//The keys ensure duplicate markers are not created and allow for the markers to be updated if aspects of their positioning i.e lineNum change.
	
	
	//A key is created for every diagram text line and if it doesnt exist then a marker of that line and the relevant positions is created.
	protected static void createKey(StateReference stateReference) throws CoreException {
		
		String theLine = stateReference.editorLine;
		int lineNum = stateReference.lineNum;
		
		int charStart;
		int charEnd;
		if (stateReference.isTransition) {
			charStart = stateReference.transition.leftCharStart + 1;
			charEnd = stateReference.transition.rightCharEnd;
		} else {
			charStart = stateReference.charStart;
			charEnd = stateReference.charEnd;	
		}
		 

		String key = stateDiagram.className + theLine + String.valueOf(lineNum + 1) + String.valueOf(charStart) + String.valueOf(charEnd);
		if (plantMarkerKey.contains(key)) {
			System.out.println("Key exists");
			return;
		}
		if (possibleChangedMarker(theLine, lineNum, charStart, charEnd)) { //returns true if the diagram text hasnt changed but its positions had. It updates the key + marker
			System.out.println("marker changed");
			return;
		}
		//else brand new line so create new marker and key
		System.out.println("key doesnt exist, adding key");
		plantMarkerKey.add(key);
		addMarkerTask(theLine, lineNum, charStart, charEnd);
		
	}

	//creates a Marker of type FSM.MARKER
	protected static void addMarkerTask(String theLine, int lineNum, int charStart, int charEnd) throws CoreException {

		IMarker marker = stateDiagram.root.createMarker("FSM.MARKER");
		marker.setAttribute(IMarker.MESSAGE, theLine);
		marker.setAttribute(IMarker.LINE_NUMBER, lineNum + 1);
		marker.setAttribute(IMarker.SOURCE_ID, stateDiagram.path.toString());
		marker.setAttribute(IMarker.CHAR_START, charStart);
		marker.setAttribute(IMarker.CHAR_END, charEnd);
	}
	
	
	/**
	 * Checks the possibility that the marker positions could have changed for a particular diagramText.
	 * If so it deletes the old marker and key and creates new ones with the updated positions. 
	 * @param theLine - the line in the editor
	 * @param lineNum - ...
	 * @param charStart - start offset of the transition/state
	 * @param charEnd - end offset of the transition/state
	 * @return
	 */
	protected static boolean possibleChangedMarker(String theLine, int lineNum, int charStart, int charEnd) {
		try {
			IMarker[] allMarkers;
		
			allMarkers = stateDiagram.root.findMarkers("FSM.MARKER", false, IResource.DEPTH_ZERO);
			
			
			String path = stateDiagram.path.toString();
			for (IMarker aMarker : allMarkers) {
		
				String markerMessage = (String)aMarker.getAttribute(IMarker.MESSAGE);
				
				if (theLine.contains("transitionState") && !theLine.equals(markerMessage)) continue;
				
				int markerLine = (int)aMarker.getAttribute(IMarker.LINE_NUMBER);
				
				String markerPath = (String)aMarker.getAttribute(IMarker.SOURCE_ID);
				String[] tmp = markerPath.split("/");
				String markerClassName = tmp[tmp.length-1];

				int markerCharStart = (int)aMarker.getAttribute(IMarker.CHAR_START);
				int markerCharEnd = (int)aMarker.getAttribute(IMarker.CHAR_END);
				
				IDocument document = stateDiagram.document;
				String sameLineInDoc = document.get(document.getLineOffset(markerLine-1), document.getLineLength(markerLine-1)).trim();
				
				//Crossreferences old marker on that line with the diagramText on that line to see if different
				if (markerLine == (lineNum + 1) && markerPath.equals(path)) {
					if (!markerMessage.equals(theLine) || markerCharStart != charStart || markerCharEnd != charEnd) {
						String oldKey = markerClassName + markerMessage + String.valueOf(markerLine)  + String.valueOf(markerCharStart) + String.valueOf(markerCharEnd);
						String newKey = stateDiagram.className + theLine + String.valueOf(lineNum + 1) + String.valueOf(charStart) +  String.valueOf(charEnd);
						//delete old marker
						aMarker.delete();
						//and its key...
						plantMarkerKey.remove(oldKey);
						
						//create new marker
						addMarkerTask(theLine, lineNum, charStart, charEnd);
						// and its new key
						plantMarkerKey.add(newKey);
						return true;
					}
				}
				
				//Finds the marker with the same message as the diagram text and updates it if linNum or offsets have changed
				if (markerPath.equals(path) && markerMessage.equals(theLine)) {
					if (markerLine != lineNum + 1 || markerCharStart != charStart || markerCharEnd != charEnd) {
						String oldKey = markerClassName + markerMessage + String.valueOf(markerLine)  + String.valueOf(markerCharStart) + String.valueOf(markerCharEnd);
						String newKey = stateDiagram.className + theLine + String.valueOf(lineNum + 1) + String.valueOf(charStart) +  String.valueOf(charEnd);

						//delete old marker
						aMarker.delete();
						//and its key...
						plantMarkerKey.remove(oldKey);
						
						//create new marker
						addMarkerTask(theLine, lineNum, charStart, charEnd);
						// and its new key
						plantMarkerKey.add(newKey);
						return true;
					}
				}
				
				//line deleted - delete marker and key
				if (!sameLineInDoc.equals(markerMessage)) {
					String oldKey = markerClassName + markerMessage + String.valueOf(markerLine)  + String.valueOf(markerCharStart) + String.valueOf(markerCharEnd);
					
					//delete key
					plantMarkerKey.remove(oldKey);
					//and marker 
					aMarker.delete();
				}
				
			}
				
		} catch (CoreException e) {
			System.out.println("Failed to initialise keys");
		} catch (BadLocationException e) {
			System.out.println("Cant find same line in doc");
		}
		return false;

	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	//-----------------------------------------------HIGHLIGHTING PARTS OF DOCUMENT-----------------------------------------------------------
	
	
	//Loops through stateLinkers map to determine if the selectedLine is equal to a state descriptive line. If so, then all references to that state
	//are highlighted.
	protected static void isStateSelected() throws CoreException { 
		for (Map.Entry<String, ArrayList<StateReference>> entry : stateDiagram.stateLinkers.entrySet()) {


			String stateName = entry.getKey();
			ArrayList<StateReference> stateReferences = entry.getValue();
			for (StateReference stateReference : stateReferences) {
				int lineNum = stateReference.lineNum;
				
				if (!stateReference.isTransition && lineNum == selectedLineNum) {
					stateSelected = stateName;
					displayHighlights(stateName, stateDiagram.stateLinkers.get(stateName));
				}
			}
		}
	}
	
	//NOTE: The name given to states which don't have a direct reference - i.e. are only referenced within transitions, is transitionState.
	/**
	 * Yes the name is correct. For a given transtionState, this highlights all other transitions which reference that transitionState
	 * @param stateReference
	 */
	protected static void displayTransitionStateTransitionMarkers(StateReference stateReference) throws CoreException {
		int lineNum = stateReference.lineNum;
		Transition transition = stateReference.transition;
		
		int charStart = transition.leftCharStart;
		int charEnd = transition.leftCharEnd;
		addHighlightState(lineNum, charStart, charEnd);
		
		int charTransStart = charStart + stateReference.theLine.indexOf("-");
		int charTransEnd = charStart + stateReference.theLine.indexOf(">") + 1;
		addHighlightTransition(lineNum, charTransStart, charTransEnd);
		
		charStart = transition.rightCharStart;
		charEnd = transition.rightCharEnd;
		addHighlightState(lineNum, charStart, charEnd);
		
		
		if (transition.multiLineTransition) {
			int multiLineStart = transition.multiLineStart;
			int multiLineEnd = transition.multiLineEnd;
			addHighlightTransition(lineNum, multiLineStart, multiLineEnd);
		}
	}
	
	/**
	 * For a given state, this highlights all references to that state in the editor
	 * @param stateName - ...
	 * @param stateReferences - all state references of that state - i.e any mention of it in the editor. 
	 */
	protected static void displayHighlights(String stateName, ArrayList<StateReference> stateReferences) throws CoreException {

		int charStart, charEnd, charTransStart, charTransEnd;

		for (StateReference stateReference : stateReferences) {
			int lineNum = stateReference.lineNum;
			
			if (stateReference.isTransition) {
				Transition transition = stateReference.transition;
				String theLine = stateReference.theLine;
				
				//construct the positions of which parts of the line to highlight
				if (transition.leftState.equals(stateName) && transition.rightState.equals(stateName)) {
					charStart = transition.leftCharStart;
					charEnd = transition.leftCharEnd;
					addHighlightState(lineNum, charStart, charEnd);
					charStart = transition.rightCharStart;
					charTransStart = charEnd +1; //the positions of the transition.. needed to color transition diff col to state
					charTransEnd = charStart - 1;
					charEnd = transition.rightCharEnd;
					
				}
				else if (transition.leftState.equals(stateName)) {
					charStart = transition.leftCharStart;
					charEnd = transition.leftCharEnd;
					charTransStart = charEnd ;
					charTransEnd = transition.rightCharEnd;

				} else {
					charStart = transition.rightCharStart;
					charEnd = transition.rightCharEnd;
					charTransStart = transition.leftCharStart;
					charTransEnd = charStart -1;

				}
				addHighlightTransition(lineNum, charTransStart, charTransEnd);

				if (transition.multiLineTransition) {
					int multiLineStart = transition.multiLineStart;
					int multiLineEnd = transition.multiLineEnd;
					addHighlightTransition(lineNum, multiLineStart, multiLineEnd);
				} 

				//Color the transitions on the diagram in the same colors you colored the transitions in the editor
				String colorTransition = forwardTransitionLink(stateReference.theLine);
				appendTextualDiagram(stateName, backwardTransitionLink(colorTransition, lineNum, stateReference.theLine) + "\n");
				stateDiagram.addedTransitions.add(theLine);
				stateDiagram.colorCounter++;
				
			} else {
				charStart = stateReference.charStart;
				charEnd = stateReference.charEnd;
			}
			addHighlightState(lineNum, charStart, charEnd);

		}					
	}
	
	//Highlights states in the editor
	protected static void addHighlightState(int lineNum, int charStart, int charEnd) throws CoreException {
		
		IMarker marker = stateDiagram.root.createMarker("FSM.State.Highlight");
        marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
        marker.setAttribute(IMarker.SOURCE_ID, stateDiagram.path.toString());
        marker.setAttribute(IMarker.CHAR_START,charStart);
        marker.setAttribute(IMarker.CHAR_END,charEnd);
	}
	
	//Highlights transitions in the editor
	protected static void addHighlightTransition(int lineNum, int multiLineStart, int multiLineEnd) throws CoreException {
		
		if (stateDiagram.colorCounter == 6) stateDiagram.colorCounter = 0; //loop through colors again
		String transitionHighlighter = allTransitionHighlights[stateDiagram.colorCounter];
		IMarker marker = stateDiagram.root.createMarker(transitionHighlighter );
        marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
        marker.setAttribute(IMarker.SOURCE_ID, stateDiagram.path.toString());
        marker.setAttribute(IMarker.CHAR_START,multiLineStart);
        marker.setAttribute(IMarker.CHAR_END,multiLineEnd);
	}
	
	
	//Clears all of the highlights in the editor
	protected static void removeHighlights(IResource resource) {
		try {
			IMarker[] markers = resource.findMarkers("FSM.State.Highlight", true, IResource.DEPTH_INFINITE);
			for (IMarker m : markers) {
				m.delete();
			}
			for (int j=0; j<allTransitionHighlights.length; j++) {
				markers = resource.findMarkers(allTransitionHighlights[j], true, IResource.DEPTH_INFINITE);
				for (IMarker m : markers) {
					m.delete();
				}
			}
			
		} catch (CoreException e) {
			System.out.println("Couldnt remove highlights");
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	
	/**
	 * Obtain the resource given an editor
	 * @param editorInput  - the active editor
	 * @return - IResource root (used for marker creation primarily)
	 */
	protected static IResource getRoot(IEditorInput editorInput) { 
		IPath path = ((IFileEditorInput) editorInput).getFile().getFullPath();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot wsRoot = workspace.getRoot();
		return wsRoot.findMember(path);

	}
	
	
	
	
	

}
