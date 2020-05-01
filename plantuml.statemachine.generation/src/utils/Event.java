package utils;

public class Event {
	
	
	
	public String event;
	public String editorLine; 
	public int multiLineStart;
	public int multiLineEnd;
	public int lineNum;
	
	public Event(String event, String editorLine, int multiLineStart, int multiLineEnd, int lineNum) {
		this.event = event;
		this.editorLine = editorLine;
		this.multiLineStart = multiLineStart;
		this.multiLineEnd = multiLineEnd;
		this.lineNum = lineNum;
	}
	
	public Event(String event) {
		this.event = event;
	}
	
	public void setLineEnd(int charEnd) {
		this.multiLineEnd = charEnd;
	}
	
	public void printTest() {
		System.out.println("printest");
	}
	

}


