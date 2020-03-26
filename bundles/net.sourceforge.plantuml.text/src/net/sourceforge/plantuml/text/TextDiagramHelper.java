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
	boolean toggle = true;

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

	private void initializeKeys(IResource resource, IPath path, IDocument document) {
		try {
			allMarkers = resource.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO);

			for (IMarker aMarker : allMarkers) {

				String message = aMarker.getAttribute(IMarker.MESSAGE, "");
				String subString = message.substring(0, 3);

				if (subString.equals("FSM")) {
					String theLine = message.substring(5, message.length());
					int lineNum = aMarker.getAttribute(IMarker.LINE_NUMBER, 0);
					String sameLineInDoc = document
							.get(document.getLineOffset(lineNum - 1), document.getLineLength(lineNum - 1)).trim();

					String className = path.toFile().getName();
					String key = className + theLine + String.valueOf(lineNum);

					if (sameLineInDoc.equals(theLine) == false) {
		
						System.out.println("DELETING KEY AND MARKER");
						aMarker.delete();
						plantMarkerKey.remove(key);
						continue;
					}
					if (plantMarkerKey.contains(key)) {
						continue;
					}
					plantMarkerKey.add(key);

					System.out.println("initialized keys for file: " + className + "Key = :" + key);
				}
			}
		} catch (CoreException e) {
			System.out.println("Failed to initialise keys");
		} catch (BadLocationException e) {
			System.out.println("Couldnt find line in docuent");
		}
	}

	// TODO: onStartup initialize keylist with FSM bookmarks
	// Delete keys on marker deletion?
	private void createKey(String theLine, int lineNum, IPath path, boolean toggle, IRegion region) {
		if (!validateLine(theLine))
			return;
		String className = path.toFile().getName();
		String key = className + theLine + String.valueOf(lineNum + 1);
		if (plantMarkerKey.contains(key)) {
			// System.out.println("Key exists: " + key);
			return;
		}
		plantMarkerKey.add(key);
		addTask(theLine, lineNum, path, region);
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
	    	if (theLine.contains("->"))
	    		continue;
	    	if (theLine.contains(stateName)) { 
	    		stateLines.append(diagramText.get(theLine) + ",");
	    	}
	    }
		return stateLines;
	}

	private static String backwardStateLink(String stateName, String className, int lineNum) {		
		return "state " + stateName + "[["+className+"#FSM#"+ lineNum +"]]";

	}

	private String backwardTransitionLink(String aLine, String className, int lineNum) {
		return aLine + " : " + "[["+className+"#FSM#"+lineNum+"]]";

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
		        				  int charStart =  (int)m.getAttribute(IMarker.CHAR_START); 
				    		      int charEnd =  (int)m.getAttribute(IMarker.CHAR_END);
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
	    		result.append(backwardStateLink(stateName, className, lineNum));
	    		result.append("\n");
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
		boolean toggle = true;
		initializeKeys(root, path, document);
		
	
		
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
					
				
					
				

					

					final int endOffset = end.getOffset() + end.getLength();
					StringBuilder result = new StringBuilder();
					final int maxLine = Math.min(document.getLineOfOffset(endOffset) + (includeEnd ? 1 : 0),
							document.getNumberOfLines());

					diagramText.clear();
					boolean fsm = false;
					for (int lineNum = startLine + (includeStart ? 0 : 1); lineNum < maxLine; lineNum++) {
						final String line = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum)).trim();
						diagramText.put(line, lineNum + 1);
						if (ensureFSM(line)) {
								fsm = true;
						}
					}

					String className = path.toFile().getName();
					className = className.substring(0, className.length()- 5);
					
					removeHighlights(root);
					HashSet<String> doneStates = new HashSet<String>();

//					System.out.println("Package: "+className.getPackage());
					for (int lineNum = startLine + (includeStart ? 0 : 1); lineNum < maxLine; lineNum++) {
						final String line = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum)).trim();
						IRegion markerRegion = finder.find(0, line, true, true, false, false);

						createKey(line, lineNum, path, toggle, markerRegion);
						
							
						addToList(line, lineNum, className, result, doneStates);
						
						//add transitions and their links
						if (line.contains("->") && fsm)  {
							if (selectedLineNum == lineNum) {
								String colorTransition = forwardTransitionLink(selectedLine);
								result.append(backwardTransitionLink(colorTransition, className, lineNum));
							} else
								result.append(backwardTransitionLink(line, className, lineNum));
						}
						//add states and their links
						else if (line.contains("State") && fsm) {
							if (selectedLineNum == lineNum) {
								String stateName = getStateName(line);
								String colorState = forwardStateLink(selectedLine);

								result.append(line);
								result.append("\n");
								result.append(colorState);
								displayMarkers(stateName, className, root);

							} else {
								System.out.println("line not selected");
								result.append(line);
							}
						} else
							result.append(line);
						
						if (!line.endsWith("\n")) {
							result.append("\n");
						}
					
					}
					markerAttributes.put(IMarker.CHAR_START, start.getOffset());
					initializeKeys(root, path, document);

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