package net.sourceforge.plantuml.text;

public class Event {
	
	
	
	String event;
	String editorLine; 
	int multiLineStart;
	int multiLineEnd;
	int lineNum;
	
	Event(String event, String editorLine, int multiLineStart, int multiLineEnd, int lineNum) {
		this.event = event;
		this.editorLine = editorLine;
		this.multiLineStart = multiLineStart;
		this.multiLineEnd = multiLineEnd;
		this.lineNum = lineNum;
	}
	
	Event(String event) {
		this.event = event;
	}
	
	public void setLineEnd(int charEnd) {
		this.multiLineEnd = charEnd;
	}

}


