package plantuml.statemachine.generation;

import java.util.ArrayList;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;

import utils.Event;
import utils.Node;
import utils.PendingState;
import utils.StateTree;

public class StateMachineParser {
	
	
	
	
	ArrayList<String> methodCalls;
	ArrayList<String> declaredMethods;
	ArrayList<String> exitConditions;
	ArrayList<Node> exitStates;
	ArrayList<Node> conditionalBlock;
	ArrayList<String> ignoreArray;
	ArrayList<String> addedStates;
	Stack<String> currentBlock;
	Stack<Event> events;
	Stack<PendingState> pendingStates;
	int selStart;
	
	String whileStateName;
	Stack<Node> stateFound;
	Stack<String> ignoreStack;
	
	StateTree theTree;
	StateTree storeTree;
	
	boolean ignore = false;
	boolean stopIgnoring = false;
	boolean unConditionalState = false;
	boolean drawTree = false;
	boolean selfLoop = false;
	boolean afterLoopState = false;
	boolean nextLineConditionalValidate = false;
	boolean oneLineConditional = false;
	boolean certainEvent = true;
	
	



}
