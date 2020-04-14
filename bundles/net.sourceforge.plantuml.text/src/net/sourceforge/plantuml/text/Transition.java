package net.sourceforge.plantuml.text;

public class Transition extends StateTextDiagramHelper {

	//Used to store information about transitions such as the leftState name and the relevant char positions of everything in the transition
	protected String leftState;
	protected int leftCharStart;
	protected int leftCharEnd;

	protected String rightState;
	protected int rightCharStart;
	protected int rightCharEnd;
	
	protected int multiLineStart;
	protected int multiLineEnd;
	protected boolean multiLineTransition;
	
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
