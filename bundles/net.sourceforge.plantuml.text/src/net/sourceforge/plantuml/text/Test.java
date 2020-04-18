package net.sourceforge.plantuml.text;

import net.sourceforge.plantuml.text.StateTree;
import net.sourceforge.plantuml.text.StateTree.Routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import net.sourceforge.plantuml.text.Node;


public class Test {
	
	  
	public Routes getRoute(Node from, Node to) {
		  
		System.out.println();
		int indexOfFrom = from.index;
		int indexOfTo = to.index;
		Stack<Node> route = new Stack<Node>();
		route.push(to);
		ArrayList<Node> nodesMet = new ArrayList<Node>();
		Node parent = to.parent;
		System.out.println(parent);
		boolean visible = to.visible;
		if (!visible) nodesMet.addAll(getChildren(to));
		
		boolean nodeIsRoot = false;
		
		while (!nodeIsRoot) {
			
			if (parent.equals(from)) {
				route.push(parent);
				for (Node node : getChildren(parent)) {
					if (visible) {
						if (node.index < indexOfTo && node.visible) {
							nodesMet.add(node);
						} 
					} else {
					
						if (node.index != indexOfTo && !route.contains(node))
							nodesMet.add(node);
					}
					 
					
				}
				while (!parent.equals(root)) {
					parent = parent.parent;
					if (!visible) {
						for (Node node : getChildren(parent)) {

							if (node.index != indexOfTo && node.index >indexOfFrom && !route.contains(node))
								nodesMet.add(node);
							
						}
					}
				}
				break;
			}
			else {
				for (Node node : getChildren(parent)) {
					if (visible) {
						if (node.index < indexOfTo && node.index > indexOfFrom && node.visible) {
							nodesMet.add(node);
						}
					} else {
						if (node.index != indexOfTo && node.index > indexOfFrom && !route.contains(node) )
							nodesMet.add(node);
	
					}
				}
			}
			if (parent.visible && !parent.equals(root) && !(indexOfFrom > parent.index)) {
				route = new Stack<Node>();
				nodesMet = new ArrayList<Node>();
				
				if (parent.equals(root)) {
					route.add(to);
				} else route.add(parent);
				return null;
			}
			route.add(parent);
			if (parent.equals(root)) {
				nodeIsRoot = true;
			} else {
				parent = parent.parent;
			}

		}
		
		if (route.contains(from)) {
			route.remove(from);
		}
		else if (!from.equals(root)) { //if null parent is root so route = route.
			nodeIsRoot = false;
			for (Node child : getChildren(from)) {
//				if (child.index > indexOfFrom && child.event.equals("unconditional")) return null;
			}
			parent = from.parent;
			nodesMet.addAll(getChildren(from));
			while (!nodeIsRoot) {
				
				if (route.contains(parent)) {
					route.remove(parent);
					
					break;
				} else {
					for (Node node : getChildren(parent)) {
//						if (node.index > indexOfFrom && node.event.equals("unconditional")) return null;

						if (visible) {
							if (node.index > indexOfFrom && node.index < indexOfTo && node.visible) {
								nodesMet.add(node);

							}
						
						} else {
	 						if (node.index > indexOfFrom)
								nodesMet.add(node);

						}
					
					}
											
				}
				if (parent.equals(root)) {
					nodeIsRoot = true;
				} else {
					parent = parent.parent;
				}

			}
		}
	
	 
    public static void main(String[] args) {
    	ArrayList<Node> route = new ArrayList<Node>();
    	
    	
    	Node root =  new Node("Climb", null, "", true);

         Node descend =  new Node("Descend", root, "EventA", false);
         Node to_alca =  new Node("To_alca", descend, "EventB", false);
         Node over_alca =  new Node("over_alca", root, "EventC", false);
         Node up =  new Node("Up", over_alca, "EventD", false);
         
         Node one = new Node("one", root, "EventA", false);
         Node two = new Node("two", one, "EventB", false);
         Node three = new Node("three", two, "EventC", false);
         Node four = new Node("four", one, "EventD", false);
         Node five = new Node("five", four, "EventE", false);
         Node six = new Node("six", one, "EventF", false);
         Node seven = new Node("seven", root, "EventG", false);

         Node eight = new Node("eight", seven, "EventH", false);

         
         StateTree theTree = new StateTree(root);
         theTree.addNode(root, one);
         theTree.addNode(one, two);
         theTree.addNode(two, three);
         theTree.addNode(one, four);
         theTree.addNode(four, five);
         theTree.addNode(one, six);
         theTree.addNode(root, seven);
         theTree.addNode(seven, eight);

         
	     
	   // visibleStates = theTree.getSuccessors(over_alca);
//	    for (Node node : visibleStates) { 
//	    	System.out.println(node.toString());
//	    }
        route = theTree.getRoute(one
        		, seven);
         for (Node node : route) {
        	 System.out.println(node.toString());
         }
    }
}
