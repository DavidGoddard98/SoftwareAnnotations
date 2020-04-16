package net.sourceforge.plantuml.text;

import net.sourceforge.plantuml.text.StateTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import net.sourceforge.plantuml.text.Node;


public class Test {
	
	  

	
	 
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
