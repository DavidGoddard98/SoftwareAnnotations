package plantuml.statemachine.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import utils.Event;
import utils.Node;
import utils.StateTree;

public class StateTreeTest {
	
	static StateTree stateTree;
	
	public Node createExampleNode() {
		String stateName = "ExampleState";
		String editorLine = "state = valid_states.ExampleState";
		return new Node(stateName, editorLine, true, 0, 0, 0, new Event("an event"));
	}
	
	public Node createExampleNode2() {
		String stateName = "ExampleState_2";
		String editorLine = "state = valid_states.ExampleState_2";
		return new Node(stateName, editorLine, true, 0, 0, 0, new Event("an event_2"));
	}
	
	public Node createExampleNode3() {
		String stateName = "ExampleState_3";
		String editorLine = "state = valid_states.ExampleState_3";
		return new Node(stateName, editorLine, true, 0, 0, 0, new Event("an event_3"));
	}
	
	public Node createInvisibleNode() {
		String stateName = "InvisibleNode";
		String editorLine = "state = valid_states.InvisibleNode";
		return new Node(stateName, editorLine, false, 0, 0, 0, new Event("invisible event"));
	}
	
	public Node createUnconditionalNode() {
		String stateName = "unConditional";
		String editorLine = "state = valid_states.unconditional";
		return new Node(stateName, editorLine, true, 0, 0, 0, new Event("unconditional"));
	}
	
	public void initializeTree() {
		String stateName = "rootNode";
		String editorLine = "state = valid_states.EditorLine";
		Node root = new Node(stateName, editorLine, true, 0, 0, 0, new Event("an event"));
		stateTree = new StateTree(root);
	}
	
	
	@Test
	public void checkTreeCreated() {
		Node theNode = createExampleNode();
		StateTree theTree = new StateTree(theNode);
		assertEquals(theNode, theTree.nodes.get(0));
	}
	
	@Test
	public void checkNodeSuppliedBecomesRoot() {
		Node theNode = createExampleNode();

		StateTree theTree = new StateTree(theNode);
		assertEquals(theNode, theTree.root);
	}
	
	@Test
	public void checkgetNodeReturnsNode() {
		Node theNode = createExampleNode();

		StateTree theTree = new StateTree(theNode);
		assertEquals(theNode, theTree.getNode("ExampleState"));
	}
	
	@Test
	public void checkGetNodeReturnsNullIfInvalidNodeSupplied() {
		Node theNode = createExampleNode();

		StateTree theTree = new StateTree(theNode);
		assertNull(theTree.getNode("FalseNode"));

	}
	
	@Test
	public void checkAddingNodeToRoot() {
		initializeTree();
		Node theNode = createExampleNode();
		stateTree.addNode(stateTree.root, theNode);
		assertTrue(stateTree.nodes.contains(theNode));
	}
	
	@Test
	public void checkAddingNodeToAnotherNode() {
		initializeTree();
		Node aNode = createExampleNode();
		Node aNode_2 = createExampleNode2();
		
		stateTree.addNode(stateTree.root, aNode);
		stateTree.addNode(aNode, aNode_2);
		
		assertTrue(stateTree.links.get(aNode).contains(aNode_2));
		
	}
	
	@Test
	public void checkRemoveLastNodeRemovesLastNode() {
		initializeTree();
		Node aNode = createExampleNode();
		stateTree.addNode(stateTree.root, aNode);
		stateTree.removeLastNode();
		assertFalse(stateTree.nodes.contains(aNode));
		
	}
	
	@Test 
	public void checkRemoveLastNodeDoesNothingIfJustRoot() {
		initializeTree();
		stateTree.removeLastNode();
		assertNotNull(stateTree.root);
	}
	
	public @Test void checkGetChildrenWithNoChildReturnsNoChild() {
		initializeTree();
		assertTrue(stateTree.getChildren(stateTree.root).size() == 0);
	}
	
	public @Test void checkGetChildrenWithInValidNodeReturnsEmpty() {
		initializeTree();
		assertTrue(stateTree.getChildren(createExampleNode()).size() == 0);
	}
	
	@Test
	public void checkGetChildrenWithOneChildReturnsOneChild() {
		initializeTree();
		Node aNode = createExampleNode();
		stateTree.addNode(stateTree.root, aNode);
		assertTrue(stateTree.getChildren(stateTree.root).size() == 1);
		
	}
	
