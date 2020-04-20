package net.sourceforge.plantuml.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Node {
	
	

	String stateName;
	Node parent;
	String event;
	int index;
	boolean visible;
	boolean partOfElseConditional;

	public Node(String stateName, Node parent, String event, boolean visible) {
		this.stateName = stateName;
		this.parent = parent;
		this.event = event;
		this.visible = visible;
		this.index = 0;
		this.partOfElseConditional = false;
 	}
  
	protected void setIndex(int index) {
		this.index = index;
	}
	
	protected void setVisible() {
		this.visible = true;
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
		if (event == null) {
			if (other.event != null)
				return false;
		} else if (!event.equals(other.event))
			return false;
		if (index != other.index)
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
		  return "StateName: " + this.stateName + " Index: " + this.index + " Event: " + this.event + " Visibility: " + this.visible + "\n" + "Parent: " + this.parent; 
	  }
  
	  
	  
		
	}

		  


