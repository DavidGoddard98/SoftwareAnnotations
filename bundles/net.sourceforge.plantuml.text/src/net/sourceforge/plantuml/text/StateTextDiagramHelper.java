package net.sourceforge.plantuml.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

public class StateTextDiagramHelper  {
	
	final String prefix = "@start_state_machine", prefixRegex = prefix;
	final String suffix = "@end_state_machine", suffixRegex = suffix;
	int selectedLineNum;
	String stateSelected;
	
	private boolean toggle = true;
	private static HashSet<String> plantMarkerKey = new HashSet<String>();


	
	public StateTextDiagramHelper() {
	
	}
	
	class StateReference extends StateTextDiagramHelper {
		String theLine;
		String editorLine;
		int lineNum;
		int charStart;
		int charEnd;
		boolean isTransition;
		boolean onlyTransition;
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
		
		
		StateReference(String theLine, String editorLine, int lineNum, int charStart, int charEnd, boolean onlyTransition) {
			this.theLine = theLine;
			this.editorLine = editorLine;
			this.lineNum = lineNum;
			this.charStart = charStart;
			this.charEnd = charEnd;
			this.isTransition = false;
			this.onlyTransition = onlyTransition;
		}
		
		StateReference() {
			
		}
		
		public String toString() {
			return this.theLine;
		}
	}
	
	class Transition extends StateTextDiagramHelper {
		String leftState;
		int leftCharStart;
		int leftCharEnd;
	
		String rightState;
		int rightCharStart;
		int rightCharEnd;
		
		Transition(String leftState, String rightState, int leftCharStart, int leftCharEnd, int rightCharStart, int rightCharEnd) {
			this.leftState = leftState;
			this.rightState = rightState;
			this.leftCharStart = leftCharStart;
			this.leftCharEnd = leftCharEnd;
			this.rightCharStart = rightCharStart;
			this.rightCharEnd = rightCharEnd;
		}
		
		Transition() {
			
		}
		
		public String toString() {
			return this.leftState + " -> " + this.rightState;
		}
		
		
	}
	
