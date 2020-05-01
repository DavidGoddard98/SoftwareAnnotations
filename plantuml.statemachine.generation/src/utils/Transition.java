package utils;

public class Transition  {

	//Used to store information about transitions such as the leftState name and the relevant char positions of everything in the transition
	public String leftState;
	public int leftCharStart;
	public int leftCharEnd;

	public String rightState;
	public int rightCharStart;
	public int rightCharEnd;
	
	public int multiLineStart;
	public int multiLineEnd;
	public boolean multiLineTransition;
	
	public Transition(String leftState, String rightState, int leftCharStart, int leftCharEnd, int rightCharStart, int rightCharEnd) {
		this.leftState = leftState;
		this.rightState = rightState;
		this.leftCharStart = leftCharStart;
		this.leftCharEnd = leftCharEnd;
		this.rightCharStart = rightCharStart;
		this.rightCharEnd = rightCharEnd;
		this.multiLineTransition = false;
	}
	
	public Transition(String leftState, String rightState, int leftCharStart, int leftCharEnd, int rightCharStart, int rightCharEnd, int multiLineStart, int multiLineEnd) {
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
	
	
	public Transition() {
		
	}
	
	public String toString() {
		return this.leftState + " -> " + this.rightState;
	}
	
}
