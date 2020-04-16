package net.sourceforge.plantuml.text;

public class StateStore {
	
	public String name;
	public String event;
	public Node parent;
	
	public StateStore(String name, String event, Node parent) {
		this.name = name;
		this.event = event;
		this.parent = parent;
	}
	
	public String toString() { 
		return "Name: "  + this.name;
	}
}
