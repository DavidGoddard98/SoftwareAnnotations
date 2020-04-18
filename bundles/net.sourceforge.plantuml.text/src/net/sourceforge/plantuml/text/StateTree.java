package net.sourceforge.plantuml.text;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

public class StateTree {
	

	  ArrayList<Node> nodes;
		ArrayList<Node> children;
		HashMap<Node, ArrayList<Node>> links;
		Node root;
		int currentIndex = 1;
	  
		public StateTree(Node root) {
			this.nodes = new ArrayList<Node>();
			this.links = new HashMap<Node, ArrayList<Node>>();
			nodes.add(root);
			links.put(root, new ArrayList<Node>());
			this.root = root;
		}
		
		class TransitionInformation {
			
			ArrayList<Node> route = new ArrayList<Node>();
			ArrayList<Node> metStates = new ArrayList<Node>();
			
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
	  
	  
		public void addNode(Node parent, Node node) {
			node.setIndex(currentIndex);
			this.nodes.add(node);
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

		public ArrayList<Node> getChildren(Node parent) {
			if (links.get(parent) != null)
				return links.get(parent);
			return new ArrayList<Node>();
		}
		
		
		//MAKE ARRAYLIST OF DESTINATION TO ROOT...
		public ArrayList<Node> rootToDestination(Node from, Node to, Node destination) {

			ArrayList<Node> route = new ArrayList<Node>();
			
			while (true) {
				
				for (Node node : getChildren(destination)) {
					if (node.event.equals("unconditional") && node.visible) {
						if (!to.visible && node.index > to.index) 	return null;
						else if (node.index < to.index && from.index < node.index) return null;


					}
				}
				destination = destination.parent;
				if (destination.visible && !destination.equals(from) && destination.index > from.index) 
					return null;					
				
				
				route.add(destination);
				if (destination.equals(root)) {
					return route;
				}
			}
			
		}
		
		
		public boolean findUnconditional(Node from, Node to) {
			for (Node child : getChildren(from)) {
				if (child.event.equals("unconditional") && child.visible && to.index > from.index && !child.equals(to) && !to.visible)
					return true;
			
			}
			return false;
		}
		
		public TransitionInformation getRoute(Node from, Node to) {
			ArrayList<Node> toDestination = rootToDestination(from, to, to);
			if (toDestination == null) return null;
			ArrayList<Node> nodesMet = new ArrayList<Node>();
			ArrayList<Node> route = new ArrayList<Node>();
			//if (unconditionalParents.contains(from)) return null;
			int fromIndex = from.index;
			boolean nodeFound = false;
			if (findUnconditional(from, to)) return null;
			boolean checker = false;
			while (!nodeFound) {
				
				for (Node child : getChildren(from)) {
					System.out.println("at child: " + child);
					System.out.println("looking for: " + to);
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
			if (node.visible && node.event.equals("unconditional") && !to.equals(node)) {
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
		
		