	@Test
	public void checkGetChildrenWithOneChildReturnsCorrectChild() {
		initializeTree();
		Node aNode = createExampleNode();
		stateTree.addNode(stateTree.root, aNode);
		assertTrue(stateTree.getChildren(stateTree.root).contains(aNode));
	}
	
	@Test 
	public void checkGetChildrenWithMoreThanOneChildReturnsCorrectChildren() {
		initializeTree();
		Node aNode = createExampleNode();
		Node aNode_2 = createExampleNode2();
		stateTree.addNode(stateTree.root, aNode);
		stateTree.addNode(stateTree.root, aNode_2);
		ArrayList<Node> sampleChild = new ArrayList<Node>();
		sampleChild.add(aNode);
		sampleChild.add(aNode_2);
		assertEquals(sampleChild, stateTree.getChildren(stateTree.root));
		
	}
	
	@Test
	public void checkFindLastUnconditionalReturnsLastUnconditional() {
		initializeTree();
		Node unconditionalNode = createUnconditionalNode();
		stateTree.addNode(stateTree.root, unconditionalNode);
		assertEquals(unconditionalNode, stateTree.findLastUnconditionalState());
	}
	
	@Test
	public void checkFindLastUnconditionalReturnsRootIfNoOtherInTree() {
		initializeTree();
		Node aNode = createExampleNode();
		stateTree.addNode(stateTree.root, aNode);
		assertEquals(stateTree.root, stateTree.findLastUnconditionalState());
	}
	
	@Test
	public void checkFindLastUnconditionalReturnsLastUncondWith2Unconditionals() {
		initializeTree();
		Node unconditionalNode = createUnconditionalNode();
		stateTree.addNode(stateTree.root, unconditionalNode);
		Node aSecondUnconditional = createUnconditionalNode();
		stateTree.addNode(unconditionalNode, aSecondUnconditional);

		assertEquals(aSecondUnconditional, stateTree.findLastUnconditionalState());
	}
	
	@Test
	public void checkFindLastUnconditionalReturnsLastUnconditionalIfConditionalPreceedesIt() {
		initializeTree();
		Node unconditionalNode = createUnconditionalNode();
		Node conditionalNode = createExampleNode();
		stateTree.addNode(stateTree.root, unconditionalNode);
		stateTree.addNode(stateTree.root, conditionalNode);

		assertEquals(unconditionalNode, stateTree.findLastUnconditionalState());
	}
	
	@Test
	public void checkgetNodeAndAllDescendantsReturnsNoneIfNone() {
		initializeTree();
		ArrayList<Node> expected = new ArrayList<Node>(
	            Arrays.asList(stateTree.root)
	                        
				);
		assertEquals(expected, stateTree.getNodeAndAllDescendants(stateTree.root));
	}
	
	@Test 
	public void checkGetNodeAndAllDescendantsReturnsOneDescendantsIfOne() {
		initializeTree();
		Node aNode = createExampleNode();
		stateTree.addNode(stateTree.root, aNode);
		ArrayList<Node> expected = new ArrayList<Node>(
	            Arrays.asList(stateTree.root,
	            			  aNode)
	                        
				);
		assertEquals(expected, stateTree.getNodeAndAllDescendants(stateTree.root));

	}
	
	@Test
	public void checkGetNodeAndAllDescendantsReturnsTwoDescendantsIfTwo() {
		initializeTree();
		Node aNode = createExampleNode();
		Node aNode_2 = createExampleNode2();
		stateTree.addNode(stateTree.root, aNode);
		stateTree.addNode(stateTree.root, aNode_2);
		ArrayList<Node> expected = new ArrayList<Node>(
	            Arrays.asList(stateTree.root,
	            			  aNode,
	            			  aNode_2)
	                        
				);
		assertEquals(expected, stateTree.getNodeAndAllDescendants(stateTree.root));

	}
	
	@Test
	public void checkGetNodeAndAllDescendandsReturnsNestedDescendants() {
		initializeTree();
		Node aNode = createExampleNode();
		Node aNode_child = createExampleNode2();
		stateTree.addNode(stateTree.root, aNode);
		stateTree.addNode(aNode, aNode_child);
		ArrayList<Node> expected = new ArrayList<Node>(
	            Arrays.asList(stateTree.root,
	            			  aNode,
	            			  aNode_child)
	                        
				);
		assertEquals(expected, stateTree.getNodeAndAllDescendants(stateTree.root));
	}
	
