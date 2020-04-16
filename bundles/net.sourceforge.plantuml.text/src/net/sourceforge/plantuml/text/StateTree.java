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
		
		class Routes {
			
			Stack<Node> route = new Stack<Node>();
			ArrayList<Node> metStates = new ArrayList<Node>();
			
			public Routes(Stack<Node> route, ArrayList<Node> metStates) { 
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
				parent = from.parent;
				nodesMet.addAll(getChildren(from));
				while (!nodeIsRoot) {
					
					if (route.contains(parent)) {
						route.remove(parent);
						
						break;
					} else {
						for (Node node : getChildren(parent)) {
							
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
//			if (parent.equals(root)) {
//				for (Node node : getChildren(parent)) {
//					if (visible) {
//						if (node.index < indexOfTo && !route.contains(node) && node.visible) {
//							nodesMet.add(node);
//						}
//					
//					} else {
//						if (node.index > indexOfFrom && !route.contains(node))
//							nodesMet.add(node);
//					}
//				}
//			}
			
			
			return new Routes(route, nodesMet);
			
			
			//we now have route
			//work down route finding nodes met..
			
		  
		           
		}
		
		


}
