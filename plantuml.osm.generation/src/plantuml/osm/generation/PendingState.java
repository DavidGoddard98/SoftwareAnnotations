package plantuml.osm.generation;

public class PendingState {
	//This class is used to store various information about an '//FSM:' line in the editor and is only used
	//when the user suffix's the line with '{'. This means that the user wants to highlight more than 
	//just the line, i.e. some code. This object is stored in a stack until a following '}' is found. 
	String theLine;
	String editorLine;
	int lineNum;
	
	PendingState(String theLine, String editorLine, int lineNum) {
		this.theLine = theLine;
		this.editorLine = editorLine;
		this.lineNum = lineNum;
	}
	
}
