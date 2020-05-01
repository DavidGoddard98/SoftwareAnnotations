package utils;

//Class used to all state machine references in the editor such as their lineNum, charStart and end....
public class StateReference {

	public String theLine;
	public String editorLine;
	public int lineNum;
	public int charStart;
	public int charEnd;
	public boolean isTransition;
	public Transition transition;
	public boolean isPlantUML;
	
	public String stateName;
	
	public StateReference(String theLine, String editorLine, int lineNum, int charStart, int charEnd) {
		this.theLine = theLine;
		this.editorLine = editorLine;
		this.lineNum = lineNum;
		this.charStart = charStart;
		this.charEnd = charEnd;
		this.isTransition = false;
	}
	
	public StateReference(String theLine, String editorLine, int lineNum, Transition transition) {
		this.theLine = theLine;
		this.editorLine = editorLine;
		this.lineNum = lineNum;
		this.isTransition = true;
		this.transition = transition;
	}
	
	public StateReference(String stateName, String theLine, String editorLine, int lineNum, int charStart, int charEnd) {
		this.stateName = stateName;
		this.theLine = theLine;
		this.editorLine = editorLine;
		this.lineNum = lineNum;
		this.charStart = charStart;
		this.charEnd = charEnd;
		this.isTransition = false;
	}
	
	public StateReference(String stateName, String editorLine, int lineNum, int charStart, int charEnd, boolean plantUML) {
		this.stateName = stateName;
		this.editorLine = editorLine;
		this.lineNum = lineNum;
		this.charStart = charStart;
		this.charEnd = charEnd;
		this.isTransition = false;
		this.isPlantUML = plantUML;
	}
	
	public StateReference() {
		
	}
	
	public String toString() {
		return this.theLine;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StateReference other = (StateReference) obj;
		if (charEnd != other.charEnd)
			return false;
		if (charStart != other.charStart)
			return false;
		if (editorLine == null) {
			if (other.editorLine != null)
				return false;
		} else if (!editorLine.equals(other.editorLine))
			return false;
		if (isPlantUML != other.isPlantUML)
			return false;
		if (isTransition != other.isTransition)
			return false;
		if (lineNum != other.lineNum)
			return false;
		if (stateName == null) {
			if (other.stateName != null)
				return false;
		} else if (!stateName.equals(other.stateName))
			return false;
		if (theLine == null) {
			if (other.theLine != null)
				return false;
		} else if (!theLine.equals(other.theLine))
			return false;
		if (transition == null) {
			if (other.transition != null)
				return false;
		} else if (!transition.equals(other.transition))
			return false;
		return true;
	}
			
	
}
