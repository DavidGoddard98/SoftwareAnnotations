package utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

import plantuml.statemachine.generation.StateTextDiagramHelper;
import utils.StateTree.TransitionInformation;

public class StateTree {
	
	public ArrayList<Node> nodes;
	public ArrayList<Node> children;
	public HashMap<Node, ArrayList<Node>> links;
	public HashMap<Node , ArrayList<Node>> noLink;
	public Node root;
	public int currentIndex = 1;
	  
	public StateTree(Node root) {
		this.nodes = new ArrayList<Node>();
		this.links = new HashMap<Node, ArrayList<Node>>();
		this.noLink = new HashMap<Node, ArrayList<Node>> ();
		nodes.add(root);
		links.put(root, new ArrayList<Node>());
		this.root = root;
	}
	
		
	public class TransitionInformation {
			
		public ArrayList<Node> route = new ArrayList<Node>();
		public ArrayList<Node> metStates = new ArrayList<Node>();
			
		public TransitionInformation(ArrayList<Node> route, ArrayList<Node> metStates) { 
			this.route = route;
			this.metStates = metStates;
		}
	}
		
	public Node getNode(String nodeName) {
		for (Node node : nodes) {
			if (node.stateName.equals(nodeName)) return node;
		}
		System.out.println("Couldnt find node");
		return null;
	}
		
	public Node findLastUnconditionalState() {
		Node lastUnconditional = null;
		int highestIndex = 0;
		System.out.println("here");
		for (Node state : nodes) {
			if (state.event.event.equals("unconditional") && state.visible) {
				if (state.index > highestIndex) {
					highestIndex = state.index;
					lastUnconditional = state;

				}
			}
		}
		if (highestIndex == 0) return this.root;
		return lastUnconditional;
	}
	
	public void addNoLink(Node parent, Node node) {
		if(noLink.containsKey(parent)) {
			noLink.get(parent).add(node);
		} else {
			ArrayList<Node> newOne = new ArrayList<Node>();
			newOne.add(node);
			noLink.put(parent, newOne);
			System.out.println("That parent does not exist in tree");
		}
	}
  
	public void addNode(Node parent, Node node) {
		node.setIndex(currentIndex);
		this.nodes.add(node);
		node.setParent(parent);
		if(links.containsKey(parent)) {
			links.get(parent).add(node);
		} else {
			ArrayList<Node> newOne = new ArrayList<Node>();
			newOne.add(node);
			links.put(parent, newOne);
			System.out.println("That parent does not exist in tree");
		}
		currentIndex ++;
	}
	
	public ArrayList<Node> getNodeAndAllDescendants(Node parent) {
		if (!this.nodes.contains(parent)) return null;
		ArrayList<Node> allDescendants = new ArrayList<Node>();
		if (parent.visible) allDescendants.add(parent);
		for (Node node : nodes) {
			if (node.index > parent.index && node.visible) {
				allDescendants.add(node);
			}
		}
		return allDescendants;
	}
			


	public ArrayList<Node> getChildren(Node parent) {
		if (links.get(parent) != null)
			return links.get(parent);
		//if (!nodes.contains(parent)) return null;
		else return new ArrayList<Node>();
	}
		
	public boolean checkForUnconditional(Node from, Node to, Node destination) {
		for (Node node : getChildren(destination)) {
			if (node.event.event.equals("unconditional") && node.visible) {
				if (!to.visible && node.index > to.index) 	return true;
				else if (node.index < to.index && from.index < node.index) return true;


			}
		}
		return false;

	}
		
	public void removeLastNode() {
		int highestIndex = 0;
		Node lastNode = this.root;
		for (Node node : nodes) {
			if (node.index > highestIndex) {
				highestIndex = node.index;
				lastNode = node;
			}
		}
		if (highestIndex != 0) {
			this.nodes.remove(lastNode);

		}
		
	}
	
	
	//MAKE ARRAYLIST OF DESTINATION TO ROOT...
	public ArrayList<Node> rootToDestination(Node from, Node to, Node destination) {

		ArrayList<Node> route = new ArrayList<Node>();
		
		if (checkForUnconditional(from, to, destination)) return null;
		
		while (true) {
		
			destination = destination.parent;
			if (destination.visible && !destination.equals(from) && destination.index > from.index) 
				return null;					
			if (checkForUnconditional(from, to, destination)) return null;
			
			route.add(destination);
			if (destination.equals(root)) {
				return route;
			}
		}
		
	}
	

	
	public TransitionInformation getRoute(Node from, Node to) {
		ArrayList<Node> toDestination = rootToDestination(from, to, to);
		if (toDestination == null) return null;
		ArrayList<Node> nodesMet = new ArrayList<Node>();
		ArrayList<Node> route = new ArrayList<Node>();
		if (noLink.containsKey(from) && noLink.get(from).contains(to)) return null;
		if (to.index < from.index) return null;
		int fromIndex = from.index;
		boolean nodeFound = false;
		boolean checker = false;
		while (!nodeFound) {
			
			for (Node child : getChildren(from)) {
				
				if (child.index > fromIndex) {
					
					String searchInfo = findChildren(child, from, to, nodesMet, route, toDestination, to.visible);
					
					if (searchInfo.equals("true")) {
						nodeFound = true;
						break; 		
					} else if (searchInfo.equals("unconditional")) {
						nodeFound = true;
						checker = true;
						break;
					}
				}
			}
			if (!from.equals(root))
				from = from.parent;
		}
		if (checker) return null;
		return new TransitionInformation(route, nodesMet);
			
	}
	


	
	public String findChildren(Node node, Node from, Node to, ArrayList<Node> nodesMet, ArrayList<Node> route, ArrayList<Node> rootToDestination, boolean destinationNodeVisible) {
		if (node.visible && node.event.event.equals("unconditional") &&  !node.equals(to) && !from.event.equals("else-unconditional") ) {
			
				return "unconditional";
		
		} else if (node.equals(to)) {
			route.add(node);
			if (!destinationNodeVisible) {
				for (Node child : getChildren(node)) {
					String searchInfo = findChildren(child, from, to, nodesMet, route, rootToDestination, destinationNodeVisible);
					if (searchInfo.equals("unconditional")) return searchInfo;
				}
			}
			return "true";
		}
		else if (rootToDestination.contains(node)) {
			route.add(node);
			for (Node child : getChildren(node)) {
				String searchInfo = findChildren(child, from,  to, nodesMet, route, rootToDestination, destinationNodeVisible);
				if (searchInfo.equals("true") || searchInfo.equals("unconditional")) return searchInfo;
			}
		
		} else if (node.visible) {
			nodesMet.add(node); 
		} else {
			if (!destinationNodeVisible) nodesMet.add(node);
			for (Node child : getChildren(node)) {
				String searchInfo = findChildren(child, from, to, nodesMet, route, rootToDestination, destinationNodeVisible);
				if (searchInfo.equals("true")|| searchInfo.equals("unconditional")) return searchInfo;
			}
		}
		return "false";
	}
		
		           
}
		
		



