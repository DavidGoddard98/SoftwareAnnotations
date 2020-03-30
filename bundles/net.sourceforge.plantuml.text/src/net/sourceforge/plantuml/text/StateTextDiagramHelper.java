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
import org.eclipse.ui.IFileEditorInput;

public class StateTextDiagramHelper  {
	
	final String prefix = "@start_state_machine", prefixRegex = prefix;
	final String suffix = "@end_state_machine", suffixRegex = suffix;
	int selectedLineNum;
	String stateSelected;
			
	private boolean toggle = true;
	HashMap<String, Integer> diagramText = new HashMap<String, Integer>();
	private static HashSet<String> plantMarkerKey = new HashSet<String>();


	
	public StateTextDiagramHelper() {
	
	}
	
	
	
	
	class StateReference extends StateTextDiagramHelper {
		String theLine;
		int lineNum;
		int charStart;
		int charEnd;
		boolean isTransition;
		Transition transition;
		
		StateReference(String theLine, int lineNum, int charStart, int charEnd) {
			this.theLine = theLine;
			this.lineNum = lineNum;
			this.charStart = charStart;
			this.charEnd = charEnd;
			this.isTransition = false;
		}
		
		StateReference(String theLine, int lineNum, Transition transition) {
			this.theLine = theLine;
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
	private String stateDescriptor(String line) {
	    String theLine = line.replaceAll("\\s+", "").toLowerCase();
	    if (theLine.length() <= 6) 
	    	return null;
	    if (theLine.substring(0,6).equals("//fsm:")) {
	      int index = line.indexOf(":") + 1;
	      return line.substring(index, line.length()).trim();
	    }
	    return null;
	  }
	
	
	private void appendToLists(String line, int lineNum, List<Transition> transitions,
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
				int rightCharStart = markerRegion.getOffset() + index;
				int rightCharEnd = markerRegion.getOffset() + anotherIndex;
				
				transition = new Transition(leftState, rightState, leftCharStart, leftCharEnd, rightCharStart, rightCharEnd);
				stateReference = new StateReference(line, lineNum, transition);
				System.out.println(stateReference.toString());
				
				
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
	
			transitions.add(transition);
			
			
			
			
			
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
				
				stateReference = new StateReference(line, lineNum, charStart, charEnd);
				
			} else if (line.contains(":")) {
				index = line.indexOf(":") - 1;
				stateName = line.substring(0, index).trim();
				charStart = markerRegion.getOffset();
				charEnd = markerRegion.getOffset() + markerRegion.getLength();
				
				stateReference = new StateReference(line, lineNum, charStart, charEnd);
				
				
				
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

		}
			
	}
	
	public static void print(HashMap<String, ArrayList<StateReference>>  map) 
    { 
        if (map.isEmpty()) { 
            System.out.println("map is empty"); 
        } 
  
        else { 
            System.out.println(map); 
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
		
		
		if (toggle) {
			initializeKeys(root, path, document);
			toggle = false;
		}
		
		
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
	
					List<Transition> transitions = new ArrayList<Transition>();
					HashMap<String, ArrayList<StateReference>> stateLinkers = new HashMap<String, ArrayList<StateReference>> (); 
					
					selectedLineNum = document.getLineOfOffset(selectionStart);

					
					diagramText.clear();
					stateSelected = null;
					
					for (int lineNum = startLine + (includeStart ? 0 : 1); lineNum < maxLine; lineNum++) {
						final String line = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum)).trim();
						String newLine = stateDescriptor(line);
						if (newLine != null){ 
							appendToLists(newLine, lineNum, transitions, stateLinkers, finder);
							diagramText.put(newLine, lineNum);
						}
						
					}
					
					
					print(stateLinkers);
					
					
					String className = path.toFile().getName();
					className = className.substring(0, className.length()- 5);
					
					
					
					
					removeHighlights(root);
				
					
					String stateName; String line;
					int lineNum; 
					ArrayList<StateReference> stateReferences;
					ArrayList<String> addedTransitions = new ArrayList<String>();
				
