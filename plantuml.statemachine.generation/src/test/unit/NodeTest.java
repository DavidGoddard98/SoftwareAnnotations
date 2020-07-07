

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import utils.Event;
import utils.Node;

public class NodeTest {

	
	@Test
	public void checkSetNodeIndex() {
		Node node = new Node("ExampleNode", "exampleNode", true, 0, 0, 0, new Event("Event"));
		node.setIndex(5);
		assertEquals(5, node.index);
	}
	
	@Test
	public void checkSetNodeVisible() {
		Node invisibleNode = new Node("ExampleNode", "exampleNode", false, 0, 0, 0, new Event("Event"));
		invisibleNode.setVisible();
		assertTrue(invisibleNode.visible);
	}
	
	@Test
	public void checkSetParent() {
		Node node = new Node("ExampleNode", "exampleNode", true, 0, 0, 0, new Event("Event"));
		Node parent = new Node("parentNode", "parentNode", true, 0, 0, 0, new Event("Event"));
		node.setParent(parent);
		assertEquals(parent, node.parent);
	}
	
	@Test
	public void checkEqualsReturnsTrueIfSameNode() {
		Node node = new Node("ExampleNode", "exampleNode", true, 0, 0, 0, new Event("Event"));
		assertTrue(node.equals(node));
	}
	
	@Test
	public void checkEqualsReturnsFalseIfDifferentNode() {
		Node node = new Node("ExampleNode", "exampleNode", true, 0, 0, 0, new Event("Event"));
		Node parent = new Node("parentNode", "parentNode", true, 0, 0, 0, new Event("Event"));
		assertFalse(node.equals(parent));
	}
	
	
}
