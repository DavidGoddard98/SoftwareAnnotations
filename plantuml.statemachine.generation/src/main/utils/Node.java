package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Node {
	
	
	
	public String stateName;
	public String editorLine;
	public Node parent;
	public int index;
	public boolean visible;
	public int charStart;
	public int charEnd;
	public int lineNum;
	public Event event;
	public ArrayList<Action> action;

	public Node(String stateName, String editorLine, boolean visible, int charStart, int charEnd, int lineNum, Event event) {
		this.stateName = stateName;
		this.editorLine = editorLine;
		this.parent = null;
		this.visible = visible;
		this.index = 0;
		this.action = new ArrayList<Action>();
		this.charStart = charStart;
		this.charEnd = charEnd;
		this.lineNum = lineNum;
		this.event = event;
 	}
  
	public void setIndex(int index) {
		this.index = index;
	}
	
	public void setVisible() {
		this.visible = true;
	}
	
	public void setParent(Node parent) {
		this.parent = parent;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (charEnd != other.charEnd)
			return false;
		if (charStart != other.charStart)
			return false;
		if (editorLine == null) {
			if (other.editorLine != null)
				return false;
		} else if (!editorLine.equals(other.editorLine))
			return false;
		if (event == null) {
			if (other.event != null)
				return false;
		} else if (!event.equals(other.event))
			return false;
		if (index != other.index)
			return false;
		if (lineNum != other.lineNum)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (stateName == null) {
			if (other.stateName != null)
				return false;
		} else if (!stateName.equals(other.stateName))
			return false;
		if (visible != other.visible)
			return false;
		return true;
	}

	public String toString() {
		  return "StateName: " + this.stateName + " Index: " + this.index + " Event: " + this.event.event + " Visibility: " + this.visible + "\n" + "Parent: " + this.parent; 
	  }
  
	  
	  
		
	}

		  


