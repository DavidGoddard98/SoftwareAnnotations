package net.sourceforge.plantuml.text;


//Class used to all state machine references in the editor such as their lineNum, charStart and end....
public class StateReference extends StateTextDiagramHelper {

	protected String theLine;
	protected String editorLine;
	protected int lineNum;
	protected int charStart;
	protected int charEnd;
	protected boolean isTransition;
	protected Transition transition;
	
	protected String stateName;
	
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
	
	StateReference(String stateName, String theLine, String editorLine, int lineNum, int charStart, int charEnd) {
		this.stateName = stateName;
		this.theLine = theLine;
		this.editorLine = editorLine;
		this.lineNum = lineNum;
		this.charStart = charStart;
		this.charEnd = charEnd;
		this.isTransition = false;
	}
	
	StateReference() {
		
	}
	
	public String toString() {
		return this.theLine;
	}
	
	
	
	
	
			
	
}