	@Test
	public void checkGetNodeAndAllDescendantsReturnsNestedAndUnestedDescendants() {
		initializeTree();
		Node aNode = createExampleNode();
		Node aNode_2 = createExampleNode2();
		Node aNode_2_Child = createExampleNode3();
		stateTree.addNode(stateTree.root, aNode);
		stateTree.addNode(stateTree.root, aNode_2);
		stateTree.addNode(aNode_2, aNode_2_Child);
		ArrayList<Node> expected = new ArrayList<Node>(
	            Arrays.asList(stateTree.root,
	            			  aNode,
	            			  aNode_2,
	            			  aNode_2_Child)
	                        
				);
		assertEquals(expected, stateTree.getNodeAndAllDescendants(stateTree.root));
	}
	
	@Test
	public void checkForUnconditionalReturnsFalseIfTargetHasNoChildren() {
		initializeTree();
		Node unconditionalNode = createUnconditionalNode();
		stateTree.addNode(stateTree.root, unconditionalNode);
		assertFalse(stateTree.checkForUnconditional(stateTree.root, unconditionalNode, unconditionalNode));

	}
	
	@Test
	public void checkForUnconditionalReturnsFalseIfAllChildrenConditional() {
		initializeTree();
		Node aNode = createExampleNode();
		Node aNode_2 = createExampleNode2();
		stateTree.addNode(stateTree.root, aNode);
		stateTree.addNode(aNode, aNode_2);
		assertFalse(stateTree.checkForUnconditional(stateTree.root, aNode_2, aNode));

	}
	
	@Test
	public void checkForUnconditionalReturnsFalseIfAllChildrenInvisible() {
		initializeTree();
		Node aNode = createExampleNode();
		Node aNode_2 = createInvisibleNode();
		
		stateTree.addNode(stateTree.root, aNode);
		stateTree.addNode(aNode, aNode_2);
		assertFalse(stateTree.checkForUnconditional(stateTree.root, aNode_2, aNode));

	}
	
	
	
	@Test
	public void checkForUnconditionalReturnsTrueIfUnconditionalIndexGreaterThanToButLessThanFrom() {
		initializeTree();
		Node aNode = createExampleNode();
		Node aNode_2 = createExampleNode2();
		Node uncondtionalNode = createUnconditionalNode();
		stateTree.addNode(stateTree.root, aNode);
		stateTree.addNode(aNode, uncondtionalNode);
		stateTree.addNode(aNode, aNode_2);
		
		assertTrue(stateTree.checkForUnconditional(stateTree.root, aNode_2, aNode));
		
	}
	
	@Test
	public void checkForUnconditonalReturnsTrueIfUnconditionalIndexGreaterThanToAndToInvisible() {
		initializeTree();
		Node aNode = createExampleNode();
		Node invisibleNode = createInvisibleNode();
		Node uncondtionalNode = createUnconditionalNode();
		stateTree.addNode(stateTree.root, aNode);
		stateTree.addNode(aNode, invisibleNode);

		stateTree.addNode(aNode, uncondtionalNode);
		
		assertTrue(stateTree.checkForUnconditional(stateTree.root, invisibleNode, aNode));
	}
	
	@Test
	public void checkForUnconditonalReturnsFalseIfUnconditionalIndexGreaterThanToAndToVisible() {
		initializeTree();
		Node aNode = createExampleNode();
		Node aNode_2 = createExampleNode2();
		Node uncondtionalNode = createUnconditionalNode();
		stateTree.addNode(stateTree.root, aNode);
		stateTree.addNode(aNode, aNode_2);

		stateTree.addNode(aNode, uncondtionalNode);
		
		assertFalse(stateTree.checkForUnconditional(stateTree.root, aNode_2, aNode));
	}
	
	
	@Test
	public void checkRootToDestinationReturnsRouteIfNoVisibleStateFound() {
		initializeTree();
		Node from = createExampleNode();
		Node invisibleNode = createInvisibleNode();
		Node invisibleNode_2 = createInvisibleNode();
		Node to = createExampleNode();
		stateTree.addNode(stateTree.root, from);
		stateTree.addNode(stateTree.root, invisibleNode);
		stateTree.addNode(invisibleNode, invisibleNode_2);
		stateTree.addNode(invisibleNode_2, to);
		
		ArrayList<Node> expected = new ArrayList<Node>(
	            Arrays.asList(invisibleNode_2,
	            			  invisibleNode,
	            		      stateTree.root)
	                        
				);
		assertEquals(expected, stateTree.rootToDestination(from, to, to));
	}
	
