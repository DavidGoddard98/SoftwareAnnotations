
package net.sourceforge.plantuml.text;

import java.io.IOException;
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
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.folding.IJavaFoldingStructureProviderExtension;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

public class StateTextDiagramHelper  {
	
	//The strings used to declare the start and end of a stateMachine
	final String prefix = "@start_state_machine", prefixRegex = prefix;
	final String suffix = "@end_state_machine", suffixRegex = suffix;
	
	int selectedLineNum;
	String stateSelected;
	private ArrayList<String> addedTransitions;
	
	//Stores the custom made keys created in 'createKey()' which reference different '//FSM:' lines
	private static HashSet<String> plantMarkerKey = new HashSet<String>();
	
	//These two arrays are explicitly linked. 
	//The first one contains the names of the different annotation markers that are used to highlight various transitions in different colors
	//The second one contains those marker colors as a string representation and are passed on to the plantuml library to color the transitions in their relevant colors.
	private static String[] allTransitionHighlights = {"FSM.Transition.Highlight_1", "FSM.Transition.Highlight_2", "FSM.Transition.Highlight_3", "FSM.Transition.Highlight_4", "FSM.Transition.Highlight_5", "FSM.Transition.Highlight_6"};
	private static String[] transitionColors = {"#Lime", "#Gold", "#FireBrick", "#HotPink", "#DarkOrchid", "#DarkGreen"};
	private int k = 0; //used to progressively iterate through the above arrays.
	
	//used to maintain consistency of diagram structure
	HashMap<String, ArrayList<String>> textualDiagram;

	
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
	
	//Class used to store all state machine references in the editor such as their lineNum, charStart and end....
	class StateReference extends StateTextDiagramHelper {
		String theLine;
		String editorLine;
		int lineNum;
		int charStart;
		int charEnd;
		boolean isTransition;
		Transition transition;
		
		StateReference(String theLine, String editorLine, int lineNum, int charStart, int charEnd) {
			this.theLine = theLine;
			this.editorLine = editorLine;
			this.lineNum = lineNum;
			this.charStart = charStart;
			this.charEnd = charEnd;
			this.isTransition = false;
		}
		
		StateReference(String theLine, String editorLine, int lineNum, Transition transition) {
			this.theLine = theLine;
			this.editorLine = editorLine;
			this.lineNum = lineNum;
			this.isTransition = true;
			this.transition = transition;
		}
		
		StateReference() {
			
		}
		
		public String toString() {
			return this.theLine;
		}
	}
	
	//Class to describe transitions
	class Transition extends StateTextDiagramHelper {
		String leftState;
		int leftCharStart;
		int leftCharEnd;
	
		String rightState;
		int rightCharStart;
		int rightCharEnd;
		
		int multiLineStart;
		int multiLineEnd;
		boolean multiLineTransition;
		
		Transition(String leftState, String rightState, int leftCharStart, int leftCharEnd, int rightCharStart, int rightCharEnd) {
			this.leftState = leftState;
			this.rightState = rightState;
			this.leftCharStart = leftCharStart;
			this.leftCharEnd = leftCharEnd;
			this.rightCharStart = rightCharStart;
			this.rightCharEnd = rightCharEnd;
			this.multiLineTransition = false;
		}
		
		Transition(String leftState, String rightState, int leftCharStart, int leftCharEnd, int rightCharStart, int rightCharEnd, int multiLineStart, int multiLineEnd) {
			this.leftState = leftState;
			this.rightState = rightState;
			this.leftCharStart = leftCharStart;
			this.leftCharEnd = leftCharEnd;
			this.rightCharStart = rightCharStart;
			this.rightCharEnd = rightCharEnd;
			this.multiLineStart = multiLineStart;
			this.multiLineEnd = multiLineEnd;
			this.multiLineTransition = true;
		}
		
		
		Transition() {
			
		}
		
		public String toString() {
			return this.leftState + " -> " + this.rightState;
		}
		
		
	}
	