					for (Map.Entry<String, ArrayList<StateReference>> entry : stateLinkers.entrySet()) {
						stateName = entry.getKey();
						stateReferences = entry.getValue();
						
						for (StateReference stateReference : stateReferences) {
							line = stateReference.theLine;
							lineNum = stateReference.lineNum;

							IRegion markerRegion = finder.find(0, line, true, true, false, false);
							
							createKey(line, lineNum, path, markerRegion, document, root);
							
							if (stateReference.isTransition) {
								//TRANSITION
								
								//transitionDone ?
								if (addedTransitions.contains(line)) continue;
								if (selectedLineNum == lineNum) {
									String colorTransition = forwardTransitionLink(line);
									result.append(backwardTransitionLink(colorTransition, className, lineNum));
		//							for (int i =0 ; i<transitionStateNames.size(); i++) {
		//								if (transitionStates.contains(transitionStateNames.get(i)))
		//									displayMarkers(transitionStateNames.get(i), className, root);
		//							}
								} else
									result.append(backwardTransitionLink(line, className, lineNum));
								addedTransitions.add(line);
							}
							
							else {
								//STATE
								if (selectedLineNum == lineNum || stateName.equals(stateSelected) ) {
									String colorState = forwardStateLink(stateName);
						    		
									result.append(line);
									result.append("\n");
									result.append(backwardStateLink(stateName, className, lineNum));
						    		result.append("\n");
									result.append(colorState);
									
									displayMarkers(stateName, diagramText, className, root);
		
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
						
						
						
					}
					markerAttributes.put(IMarker.CHAR_START, start.getOffset());
					return result;
				}
			}

		} catch (final BadLocationException e) {
		}
		return null;
	}
	
	private static boolean validateLine(String theLine) {
		if (theLine.isEmpty()) {
			return false;
		} else if (theLine.contains("@enduml")) {
			return false;
		} else if (theLine.contains("@startuml")) {
			return false;
		} else
			return true;
	}


	private static IMarker[] allMarkers;
	