	@Test
	public void checkRootToDestinationReturnsNullIfVisibleStateFound() {
		initializeTree();
		Node from = createExampleNode();
		Node aNode =  createExampleNode();
		Node invisibleNode_2 = createInvisibleNode();
		Node to = createExampleNode();
		stateTree.addNode(stateTree.root, from);
		stateTree.addNode(stateTree.root, aNode);
		stateTree.addNode(aNode, invisibleNode_2);
		stateTree.addNode(invisibleNode_2, to);
		
		
		assertNull(stateTree.rootToDestination(from, to, to));
	}
	
	@Test
	public void checkRootToDestinationReturnsNullIfUnconditionalFound() {
		initializeTree();
		Node from = createExampleNode();
		Node aNode =  createExampleNode();
		Node invisibleNode_2 = createInvisibleNode();
		Node to = createExampleNode();
		stateTree.addNode(stateTree.root, from);
		stateTree.addNode(stateTree.root, aNode);
		stateTree.addNode(aNode, invisibleNode_2);
		stateTree.addNode(invisibleNode_2, to);
		
		
		assertNull(stateTree.rootToDestination(from, to, to));
	}
	
	
	/////////////////////////////get Route//////////////////////////////////
	@Test
	public void checkGetRouteReturnsNullIfToIndexLessThanFromIndex() {
		initializeTree();
		Node from = createExampleNode();
		Node to = createExampleNode();
		stateTree.addNode(stateTree.root, to);
		stateTree.addNode(to, from);
		assertNull(stateTree.getRoute(from, to));

	}
	
	@Test
	public void checkGetRouteReturnsRouteFromParentAndChild() {
		initializeTree();
		Node from = createExampleNode();
		Node to = createExampleNode();
		stateTree.addNode(stateTree.root, from);
		stateTree.addNode(from, to);
		ArrayList<Node> expected = new ArrayList<Node>(
	            Arrays.asList(to)          
				);
		assertEquals(expected, stateTree.getRoute(from, to).route);
	}
	
	@Test
	public void checkGetRouteReturnsRouteFromParentToDescendant() {
		initializeTree();
		Node from = createExampleNode();
		Node inbetweenInvisible = createInvisibleNode();
		Node to = createExampleNode();
		stateTree.addNode(stateTree.root, from);
		stateTree.addNode(from, inbetweenInvisible);
		stateTree.addNode(inbetweenInvisible, to);
		ArrayList<Node> expected = new ArrayList<Node>(
	            Arrays.asList(inbetweenInvisible,
	            			  to)          
				);
		assertEquals(expected, stateTree.getRoute(from, to).route);
	}
	
	@Test
	public void checkGetRouteReturnsRouteBetweenTwoChildren() {
		initializeTree();
		Node from = createExampleNode();
		Node to = createExampleNode();
		stateTree.addNode(stateTree.root, from);
		stateTree.addNode(stateTree.root, to);
		ArrayList<Node> expected = new ArrayList<Node>(
	            Arrays.asList(to)          
				);
		assertEquals(expected, stateTree.getRoute(from, to).route);
	}
	
//	@Test
//	public void checkGetRouteReturnsRouteBetweenTwoSubTrees() {
//		initializeTree();
//		Node from = createExampleNode();
//		Node aNodeTree1 = createExampleNode2();
//		Node aNode2Tree1 = createExampleNode3();
//		Node to = createExampleNode();
//		Node aNodeTree2 = createExampleNode2();
//		Node aNode2Tree2 = createExampleNode3();
//		stateTree.addNode(stateTree.root, aNodeTree1);
//		stateTree.addNode(aNodeTree1, from);
//		stateTree.addNode(aNodeTree1, aNode2Tree1);
//		
//		
//		stateTree.addNode(stateTree.root, aNodeTree2);
//		stateTree.addNode(aNodeTree2, to);
//		stateTree.addNode(aNodeTree2, aNode2Tree2);
//		
//		ArrayList<Node> expected = new ArrayList<Node>(
//	            Arrays.asList(aNode2Tree1,
//	            			  aNodeTree2,
//	            			  to)          
//				);
//		System.out.println(stateTree.getRoute(from, to).route);
//		assertEquals(expected, stateTree.getRoute(from, to).route);
//
//
//	}
	
//	@Test
//	public void checkGetRouteReturnsNullIfVisibleNodeFound() {
//		initializeTree();
//		Node from = createExampleNode();
//		Node aNode = createExampleNode();
//		Node to = createExampleNode();
//		stateTree.addNode(stateTree.root, from);
//		stateTree.addNode(from, aNode);
//		stateTree.addNode(aNode, to);
//		assertNull(stateTree.getRoute(from, to).route);
//	}

	
	
	

	
}
