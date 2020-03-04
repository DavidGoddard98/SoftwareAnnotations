package net.sourceforge.plantuml.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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

	public TextDiagramHelper(final String prefix, final String prefixRegex, final String suffix, final String suffixRegex) {
		super();
		this.prefix = prefix;
		this.prefixRegex = prefixRegex;
		this.suffix = suffix;
		this.suffixRegex = suffixRegex;
	}
	
	/**
	 * @startuml

[*] --> State1
State1 --> [*]
State1 : this is a string
State1 : this is another string

State1 -> State2
State2 --> [*]

@enduml
	 */
	public StringBuilder getDiagramTextLines(final IDocument document, final int selectionStart, final Map<String, Object> markerAttributes, IEditorInput editorInput) {
		final boolean includeStart = prefix.startsWith("@"), includeEnd = suffix.startsWith("@");
		final FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
		IPath path = ((IFileEditorInput)editorInput).getFile().getFullPath();
		System.out.println("PATHHH" + path.toString());
		System.out.println("Path file" + path.toFile().toString());
		System.out.println("getDiagramTextLines");
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
				final int startOffset = start.getOffset(), startLine = document.getLineOfOffset(startOffset + (includeStart ? 0 : start.getLength()));

				final IRegion end = finder.find(startOffset + start.getLength(), suffixRegex, true, true, false, true);
				if (end != null && end.getOffset() >= selectionStart) {
					int selectedLineNum = document.getLineOfOffset(selectionStart);
					String selectedLine = document.get(document.getLineOffset(selectedLineNum), document.getLineLength(selectedLineNum)).trim();
					boolean transitionSelected = false;
					boolean stateSelected = false;
					int indexOfTrans = 0;
					String test ="";
					String colorState = "";
					char tmp = 'c';
			
					for (int i=0; i< selectedLine.length(); i++) {
						if (selectedLine.charAt(i) == ' ' && selectedLine.charAt(i + 1) == ':') {
							colorState = "state " + selectedLine.substring(0, i) + " #green";
							stateSelected = true;
						}
						char c = selectedLine.charAt(i);
						if (c == '>' && tmp == '-') {
							transitionSelected = true;
							indexOfTrans = selectedLine.indexOf('-');
							test = selectedLine.substring(0,indexOfTrans) + "-[thickness=5,#blue]" + selectedLine.substring(indexOfTrans+1, selectedLine.length());
						}
						
						tmp = c;
					}

					final int endOffset = end.getOffset() + end.getLength();
					//					String linePrefix = document.get(startLinePos, startOffset - startLinePos).trim();
					final StringBuilder result = new StringBuilder();
					final int maxLine = Math.min(document.getLineOfOffset(endOffset) + (includeEnd ? 1 : 0), document.getNumberOfLines());
					for (int lineNum = startLine + (includeStart ? 0 : 1); lineNum < maxLine; lineNum++) {
						final String line = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum)).trim();
						if (transitionSelected == true && selectedLineNum == lineNum) 
							result.append(test);
						else if (stateSelected == true && selectedLineNum == lineNum) {
							result.append(colorState);
							result.append("\n");

							result.append(line);
						} else
							result.append(line);
						if (! line.endsWith("\n")) {
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
		System.out.println("getDiagramText - line 82");
		
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
				final int diagramStart = start.getOffset() + start.getLength() + 1, diagramLine = document.getLineOfOffset(diagramStart);
				final String line = document.get(document.getLineOffset(diagramLine), document.getLineLength(diagramLine)).trim();
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