	//initialize all fsm markers made in previous sessions
	protected static void initializeKeys(IResource resource, IPath path, IDocument document) {
		try {
			allMarkers = resource.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO);

			for (IMarker aMarker : allMarkers) {

				String message = aMarker.getAttribute(IMarker.MESSAGE, "");
				String subString = message.substring(0, 3);

				if (subString.equals("FSM")) {
					String theLine = message.substring(5, message.length());
					int lineNum = (int)aMarker.getAttribute(IMarker.LINE_NUMBER);
					int markerCharStart = (int)aMarker.getAttribute(IMarker.CHAR_START);
					int markerCharEnd = (int)aMarker.getAttribute(IMarker.CHAR_END);
					String className = path.toFile().getName();
					String key = className + theLine + String.valueOf(lineNum) + String.valueOf(markerCharStart) + String.valueOf(markerCharEnd);;
					plantMarkerKey.add(key);
				}
			}
		} catch (CoreException e) {
			System.out.println("Error initializing keys");
		}
	}

	private static boolean possibleChangedMarker(String theLine, int lineNum, IPath iPath, IDocument document, IRegion region, IResource root, int charStart, int charEnd) {
		try {
			allMarkers = root.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO);
			
			
			String className = iPath.toFile().getName(); 
			//remove .java
			
			
			String path = iPath.toString();
			for (IMarker aMarker : allMarkers) {
				//Marker
				String markerMessage = (String)aMarker.getAttribute(IMarker.MESSAGE);
				//remove 'fsm '
				markerMessage = markerMessage.substring(5, markerMessage.length());
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
						addTask(theLine, lineNum, iPath,region);
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
						addTask(theLine, lineNum, iPath,region);
						// and its new key
						plantMarkerKey.add(newKey);
						return true;
					}
				}
				
				//line deleted - delete marker and key
				if (!sameLineInDoc.equals(markerMessage)) {
					System.out.println("Deleting empty line");
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
		System.out.println("Nothing to report here");
		return false;

	}

	// TODO: onStartup initialize keylist with FSM bookmarks
	// Delete keys on marker deletion?
	private static void createKey(String theLine, int lineNum, IPath path, IRegion region, IDocument document, IResource root) {
		try {
			if (!validateLine(theLine))
				return;
			String className = path.toFile().getName();
			final FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
			IRegion markerRegion = finder.find(0, theLine, true, true, false, false);
			int charStart = markerRegion.getOffset();
			int charEnd = markerRegion.getOffset() + markerRegion.getLength();
			
			
			
			String key = className + theLine + String.valueOf(lineNum + 1) + String.valueOf(charStart) + String.valueOf(charEnd);
			if (plantMarkerKey.contains(key)) {
				return;
			}
			if (possibleChangedMarker(theLine, lineNum, path, document, region, root, charStart, charEnd)) {
				return;
			}
			//else brand new line so create new marker and key
			plantMarkerKey.add(key);
			addTask(theLine, lineNum, path, region);
		}catch (BadLocationException e) {
			System.out.println("error creating key");
		}
	}

	private static void addTask(String theLine, int lineNum, IPath path, IRegion region) {
		// use Platform.run to batch the marker creation and attribute setting

		Platform.run(new ISafeRunnable() {
			public void run() throws Exception {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IWorkspaceRoot wsRoot = workspace.getRoot();
				IResource root = wsRoot.findMember(path);

				IMarker marker = root.createMarker(IMarker.BOOKMARK);
				marker.setAttribute(IMarker.MESSAGE, "FSM: " + theLine);
				marker.setAttribute(IMarker.LINE_NUMBER, lineNum + 1);
				marker.setAttribute(IMarker.SOURCE_ID, path.toString());
				marker.setAttribute(IMarker.CHAR_START, region.getOffset());
				marker.setAttribute(IMarker.CHAR_END, region.getOffset() + region.getLength());
			}
		});

	}


	//Checks that the current diagram description is of a state machine
	protected static boolean ensureFSM(String line) {
		String stateValid = "[*]";
		return (line.toLowerCase().contains(stateValid.toLowerCase()));
	}


	//Link from the editor to the diagram
	private static String forwardStateLink(String stateName) {
		return "state " + stateName + " #Cyan";
	}

	//Link from the editor to the diagram
	private static String forwardTransitionLink(String selectedLine) {
		String colorTransition = "";
		int indexOfArrow = selectedLine.indexOf("->");
		colorTransition = selectedLine.substring(0, indexOfArrow) +  "-[thickness=5,#blue]>" + selectedLine.substring(indexOfArrow +2, selectedLine.length());
		return colorTransition;
	}
	
	private static StringBuilder getStateLines(HashMap<String, Integer> diagramText, String stateName) {
		StringBuilder stateLines = new StringBuilder();
		
		for (String theLine : diagramText.keySet()) {
//	    	if (theLine.contains("->"))
//	    		continue;
	    	if (isContain(theLine, stateName)) { 
	    		stateLines.append(diagramText.get(theLine) + ",");
	    	}
	    }
		return stateLines;
	}

	private static String backwardStateLink(String stateName, String className, int lineNum) {
		String test = "state " + stateName + "[["+className+"#FSM#state#"+ lineNum +"]]";
		return test;

	}

	private static String backwardTransitionLink(String aLine, String className, int lineNum) {
		return aLine + " : " + "[["+className+"#FSM#transition#"+lineNum+"]]";

	}
	
	//from --https://stackoverflow.com/questions/25417363/java-string-contains-matches-exact-word/25418057
	 private static boolean isContain(String source, String subItem){
         String pattern = "\\b"+subItem+"\\b";
         Pattern p=Pattern.compile(pattern);
         Matcher m=p.matcher(source);
         return m.find();
    }

	
	private static void displayMarkers(String stateName, HashMap<String, Integer> diagramText, String fileName, IResource root)  {
		//
	
		
		String stateLines = getStateLines(diagramText, stateName).toString();
   	    String[] lineNums = stateLines.split(",");
   	    int[] intLineNums = Arrays.asList(lineNums).stream().mapToInt(Integer::parseInt).toArray();
   	    try { 
   	    	
	   	   
			IMarker[] markers = root.findMarkers(IMarker.BOOKMARK, true, IResource.DEPTH_INFINITE);
		      for (IMarker m : markers) {
		    	  String path = (String)(m.getAttribute(IMarker.SOURCE_ID));
		    	  String[] tmp = path.split("/");
		    	  String file = tmp[tmp.length-1];
		    	  file = file.substring(0, file.length() - 5);
		    	  
		    	 
		    	  
		    	  if (file.equals(fileName)) {
			    	  int markerLine = (int)m.getAttribute(IMarker.LINE_NUMBER);
			    	  for (int i = 0; i<intLineNums.length; i++) {
			    		  
		
		        		  if (markerLine == intLineNums[i]) {
		        			
		        			  try {
		        				  String message = (String)m.getAttribute(IMarker.MESSAGE);
		        				  message = message.substring(5, message.length());
		        				  int charStart =  (int)m.getAttribute(IMarker.CHAR_START); 
				    		      int charEnd =  (int)m.getAttribute(IMarker.CHAR_END);
		        				  if (message.contains("->")) {
		        					  String[] transitionSplit = message.split("->", 2);
		        					  if (transitionSplit[0].contains(stateName)) {
		        						  charEnd = charStart + stateName.length();
		        					  }
		        					  if (transitionSplit[1].contains(stateName)) {
		        						  charStart = charStart + message.indexOf(stateName) ;
		        						  charEnd = charStart + stateName.length();
		        					  }
		        					
		        				  }
		        				
		        				 
				    		      IMarker marker = root.createMarker("FSM.MARKER");
				    		      marker.setAttribute(IMarker.LINE_NUMBER, markerLine);
				    		      marker.setAttribute(IMarker.SOURCE_ID, path);
				    			  marker.setAttribute(IMarker.CHAR_START,charStart);
				    		      marker.setAttribute(IMarker.CHAR_END,charEnd);
		        			  } catch (CoreException e) {
		        				  
		        		
		        			 	  System.out.println("null");
		        			  } 
		        		  }
			    	  }
		    	  }
		      }
   	    } catch (CoreException e) {
   	    	System.out.println("couldnt display markers");
   	    }

	}
	
	protected static String getStateName(String line) {
		String stateName;
	    String[] state = line.split(" ", 2);
	    stateName = state[0].replace(":", "");
	    return stateName;
	}
	
	protected static void removeHighlights(IResource resource) {
		try {
			IMarker[] markers = resource.findMarkers("FSM.MARKER", true, IResource.DEPTH_INFINITE);
			for (IMarker m : markers) {
				m.delete();
			}
		} catch (CoreException e) {
			System.out.println("Couldnt remove highlights");
		}
		
	}
	
	private static List<String> getTransitionStateName(String line) {
		String stateName;
		List<String> stateNames = new ArrayList<String>();
		String[] splitLine = line.split("State");
	    String[] tmp;
	    for  (int i = 1; i<splitLine.length; i++) {
	    	splitLine[i] = splitLine[i].replace(":", "");
	    	tmp = splitLine[i].split(" ", 2);
	    	stateName = "State" + tmp[0];
	    	stateNames.add(stateName);
	    }
	    return stateNames;

	}
	
	//create List of states..
	private static void addToList(String line, int lineNum, String className, StringBuilder result, HashSet<String> doneStates) {
		
		String stateName;
	    String[] states = line.split("State");
	    String[] tmp;
	    
	    for  (int i = 1; i<states.length; i++) {
	    	states[i] = states[i].replace(":", "");
	    	tmp = states[i].split(" ", 2);
	    	stateName = "State" + tmp[0];
	    	
	    	if (!doneStates.contains(stateName)) {
	    		
	    		doneStates.add(stateName);
	    	}
	    }
	}

}