	//Check whether line is a descriptor or not
	private static String stateDescriptor(String line) {
	    String theLine = line.replaceAll("\\s+", "").toLowerCase();
	    if (theLine.length() <= 6) 
	    	return null;
	    if (theLine.substring(0,6).equals("//fsm:")) {
	      int index = line.indexOf(":") + 1;
	      return line.substring(index, line.length()).trim();
	    }
	    return null;
	  }
	
	
	private void appendToLists(String line, String editorLine, int lineNum, ArrayList<String> actualStates,
			HashMap<String, ArrayList<StateReference>> stateLinkers, FindReplaceDocumentAdapter finder) throws BadLocationException {
		////////////////////
		IRegion markerRegion = finder.find(0, line, true, true, false, false);
		///////////////////
		int charStart;
		int charEnd;
		int index;
		ArrayList<StateReference> stateReferences = new ArrayList<StateReference>();

		//TRANSITIONS
		if (line.contains("->") || line.contains("<-")) {
			String leftState = "" ;
			String rightState = "";
			Transition transition = new Transition();
			StateReference stateReference = new StateReference();
			
			if (line.contains("->") ) {
				
				index = line.indexOf("-") - 1;
				leftState = line.substring(0, index).trim();
				
				int leftCharStart = markerRegion.getOffset();
				int leftCharEnd = markerRegion.getOffset() + leftState.length();
				
				index = line.indexOf(">") + 1;
				int anotherIndex = line.length();
				if (line.contains(":"))
					anotherIndex = line.indexOf(":") - 1;
				rightState = line.substring(index, anotherIndex).trim();
				int rightCharStart = markerRegion.getOffset() + index + 1;
				int rightCharEnd = markerRegion.getOffset() + anotherIndex;
				
				transition = new Transition(leftState, rightState, leftCharStart, leftCharEnd, rightCharStart, rightCharEnd);
				stateReference = new StateReference(line, editorLine, lineNum, transition);
				
				
			} else if (line.contains("<-")) {
				//
			}
			
			//ADD THE TRANSITION'S STATE REFERENCES

			if (stateLinkers.containsKey(leftState)) {
				stateReferences = stateLinkers.get(leftState);
				stateReferences.add(stateReference);
				stateLinkers.put(leftState, stateReferences);
				
			} else {

				stateReferences.add(stateReference);
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
				charEnd = markerRegion.getOffset() + markerRegion.getLength();
				
				stateReference = new StateReference(line, editorLine, lineNum, charStart, charEnd);
				
			} else if (line.contains(":")) {
				index = line.indexOf(":") - 1;
				stateName = line.substring(0, index).trim();
				charStart = markerRegion.getOffset();
				charEnd = markerRegion.getOffset() + markerRegion.getLength();
				
				stateReference = new StateReference(line, editorLine, lineNum, charStart, charEnd);
				
				
				
			}

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
	
	
	public StringBuilder getDiagramTextLines(final IDocument document, final int selectionStart,
			final Map<String, Object> markerAttributes, IEditorInput editorInput) {
		final boolean includeStart = prefix.startsWith("@"), includeEnd = suffix.startsWith("@");
		final FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
		IPath path = ((IFileEditorInput) editorInput).getFile().getFullPath();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot wsRoot = workspace.getRoot();
		IResource root = wsRoot.findMember(path);
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
//					System.out.println();
//					System.out.println();
//					int i = 0;
//					for (String key : plantMarkerKey) {
//						System.out.println(i + key);
//						i ++;
//					}
//					System.out.println("key size: " + plantMarkerKey.size());
					IMarker[] allMarkers = root.findMarkers("FSM.MARKER", false, IResource.DEPTH_ZERO);
					int i = 0;
					for (IMarker marker : allMarkers) {
						System.out.println(marker.getAttribute(IMarker.MESSAGE));

						i ++;
					}
					System.out.println("markerSize: " + allMarkers.length);
					selectedLineNum = document.getLineOfOffset(selectionStart);

					
					stateSelected = null;
					HashMap<String, ArrayList<StateReference>> stateLinkers = new HashMap<String, ArrayList<StateReference>> (); 
				    ArrayList<String> actualStates = new ArrayList<String>();

					for (int lineNum = startLine + (includeStart ? 0 : 1); lineNum < maxLine; lineNum++) {
						final String line = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum)).trim();
						String newLine = stateDescriptor(line);
						if (newLine != null){ 
							appendToLists(newLine, line, lineNum, actualStates, stateLinkers, finder);
						}
						
					}
					
					
					String className = path.toFile().getName();
					
					removeHighlights(root);
				
					String stateName; 
					String line;
					int lineNum; 
					ArrayList<StateReference> stateReferences;
					ArrayList<String> addedTransitions = new ArrayList<String>();
					boolean displayedMarkers = false;

					for (Map.Entry<String, ArrayList<StateReference>> entry : stateLinkers.entrySet()) {
						ArrayList<StateReference> addedTransitionRef = new ArrayList<StateReference>();


						stateName = entry.getKey();
						stateReferences = entry.getValue();
						boolean onlyTransitions = true;
						for (StateReference stateReference : stateReferences) {
							line = stateReference.theLine;
							lineNum = stateReference.lineNum;
							
							
							createKey(stateReference, className, path, document, root);
							
							if (stateReference.isTransition) {
								//TRANSITION
								
								//transitionDone ?
								addedTransitionRef.add(stateReference);
								if (addedTransitions.contains(line)) continue;
								if (selectedLineNum == lineNum) {
									String colorTransition = forwardTransitionLink(line);
									result.append(backwardTransitionLink(colorTransition, className, lineNum));
								} else
									result.append(backwardTransitionLink(line, className, lineNum));
								addedTransitions.add(line);
							}
							
							else {
								//STATE
								onlyTransitions = false;
								if (stateName.equals(stateSelected) ) {
									String colorState = forwardStateLink(stateName);
						    		
									result.append(line);
									result.append("\n");
									result.append(backwardStateLink(stateName, className, lineNum));
						    		result.append("\n");
									result.append(colorState);
									if (!displayedMarkers) {
										displayMarkers(stateName, stateLinkers, path, root);
										displayedMarkers = true;
									}
		
								} else {
									result.append(backwardStateLink(stateName, className, lineNum));
						    		result.append("\n");
									result.append(line);
								}
							}
							if (!line.endsWith("\n")) {
								result.append("\n");
							}
							
						}
					
						
						if (onlyTransitions) {
							for (StateReference addedTransition : addedTransitionRef) {
								String leftStateName = addedTransition.transition.leftState;
								String rightStateName = addedTransition.transition.rightState;
								String theLine = addedTransition.editorLine;
								int charStart, charEnd;
							    lineNum = addedTransition.lineNum;
								if (!actualStates.contains(leftStateName)) {
									charStart = addedTransition.transition.leftCharStart;
									charEnd = addedTransition.transition.leftCharEnd;
									StateReference leftStateRef = new StateReference(theLine, "transitionState" + leftStateName, lineNum, charStart, charEnd, true);
									result.append(backwardsTransitionStateLink(leftStateRef, leftStateName, className, path, document, root));
									result.append("\n");
								}		

								if (!actualStates.contains(rightStateName)) {

									charStart = addedTransition.transition.rightCharStart;
									charEnd = addedTransition.transition.rightCharEnd;
									StateReference rightStateRef = new StateReference(theLine, "transitionState" + rightStateName, lineNum, charStart, charEnd, true);
									result.append(backwardsTransitionStateLink(rightStateRef, rightStateName, className, path, document, root));
									result.append("\n");

								}
			
							}
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
			charStart = stateReference.transition.leftCharStart;
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
		addTask(theLine, lineNum, path, charStart, charEnd);
		
	}

	private static void addTask(String theLine, int lineNum, IPath path, int charStart, int charEnd) {
		// use Platform.run to batch the marker creation and attribute setting
		Platform.run(new ISafeRunnable() {
			public void run() throws Exception {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IWorkspaceRoot wsRoot = workspace.getRoot();
				IResource root = wsRoot.findMember(path);

				IMarker marker = root.createMarker("FSM.MARKER");
				marker.setAttribute(IMarker.MESSAGE, theLine);
				marker.setAttribute(IMarker.LINE_NUMBER, lineNum + 1);
				marker.setAttribute(IMarker.SOURCE_ID, path.toString());
				marker.setAttribute(IMarker.CHAR_START, charStart);
				marker.setAttribute(IMarker.CHAR_END, charEnd);
			}
		});

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
						addTask(theLine, lineNum, iPath, charStart, charEnd);
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
						addTask(theLine, lineNum, iPath, charStart, charEnd);
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
		int indexOfArrow = selectedLine.indexOf("->");
		colorTransition = selectedLine.substring(0, indexOfArrow) +  "-[thickness=5,#blue]>" + selectedLine.substring(indexOfArrow +2, selectedLine.length());
		return colorTransition;
	}

	private String backwardStateLink(String stateName, String className, int lineNum) {
		String link = "state " + stateName + "[["+className+"#FSM#state#"+ lineNum +"]]";
		return link;

	}

	private String backwardTransitionLink(String aLine, String className, int lineNum) {
		return aLine + " : " + "[["+className+"#FSM#transition#"+lineNum+"]]";

	}
	
	private String backwardsTransitionStateLink(StateReference stateReference, String stateName, String className, IPath path, IDocument document, IResource root) {
		createKey(stateReference, className, path, document, root);
		String link = "state " + stateName + "[["+className+"#FSM#"+stateReference.editorLine+"#"+stateReference.lineNum+"]]";
		return link;
	}
	
	private void displayMarkers(String stateName, HashMap<String, ArrayList<StateReference>> stateLinkers, IPath path , IResource root) throws CoreException {
		//
		ArrayList<StateReference> stateReferences = new ArrayList<StateReference>();

		int lineNum = 0;
		int charStart = 0;
		int charEnd = 0;
		for (Map.Entry<String, ArrayList<StateReference>> entry : stateLinkers.entrySet()) {
			String referenceStateName = entry.getKey();
			if (referenceStateName.equals(stateName)) {
				stateReferences = entry.getValue();
				
				for (StateReference stateReference : stateReferences) {
					lineNum = stateReference.lineNum;
					
					if (stateReference.isTransition) {
						if (stateReference.transition.leftState.contentEquals(stateName)) {
							charStart = stateReference.transition.leftCharStart;
							charEnd = stateReference.transition.leftCharEnd;
						} else {
							charStart = stateReference.transition.rightCharStart;
							charEnd = stateReference.transition.rightCharEnd;
						}
					} else {
						charStart = stateReference.charStart;
						charEnd = stateReference.charEnd;
					}
	
			        IMarker marker = root.createMarker("FSM.Highlight");
			        marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
			        marker.setAttribute(IMarker.SOURCE_ID, path.toString());
			        marker.setAttribute(IMarker.CHAR_START,charStart);
			        marker.setAttribute(IMarker.CHAR_END,charEnd);
				}
			}
		}
	}
	//@start_state_machine
	//FSM: State.l1 : a
	//FSM: State1 -> State2
	
	//FSM: State3 -> state2
	
	//@end_state_machine
	
	/**
	 * @startuml
	 * State1 -> State2
	 * @enduml
	 
	 */
	 
	
	protected static void removeHighlights(IResource resource) {
		try {
			IMarker[] markers = resource.findMarkers("FSM.Highlight", true, IResource.DEPTH_INFINITE);
			for (IMarker m : markers) {
				m.delete();
			}
		} catch (CoreException e) {
			System.out.println("Couldnt remove highlights");
		}
		
	}

}