	/**
	 * Called iteratively on every line in the editor and checks whether it has '//FSM:' as its prefix
	 * @param 	String - the line as seen in the editor
	 * @return	String - null if the line is not a diagram descriptor, else return the line without the '//FSM:'
	 */
	private String stateDescriptor(String line) {
		
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
	 * @param actualStates - list of all states so far
	 * @param stateLinkers - datatype used to store the statereferences and link them to their state name
	 * @param finder - 
	 * @param document
	 */
	private void appendToLists(String line, String editorLine, int lineNum, int multiLineEnd, ArrayList<String> actualStates,
			HashMap<String, ArrayList<StateReference>> stateLinkers, FindReplaceDocumentAdapter finder, int start, IDocument document) throws BadLocationException {
		////////////////////
		IRegion markerRegion = finder.find(start, line, true, true, false, false);
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
				transition = new Transition(leftState, rightState, leftCharStart, leftCharEnd, rightCharStart, rightCharEnd, rightCharEnd, document.getLineOffset(multiLineEnd));
			}
			StateReference stateReference = new StateReference(line, editorLine, lineNum, transition);
				

			
			//ADD THE TRANSITION'S STATE REFERENCES
			//Adds them to a HashMap. key = stateName, and then for each state discovered there is an arraylist of 
			//state references for that state. Clearly for transitions there will be duplicates as a transition can have up to 2 states. 
			//therefore this state reference will be added to both states.
		
			if (stateLinkers.containsKey(leftState)) { //checks if a key with that state name exists 
				stateReferences = stateLinkers.get(leftState); //if it does then add it...
				stateReferences.add(stateReference);
				stateLinkers.put(leftState, stateReferences);
				
			} else {

				stateReferences.add(stateReference); // otherwise create a new key and entry. 
				stateLinkers.put(leftState, stateReferences);
			}
			stateReferences = new ArrayList<StateReference>();

			if (stateLinkers.containsKey(rightState)) {

				stateReferences = stateLinkers.get(rightState);
				stateReferences.add(stateReference);
				stateLinkers.put(rightState, stateReferences);
			} else {

				stateReferences.add(stateReference);
				stateLinkers.put(rightState, stateReferences);
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
					charEnd = document.getLineOffset(multiLineEnd);
				}
				
				stateReference = new StateReference(line, editorLine, lineNum, charStart, charEnd);
				
			} else if (line.contains(":")) {
				index = line.indexOf(":") - 1;
				stateName = line.substring(0, index).trim();
				charStart = markerRegion.getOffset();
				if (multiLineEnd == -1) {
					charEnd = markerRegion.getOffset() + markerRegion.getLength();

				} else {
					charEnd = document.getLineOffset(multiLineEnd);
				}				
				stateReference = new StateReference(line, editorLine, lineNum, charStart, charEnd);
				
				
				
			}
			//Similar operation to the one above in transitions.. add the created stateReferences to the hashmap
			if (stateLinkers.containsKey(stateName)) {
				stateReferences = stateLinkers.get(stateName);
				stateReferences.add(stateReference);
				stateLinkers.put(stateName, stateReferences);
				
			} else {
				stateReferences.add(stateReference);
				stateLinkers.put(stateName, stateReferences);
			}
			if (lineNum == selectedLineNum) stateSelected = stateName;
			actualStates.add(stateName);

		}
			
	}
	
	//NOTE: The name given to states which don't have a direct reference - i.e. are only referenced within transitions, is transitionState.
	/**
	 * Yes the name is correct. For a transtionState, this highlights all other transitions which reference that transitionState
	 * @param stateReference
	 * @param path
	 * @param root
	 * @throws CoreException
	 */
	private void displayTransitionStateTransitionMarkers(StateReference stateReference, IPath path, IResource root) throws CoreException {
		int lineNum = stateReference.lineNum;
		Transition transition = stateReference.transition;
		
		int charStart = transition.leftCharStart;
		int charEnd = transition.leftCharEnd;
		addHighlightTask(lineNum, charStart, charEnd, root, path);
		
		int charTransStart = charStart + stateReference.theLine.indexOf("-");
		int charTransEnd = charStart + stateReference.theLine.indexOf(">") + 1;
		addHighlightTransition(lineNum, charTransStart, charTransEnd, root, path);
		
		charStart = transition.rightCharStart;
		charEnd = transition.rightCharEnd;
		addHighlightTask(lineNum, charStart, charEnd, root, path);
		
		
		if (transition.multiLineTransition) {
			int multiLineStart = transition.multiLineStart;
			int multiLineEnd = transition.multiLineEnd;
			addHighlightTransition(lineNum, multiLineStart, multiLineEnd, root, path);
		}
	}
	
	//Similar to the hashmap used to store the statereferences, this does the same but instead of statereferences it stores the line as a string.
	//Essentially it stores all of the lines used to describe the diagram and once they have all been collected it iterates through them appending
	//them to a StringBuilder to be sent to the plant uml library. This is done so that the diagram remains consistent (the positioning of its components).
	// I was getting various errors as the order of the lines matters. 
	private void appendTextualDiagram(String stateName, String line) {
		//all textual descs for this state
		ArrayList<String> stateTextualDesc = new ArrayList<String>();

		if (textualDiagram.containsKey(stateName)) {
			stateTextualDesc = textualDiagram.get(stateName);
			stateTextualDesc.add(line);
			textualDiagram.put(stateName, stateTextualDesc);
			
		} else {
			stateTextualDesc.add(line);
			textualDiagram.put(stateName, stateTextualDesc);
		}
	}
	
	//This method is essentially on a loop as it gets called any time the user clicks within the //@state_state_machine and //end_state_machine, in the editor. 
	//
	public StringBuilder getDiagramTextLines(final IDocument document, final int selectionStart,
			final Map<String, Object> markerAttributes, IEditorInput editorInput) {
		final boolean includeStart = prefix.startsWith("@"), includeEnd = suffix.startsWith("@");
		final FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
		IPath path = ((IFileEditorInput) editorInput).getFile().getFullPath();
		IResource root = getRoot(editorInput);
		try {

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
					StringBuilder result = new StringBuilder();
					final int maxLine = Math.min(document.getLineOfOffset(endOffset) + (includeEnd ? 1 : 0),
							document.getNumberOfLines());
				
					Stack<PendingState> pendingStates = new Stack<PendingState>();
					HashMap<String, ArrayList<StateReference>> stateLinkers = new HashMap<String, ArrayList<StateReference>> (); 
				    ArrayList<String> actualStates = new ArrayList<String>();
				    textualDiagram = new HashMap<String, ArrayList<String>> (); 
					stateSelected = null;
					selectedLineNum = document.getLineOfOffset(selectionStart);

				    k = 0;

					stateSelected = null;

				    
					for (int lineNum = startLine + (includeStart ? 0 : 1); lineNum < maxLine; lineNum++) {
						final String line = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum)).trim();
						String newLine = stateDescriptor(line);
						if (newLine != null){
							
							if (line.contains("}") && pendingStates.size() > 0) {
								PendingState pendingState = pendingStates.pop();
								appendToLists(pendingState.theLine, pendingState.editorLine, pendingState.lineNum, lineNum, actualStates, stateLinkers, finder, startOffset, document);
							} else if (line.contains("}")) { //no opening '{' so ignore - user error
								continue;
							} else if (line.contains("{")) {
								PendingState pendingState = new PendingState(newLine, line, lineNum);
								pendingStates.push(pendingState);
							} else {
								appendToLists(newLine, line, lineNum, -1, actualStates, stateLinkers, finder, startOffset, document);
							}
							
						}
						
					}
					
					IJavaFoldingStructureProviderExtension extension = (IJavaFoldingStructureProviderExtension) JavaPlugin.getDefault().getFoldingStructureProviderRegistry().getCurrentFoldingProvider();
					extension.collapseComments();
					String className = path.toFile().getName();
					
					
					
					
					
					String stateName; 
					String line;
					int lineNum; 
					ArrayList<StateReference> stateReferences;
					addedTransitions = new ArrayList<String>();
					
					HashMap<String, ArrayList<StateReference>>transitionStateReferences = new HashMap<String, ArrayList<StateReference>>();
					for (Map.Entry<String, ArrayList<StateReference>> entry : stateLinkers.entrySet()) {


						stateName = entry.getKey();
						stateReferences = entry.getValue();
						boolean onlyTransitions = true;
						for (StateReference stateReference : stateReferences) {
							if (!stateReference.isTransition) onlyTransitions = false;
						}
						
						if (onlyTransitions) transitionStateReferences.put(stateName, stateReferences);
					}
					
					
					//TRANSITION STATEREF LOOP//
					String theLine;
					int charStart = 0;
					int charEnd = 0;
					for (Map.Entry<String, ArrayList<StateReference>> entry : transitionStateReferences.entrySet()) {

						stateName = entry.getKey();
						stateReferences = entry.getValue();
						boolean stateLink = false;
						
						
						for (StateReference stateReference : stateReferences) {	
							Transition transition = stateReference.transition;
						
							if (transition.leftState.equals(stateName)) {
								charStart = transition.leftCharStart;
							    charEnd = transition.leftCharEnd;
							}
							else {
								charStart = transition.rightCharStart;
								charEnd = transition.rightCharEnd;
							}
							
							//ADD THE LINKS FROM THE STATE NODE TO THE TEXT
							if (!stateLink && !stateName.equals("[*]")) {
								lineNum = stateReference.lineNum;
								theLine = stateReference.editorLine;
								StateReference leftStateRef = new StateReference(theLine, "transitionState" + stateName, lineNum, charStart, charEnd);
								appendTextualDiagram(stateName, backwardsTransitionStateLink(leftStateRef, stateName, className, path, document, root)+ "\n");
								stateLink = true;
							}
							
							
							//ADD THE LINKS FROM THE TRANSITION TO THE TEXT AND FROM THE TEXT TO THE TRANSITION
							if (selectionStart == charStart ) {
								displayHighlights(stateName, stateReferences, className, path, root);
								appendTextualDiagram(stateName, forwardStateLink(stateName) + "\n");
							}
						}
						
					}
					
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
					if (stateSelected != null ) displayHighlights(stateSelected, stateLinkers.get(stateSelected), className, path, root );

					for (Map.Entry<String, ArrayList<StateReference>> entry : stateLinkers.entrySet()) {


						stateName = entry.getKey();
						stateReferences = entry.getValue();
						for (StateReference stateReference : stateReferences) {
							line = stateReference.theLine;
							lineNum = stateReference.lineNum;
							
							
							createKey(stateReference, className, path, document, root);
							
							if (!stateReference.isTransition) {
								//STATE
								if (stateName.equals(stateSelected) ) {
									String colorState = forwardStateLink(stateName);
									appendTextualDiagram(stateName, line + "\n");
									appendTextualDiagram(stateName, backwardStateLink(stateName, className, lineNum) + "\n");
									appendTextualDiagram(stateName, colorState + "\n");
									
								} else {
									appendTextualDiagram(stateName, backwardStateLink(stateName, className, lineNum) + "\n");
						    		appendTextualDiagram(stateName, line + "\n");
								}
							}
							
							else  {
								//TRANSITION							
							
								//transitionDone ?
								if (addedTransitions.contains(line)) continue;

								if (selectedLineNum == lineNum) {
									
									String colorTransition = forwardTransitionLink(line);
									appendTextualDiagram(stateName, backwardTransitionLink(colorTransition, className, lineNum, line) + "\n");
									displayTransitionStateTransitionMarkers(stateReference, path, root);

									
								} else {
									appendTextualDiagram(stateName, backwardTransitionLink(line, className, lineNum, line) + "\n");
								}
								addedTransitions.add(line);
							}
		
						}	
						
					}
					
					for (Map.Entry<String, ArrayList<String>> entry : textualDiagram.entrySet()) {


						stateName = entry.getKey();
						ArrayList<String> textualDesc = entry.getValue();
						for (String text : textualDesc) {
							result.append(text);
						}
					}
					markerAttributes.put(IMarker.CHAR_START, start.getOffset());
					return result;
				}
			}

		} catch (final BadLocationException e) {
		} catch (CoreException e) {
		}
		return null;
	}
	

	

	private void createKey(StateReference stateReference, String className, IPath path, IDocument document, IResource root) {
		
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
		 

		String key = className + theLine + String.valueOf(lineNum + 1) + String.valueOf(charStart) + String.valueOf(charEnd);
		if (plantMarkerKey.contains(key)) {
			System.out.println("Key exists");
			return;
		}
		if (possibleChangedMarker(theLine, lineNum, className, path, document, root, charStart, charEnd)) {
			System.out.println("marker changed");
			return;
		}
		//else brand new line so create new marker and key
		System.out.println("key doesnt exist, adding key");
		plantMarkerKey.add(key);
		addMarkerTask(theLine, lineNum, path, charStart, charEnd, root);
		
	}

	private static void addMarkerTask(String theLine, int lineNum, IPath path, int charStart, int charEnd, IResource root) {
		// use Platform.run to batch the marker creation and attribute setting
		Platform.run(new ISafeRunnable() {
			public void run() throws Exception {

				IMarker marker = root.createMarker("FSM.MARKER");
				marker.setAttribute(IMarker.MESSAGE, theLine);
				marker.setAttribute(IMarker.LINE_NUMBER, lineNum + 1);
				marker.setAttribute(IMarker.SOURCE_ID, path.toString());
				marker.setAttribute(IMarker.CHAR_START, charStart);
				marker.setAttribute(IMarker.CHAR_END, charEnd);
			}
		});

	}
	
	private void addHighlightTask(int lineNum, int charStart, int charEnd, IResource root, IPath path) throws CoreException {
		
		IMarker marker = root.createMarker("FSM.State.Highlight");
        marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
        marker.setAttribute(IMarker.SOURCE_ID, path.toString());
        marker.setAttribute(IMarker.CHAR_START,charStart);
        marker.setAttribute(IMarker.CHAR_END,charEnd);
	}
	
	private void addHighlightTransition(int lineNum, int multiLineStart, int multiLineEnd, IResource root, IPath path) throws CoreException {
		String transitionHighlighter = allTransitionHighlights[k];
		IMarker marker = root.createMarker(transitionHighlighter );
        marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
        marker.setAttribute(IMarker.SOURCE_ID, path.toString());
        marker.setAttribute(IMarker.CHAR_START,multiLineStart);
        marker.setAttribute(IMarker.CHAR_END,multiLineEnd);
     
        
	}

	private static boolean possibleChangedMarker(String theLine, int lineNum, String className, IPath iPath, IDocument document, IResource root, int charStart, int charEnd) {
		try {
			IMarker[] allMarkers;
		
			allMarkers = root.findMarkers("FSM.MARKER", false, IResource.DEPTH_ZERO);
			
			
			String path = iPath.toString();
			for (IMarker aMarker : allMarkers) {
				//Marker
				String markerMessage = (String)aMarker.getAttribute(IMarker.MESSAGE);
				
				if (theLine.contains("transitionState") && !theLine.equals(markerMessage)) continue;
				
				int markerLine = (int)aMarker.getAttribute(IMarker.LINE_NUMBER);
				
				String markerPath = (String)aMarker.getAttribute(IMarker.SOURCE_ID);
				String[] tmp = markerPath.split("/");
				String markerClassName = tmp[tmp.length-1];

				int markerCharStart = (int)aMarker.getAttribute(IMarker.CHAR_START);
				int markerCharEnd = (int)aMarker.getAttribute(IMarker.CHAR_END);
				
				String sameLineInDoc = document.get(document.getLineOffset(markerLine-1), document.getLineLength(markerLine-1)).trim();
				
				if (markerLine == (lineNum + 1) && markerPath.equals(path)) {
					if (!markerMessage.equals(theLine) || markerCharStart != charStart || markerCharEnd != charEnd) {
						String oldKey = markerClassName + markerMessage + String.valueOf(markerLine)  + String.valueOf(markerCharStart) + String.valueOf(markerCharEnd);
						String newKey = className + theLine + String.valueOf(lineNum + 1) + String.valueOf(charStart) +  String.valueOf(charEnd);
						//delete old marker
						aMarker.delete();
						//and its key...
						plantMarkerKey.remove(oldKey);
						
						//create new marker
						addMarkerTask(theLine, lineNum, iPath, charStart, charEnd, root);
						// and its new key
						plantMarkerKey.add(newKey);
						return true;
					}
				}
				if (markerPath.equals(path) && markerMessage.equals(theLine)) {
					if (markerLine != lineNum + 1 || markerCharStart != charStart || markerCharEnd != charEnd) {
						String oldKey = markerClassName + markerMessage + String.valueOf(markerLine)  + String.valueOf(markerCharStart) + String.valueOf(markerCharEnd);
						String newKey = className + theLine + String.valueOf(lineNum + 1) + String.valueOf(charStart) +  String.valueOf(charEnd);

						//delete old marker
						aMarker.delete();
						//and its key...
						plantMarkerKey.remove(oldKey);
						
						//create new marker
						addMarkerTask(theLine, lineNum, iPath, charStart, charEnd, root);
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

	//Link from the editor to the diagram
	private String forwardStateLink(String stateName) {
		return "state " + stateName + " #Cyan";
	}

	//Link from the editor to the diagram
	private String forwardTransitionLink(String selectedLine) {
		String colorTransition = "";
		int indexOfArrow = selectedLine.indexOf("-");
		String theArrow = selectedLine.substring(indexOfArrow+1, indexOfArrow +2);
		String transColor = transitionColors[k];
		colorTransition = selectedLine.substring(0, indexOfArrow) +  "-[thickness=5,"+transColor+"]" + theArrow + selectedLine.substring(indexOfArrow +2, selectedLine.length());
		return colorTransition;
	}

	private String backwardStateLink(String stateName, String className, int lineNum) {
		String link = "state " + stateName + "[["+className+"#FSM#state#"+ lineNum +"]]";
		return link;

	}

	private String backwardTransitionLink(String aLine, String className, int lineNum, String originalLine) {
		if (aLine.contains(":")) {
			int index = aLine.indexOf(":");
			String transitionLabel = aLine.substring(index + 1, aLine.length());
			return aLine.substring(0, index -1) + " : " + "[["+className+"#FSM#transition#"+lineNum+ transitionLabel +"]]";
		}
		
		return aLine + " : " + "[["+className+"#FSM#transition#"+lineNum+" "+originalLine+"]]";

	}
	
	private String backwardsTransitionStateLink(StateReference stateReference, String stateName, String className, IPath path, IDocument document, IResource root) {
		createKey(stateReference, className, path, document, root);
		String link = "state " + stateName + "[["+className+"#FSM#"+stateReference.editorLine+"#"+stateReference.lineNum+"]]";
		return link;
	}
	
	private void displayHighlights(String stateName, ArrayList<StateReference> stateReferences, String className, IPath path , IResource root) throws CoreException {

		int charStart, charEnd, charTransStart, charTransEnd;

		for (StateReference stateReference : stateReferences) {
			int lineNum = stateReference.lineNum;
			
			if (stateReference.isTransition) {
				Transition transition = stateReference.transition;
				String theLine = stateReference.theLine;
				
				if (transition.leftState.equals(stateName) && transition.rightState.equals(stateName)) {
					charStart = transition.leftCharStart;
					charEnd = transition.leftCharEnd;
					addHighlightTask(lineNum, charStart, charEnd, root, path);
					charStart = transition.rightCharStart;
					charTransStart = charEnd +1;
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
				addHighlightTransition(lineNum, charTransStart, charTransEnd, root, path);

				if (transition.multiLineTransition) {
					int multiLineStart = transition.multiLineStart;
					int multiLineEnd = transition.multiLineEnd;
					addHighlightTransition(lineNum, multiLineStart, multiLineEnd, root, path);
				} 

		
				String colorTransition = forwardTransitionLink(stateReference.theLine);
				appendTextualDiagram(stateName, backwardTransitionLink(colorTransition, className, lineNum, stateReference.theLine) + "\n");
				addedTransitions.add(theLine);
				k++;
				
			} else {
				charStart = stateReference.charStart;
				charEnd = stateReference.charEnd;
			}
			addHighlightTask(lineNum, charStart, charEnd, root, path);

	        
		}
			
		
	}
	
	protected static IResource getRoot(IEditorInput editorInput) { 
		IPath path = ((IFileEditorInput) editorInput).getFile().getFullPath();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot wsRoot = workspace.getRoot();
		return wsRoot.findMember(path);

	}
	
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

}
