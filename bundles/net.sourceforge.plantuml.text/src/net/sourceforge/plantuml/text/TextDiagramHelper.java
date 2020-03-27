package net.sourceforge.plantuml.text;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import net.sourceforge.plantuml.eclipse.utils.PlantumlConstants;
import net.sourceforge.plantuml.eclipse.utils.PlantumlUtil;

public class TextDiagramHelper {

	private final String prefix, prefixRegex;
	private final String suffix, suffixRegex;
	HashSet<String> plantMarkerKey = new HashSet<String>();
	HashMap<String, Integer> diagramText = new HashMap<String, Integer>();
	private boolean toggle = true;

	public TextDiagramHelper(final String prefix, final String prefixRegex, final String suffix,
			final String suffixRegex) {
		super();
		this.prefix = prefix;
		this.prefixRegex = prefixRegex;
		this.suffix = suffix;
		this.suffixRegex = suffixRegex;
	}

	private boolean validateLine(String theLine) {
		if (theLine.isEmpty()) {
			return false;
		} else if (theLine.contains("@enduml")) {
			return false;
		} else if (theLine.contains("@startuml")) {
			return false;
		} else
			return true;
	}


	IMarker[] allMarkers;
	
	//initialize all fsm markers made in previous sessions
	private void initializeKeys(IResource resource, IPath path, IDocument document) {
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

	private boolean possibleChangedMarker(String theLine, int lineNum, IPath iPath, IRegion region, IResource root, int charStart, int charEnd) {
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
				
			}
				
		} catch (CoreException e) {
			System.out.println("Failed to initialise keys");
		} 
		System.out.println("Nothing to report here");
		return false;

	}

	// TODO: onStartup initialize keylist with FSM bookmarks
	// Delete keys on marker deletion?
	private void createKey(String theLine, int lineNum, IPath path, IRegion region, IDocument document, IResource root) {
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
			if (possibleChangedMarker(theLine, lineNum, path, region, root, charStart, charEnd)) {
				return;
			}
			//else brand new line so create new marker and key
			plantMarkerKey.add(key);
			addTask(theLine, lineNum, path, region);
		}catch (BadLocationException e) {
			System.out.println("error creating key");
		}
	}

	private void addTask(String theLine, int lineNum, IPath path, IRegion region) {
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
	private boolean ensureFSM(String line) {
		String stateValid = "[*]";
		return (line.toLowerCase().contains(stateValid.toLowerCase()));
	}


	//Link from the editor to the diagram
	private String forwardStateLink(String selectedLine) {
		String colorState = "";

		for (int i = 0; i < selectedLine.length(); i++) {
			if (selectedLine.charAt(i) == ' ' && selectedLine.charAt(i + 1) == ':') {
				colorState = "state " + selectedLine.substring(0, i) + " #Cyan";
			}
		}
		return colorState;
	}

	//Link from the editor to the diagram
	private String forwardTransitionLink(String selectedLine) {
		String colorTransition = "";
		int indexOfTrans = 0;
		char tmp = 'c';

		for (int i = 0; i < selectedLine.length(); i++) {
			char c = selectedLine.charAt(i);
			if (c == '>' && tmp == '-') {
				indexOfTrans = selectedLine.indexOf('-');
				colorTransition = selectedLine.substring(0, indexOfTrans) + "-[thickness=5,#blue]"
						+ selectedLine.substring(indexOfTrans + 1, selectedLine.length()) ;
				break;
			}
			tmp = c;
		}
		return colorTransition;
	}
	
	private StringBuilder getStateLines(HashMap<String, Integer> diagramText, String stateName) {
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
		System.out.println(test);
		return test;

	}

	private String backwardTransitionLink(String aLine, String className, int lineNum) {
		return aLine + " : " + "[["+className+"#FSM#transition#"+lineNum+"]]";

	}
	
	//from --https://stackoverflow.com/questions/25417363/java-string-contains-matches-exact-word/25418057
	 private static boolean isContain(String source, String subItem){
         String pattern = "\\b"+subItem+"\\b";
         Pattern p=Pattern.compile(pattern);
         Matcher m=p.matcher(source);
         return m.find();
    }

	
	private void displayMarkers(String stateName, String fileName, IResource root)  {
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
	
	private String getStateName(String line) {
		String stateName;
	    String[] state = line.split(" ", 2);
	    stateName = state[0].replace(":", "");
	    return stateName;
	}
	
	private void removeHighlights(IResource resource) {
		try {
			IMarker[] markers = resource.findMarkers("FSM.MARKER", true, IResource.DEPTH_INFINITE);
			for (IMarker m : markers) {
				m.delete();
			}
		} catch (CoreException e) {
			System.out.println("Couldnt remove highlights");
		}
		
	}
	
	private List<String> getTransitionStateName(String line) {
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
		
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("key set size: " + plantMarkerKey.size());
		
	
		
		try {

			// search backward and forward start and end
			IRegion start = finder.find(selectionStart, prefixRegex, false, true, false, true);
			// if not start or end is before start, we must search backward
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
				if (end != null && end.getOffset() >= selectionStart) {

					int selectedLineNum = document.getLineOfOffset(selectionStart);
					String selectedLine = document
							.get(document.getLineOffset(selectedLineNum), document.getLineLength(selectedLineNum))
							.trim();
					
					System.out.println("Selected line = " + selectedLine);
					
				
					HashSet<String> doneStates = new HashSet<String>();

				

					

					final int endOffset = end.getOffset() + end.getLength();
					StringBuilder result = new StringBuilder();
					final int maxLine = Math.min(document.getLineOfOffset(endOffset) + (includeEnd ? 1 : 0),
							document.getNumberOfLines());

					
					String className = path.toFile().getName();
					className = className.substring(0, className.length()- 5);
					
					diagramText.clear();
					boolean fsm = false;
					for (int lineNum = startLine + (includeStart ? 0 : 1); lineNum < maxLine; lineNum++) {
						final String line = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum)).trim();
						diagramText.put(line, lineNum + 1);
						
						if(!line.contains("->") && line.contains("State")) {
							String stateName = getStateName(line);

							if (!doneStates.contains(stateName)) {
					    		doneStates.add(stateName);
	
					    	}
						}
						if (ensureFSM(line)) {
								fsm = true;
						}
					}

					
					
					removeHighlights(root);
					List<String> transitionStates = new ArrayList<String>();
					List<String> transitionStateNames = new ArrayList<String>();
					String lastStateName = "";

//					System.out.println("Package: "+className.getPackage());
					for (int lineNum = startLine + (includeStart ? 0 : 1); lineNum < maxLine; lineNum++) {
						final String line = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum)).trim();
						IRegion markerRegion = finder.find(0, line, true, true, false, false);

						createKey(line, lineNum, path, markerRegion, document, root);

							
						//add transitions and their links
						if (line.contains("->") && fsm)  {
 							if (line.contains("State")) {
 								transitionStateNames = getTransitionStateName(line);
 								for (int i =0 ; i<transitionStateNames.size(); i++) {
 									if (!doneStates.contains(transitionStateNames.get(i))) {
 										System.out.println(transitionStateNames.get(i));
 										transitionStates.add(transitionStateNames.get(i));
 										System.out.println("appending"
 												+ "");
 										System.out.println(lineNum);
 										System.out.println(backwardStateLink(transitionStateNames.get(i), className, lineNum));
 										result.append(backwardStateLink(transitionStateNames.get(i), className, lineNum)); 
 									}
 								}
								
								
							}
							if (selectedLineNum == lineNum) {
								String colorTransition = forwardTransitionLink(selectedLine);
								result.append(backwardTransitionLink(colorTransition, className, lineNum));
//								for (int i =0 ; i<transitionStateNames.size(); i++) {
//									if (transitionStates.contains(transitionStateNames.get(i)))
//										displayMarkers(transitionStateNames.get(i), className, root);
//								}
							}
							else
								result.append(backwardTransitionLink(line, className, lineNum));
						}
						//add states and their links
						else if (line.contains("State") && fsm) {
							String stateName = getStateName(line);

						

							if (selectedLineNum == lineNum || stateName.equals(lastStateName) ) {
								String colorState = forwardStateLink(selectedLine);
					    		
								result.append(line);
								result.append("\n");
								result.append(backwardStateLink(stateName, className, lineNum));
					    		result.append("\n");
							
								System.out.println(stateName);
								result.append(colorState);
								displayMarkers(stateName, className, root);
								System.out.println(result);
								lastStateName = stateName;

							} else {
								result.append(backwardStateLink(stateName, className, lineNum));
					    		result.append("\n");
								result.append(line);
								
					    		

							}
						} else
							result.append(line);
						
						if (!line.endsWith("\n")) {
							result.append("\n");
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

	public String getDiagramText(final CharSequence lines) {
		return getDiagramText(new StringBuilder(lines.toString()));
	}

	public String getDiagramText(final StringBuilder lines) {

		final int prefixPos = lines.indexOf(prefix);
		int start = Math.max(prefixPos, 0);
		final int suffixPos = lines.lastIndexOf(suffix);
		final int end = (suffixPos < 0 ? lines.length() : Math.min(suffixPos + suffix.length(), lines.length()));
		final String linePrefix = lines.substring(0, start).trim();
		final StringBuilder result = new StringBuilder(lines.length());
		if (prefixPos < 0) {
			result.append(PlantumlConstants.START_UML + "\n");
		}
		while (start < end) {
			int lineEnd = lines.indexOf("\n", start);
			if (lineEnd > end) {
				break;
			} else if (lineEnd < 0) {
				lineEnd = lines.length();
			}
			String line = lines.substring(start, lineEnd).trim();
			if (line.startsWith(linePrefix)) {
				line = line.substring(linePrefix.length()).trim();
			}
			result.append(line);
			result.append("\n");
			start = lineEnd + 1;
		}
		if (suffixPos < 0) {
			result.append(PlantumlConstants.END_UML + "\n");
		}
		return result.toString().trim();
	}

	public String getDiagramText(final IFile file) {

		final IMarker marker = PlantumlUtil.getPlantUmlMarker(file, false);
		int startOffset = marker.getAttribute(IMarker.CHAR_START, 0);
		StringBuilder builder = null;
		try {
			final Scanner scanner = new Scanner(file.getContents());
			while (scanner.hasNextLine()) {
				final String nextLine = scanner.nextLine();
				if (builder == null) {
					if (startOffset <= nextLine.length()) {
						if (nextLine.indexOf(prefix, startOffset) >= 0) {
							builder = new StringBuilder();
						}
						startOffset = 0;
					} else {
						startOffset = startOffset - nextLine.length() - 1;
					}
				}
				if (builder != null) {
					builder.append(nextLine);
					builder.append("\n");
					if (nextLine.contains(suffix)) {
						break;
					}
				}
			}
			scanner.close();
		} catch (final CoreException e) {
		}
		if (builder != null) {
			return getDiagramText(builder);
		}
		return null;
	}

	public Iterator<ISelection> getDiagramText(final IDocument document) {

		final FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
		int selectionStart = 0;
		final Collection<ISelection> selections = new ArrayList<ISelection>();
		try {
			while (true) {
				final IRegion start = finder.find(selectionStart, prefixRegex, true, true, false, true);
				final IRegion end = finder.find(selectionStart, suffixRegex, true, true, false, true);
				if (start == null || end == null) {
					break;
				}
				final int diagramStart = start.getOffset() + start.getLength() + 1,
						diagramLine = document.getLineOfOffset(diagramStart);
				final String line = document
						.get(document.getLineOffset(diagramLine), document.getLineLength(diagramLine)).trim();
				final ISelection selection = new TextSelection(start.getOffset() + start.getLength(), 0) {
					@Override
					public String toString() {
						return line;
					}
				};
				selections.add(selection);
				selectionStart = end.getOffset() + end.getLength() + 1;
			}
		} catch (final BadLocationException e) {
		}
		return selections.iterator();
	}
}