import java.util.ArrayList;

private void controlLoop(int patternNo, String line, int lineNum, Matcher m) {
		switch(patternNo) {
		
			case 0: //call back...
				//remove obvious once that arnt there.
				System.out.println("found a callback: " + line);
				if (line.contains("Systen.out.print")) break;
				callBack = true;
				break;
			case 1: //complex if guard
				 System.out.println("good if guard " + line + "statement: " + m.group(2)) ;				 
				 currentBlock.push("conditional");
				 events.push(m.group(2)); //the condition
			 
				
				break;
			case 2: //switch state
				currentBlock.push("switch-state");
				visibleStates.clear();
				break;
			case 3: //state change
				System.out.println("state Change: " + line + "the State: " + m.group(4));
				if (initialState == null) {
					
					initialState = m.group(4);
					Node node = new Node(initialState, null, "", true);
					theTree = new StateTree(node);
					stateFound.push(initialState);
					visibleStates.push(node);
					result.append("[*] -> " + initialState);
					result.append("\n");
		
					break;
				} 
				

				if (unConditionalState) {
					//IF THIS IS VISIBLE
					if (callBack) {
						//AS END OF OLD TREE
						//Loop through all states and add them as a parent
						for (Node invisibleNode : theTree.nodes)
							if (!invisibleNode.visible) invisibleNodes.add(invisibleNode.stateName);
						Node node = new Node(unconditionalState.name, theTree.root, "unconditional", true);

						theTree.addNode(theTree.root, node);
						result.append(buildTransitionsFromTree(theTree, false));
						//AS ROOT FOR NEW TREE
						node = new Node(unconditionalState.name, null, "", true);
						visibleStates.clear();
						stateFound.clear();
						theTree = new StateTree(node);
						visibleStates.push(node);
						Node parentNode = node;
						if (!events.empty())  stateStore = new StateStore(m.group(4), events.peek(), parentNode);
						else stateStore = new StateStore(m.group(4), "", parentNode);
						stateFound.push(m.group(4));
						unConditionalState = false;
						callBack = false;
						break;
					}
					//IF INVISIBLE
					//Add as child to all other nodes with empty transition? - not sure
					//ignoring if that uncodnital state isnt visible atm...
					stateFound.pop();
					unConditionalState = false;
				} else {
					addBackLogNode();

				}
				
				if (!visibleStates.isEmpty()) {
					 if (currentBlock.empty()) {
						unConditionalState = true;
						
						unconditionalState = new StateStore(m.group(4), "", null);
						stateFound.push(m.group(4));
	
					
					}else if (currentBlock.peek().equals("conditional")) { 
						currentBlock.pop(); //we know the conditional is for a state 
						currentBlock.push("state-conditional"); //therefore speicify this
						
						Node parentNode = theTree.getNode(stateFound.peek());
						stateStore = new StateStore(m.group(4), events.peek(), parentNode);
						stateFound.push(m.group(4));
		

					} else if (currentBlock.peek().equals("state-conditional")) {
						
						Node parentNode = theTree.getNode(stateFound.peek());
						stateStore  = new StateStore(m.group(4), "unconditional", parentNode);
							
					} else if (currentBlock.peek().equals("case-state")) {
						Node parentNode = theTree.getNode(stateFound.peek());
						stateStore = new StateStore(m.group(4), "", parentNode);
						stateFound.push(m.group(4));
						callBack = true;
					}
							
					

							
				}
				
				break;
			case 4: //closed }
				
				if (!currentBlock.isEmpty()) {
					if (currentBlock.peek().equals("conditional-state")) {
//						visibleStates = savedVisibleStates;
						currentBlock.pop();
					} else if (currentBlock.peek().equals("state-conditional")) {
						stateFound.pop();
						currentBlock.pop();
						if (!currentBlock.contains("state-conditional") && stateFound.size() > 1) {
							stateFound.pop();
						}
					} else if (currentBlock.peek().equals("switch-state")) {
						switchStateActive=false;
						currentBlock.pop();
					} else {
						currentBlock.pop();
					}
				}
				
				
				
				if (!events.isEmpty()) {
					events.pop();
					
				}
				
				
				
				break;
			case 5: //decleration
				System.out.println("decleration: " + line);
				
				break;
			case 6: //simpleMethodCal
				int index = m.group(1).indexOf("(");
				String method = m.group(1).substring(0, index);
				System.out.println(method);
				if (declaredMethods.contains(method) && !methodCalls.contains(m.group(1))){
					methodCalls.add(m.group(1));
					System.out.println("addding :" + m.group(1));
				}
				if (exitConditions!= null && exitConditions.contains(m.group(1))) {
					System.out.println("exit conditions contains methiod");
					storedEvents.add(m.group(1));
					for (Node visibleState : visibleStates) {
						result.append(visibleState + " -down-> " + "[*] : " + m.group(1));
						result.append("\n");
					}
				}
				
			
				
				System.out.println("simple method call: " + line + "method: " + m.group(1));
				break;
			case 7: //complexMethodcall
				System.out.println("complex method call: " + line + "method: " + m.group(3));
				
				break;
			case 8: //case 
				
				visibleStates.clear();
				currentBlock.push("case-state"); //we know that the case is a state...
				storedEvents.clear();
				stateFound.clear();
				stateStore = null;
				if (m.group(2).equals("INIT")) {
					initialState = m.group(2);
					
					Node node = new Node(m.group(2), null, "", true);
					theTree = new StateTree(node);
					stateFound.push(initialState);
					visibleStates.push(node);
					result.append("[*] -> " + initialState);
					result.append("\n");
					caseName = "INIT";
				}
				else {
//					if (initialState == null) initialState = m.group(4);
					Node node = new Node(m.group(2), null, "", true);
					theTree = new StateTree(node);
					stateFound.push(m.group(2));
					visibleStates.push(node);
					result.append("state " + m.group(2));
					result.append("\n");
					caseName = m.group(2);
				}
				

				System.out.println("caseState: " + line + "the stateName: " + m.group(2));
				
				
				break;
			case 9:  //break regex
				if (currentBlock.peek() == "conditional") {
					//probably dont need the .contains("case-state") above...
					ignore = true;
					ignoreStack.push("ignore");
					break;
					
				
				} else if (currentBlock.peek() == "case-state") {
					addBackLogNode();

					if (currentBlock.contains("while-loop") && !caseName.equals("INIT")) {
						boolean selfLoop = true;
						transition = new StringBuilder();

						Node node = theTree.getNode(caseName);
						System.out.println( theTree.getChildren(node).size());
						System.out.println("CURRENRTBLOCK CASE STATE POP");
						for (Node child : theTree.getChildren(node)) {
							if (child.event.equals("unconditional")) selfLoop = false;
							transition.append(negateCondition(child.event));
						}
						
						for (int j =0; j<storedEvents.size(); j++) {
							if (exitConditions.contains(storedEvents.get(j))) { //exit state no self loop
								selfLoop = false;
							}
							transition.append(storedEvents.get(j));
							if (j != storedEvents.size() -1) transition.append(" && ");
						}
						if (selfLoop) {
							if (transition.length() == 0) transition.append("No event found");
							result.append(caseName + " -> " + caseName + " : " + transition);
							result.append("\n");
							for (String methodCall : methodCalls) {
								result.append("state " + caseName + " : " + methodCall + ";");
								result.append("\n");
							}
						}
						
					}
					result.append(buildTransitionsFromTree(theTree, false));
					drawTree = false;
					methodCalls = new ArrayList<String>();
					visibleStates.clear();
					while(currentBlock.peek() != "case-state") currentBlock.pop(); 
					currentBlock.pop();
				}
				
				break;
			case 10: //whileLoop
				currentBlock.push("while-loop");
			case 11: //methodDecleration
				System.out.println("method decleration: " + line);
				String methodDec = m.group(2);
				System.out.println("adding method: " + methodDec);
				if (!declaredMethods.contains(methodDec))
					declaredMethods.add(methodDec);
				
				break;
			case 12: //method decleration exception
				System.out.println("method dec with exception: " + line );
			    methodDec = m.group(2);
				if (!declaredMethods.contains(methodDec)) {
					declaredMethods.add(methodDec);
					System.out.println("adding method: " + methodDec);
				}
				
				break;
			default: 
				System.out.println("no match found");
			}


	}
