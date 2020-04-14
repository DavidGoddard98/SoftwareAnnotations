import java.util.regex.Matcher;

import net.sourceforge.plantuml.text.PatternIdentifier.RegexInfo;

private void identifyPattern(String line, int lineNum) {
		System.out.println();
		System.out.println();
		for (int k = 0; k<currentBlock.size(); k++) { 
			System.out.println(currentBlock.get(k));
		}
		
		for (RegexInfo info : patternIdentifier.patternStore) {
			Matcher m = info.pattern.matcher(line);
			
			if (m.matches()) {
				
				switch(info.identifier) {
				
 
				case 1: //complex if guard
					 System.out.println("good if guard " + line + "statement: " + m.group(2)) ;
					
					 currentBlock.push("conditional");
					 if (currentState != null) {
						 String aPotentialTransition = m.group(2);
						 potentialTransition.push(aPotentialTransition);
					 }
					
					break;
				case 2: //switch state
					currentBlock.push("switch-state");
					System.out.println("switch state " + line);
					
					break;
				case 3: //state change
					System.out.println("state Change: " + line + "the State: " + m.group(4));
					
					if (currentState == null && currentState != "[*]") { //initialize first state
						currentState = m.group(4);
						result.append("state " + currentState);
						result.append("\n");

					} else {//state change, make a trnasition from  currentState -> m.group(3);
						//do some stuff
						//need to figure out what the transition was to lead here.
						String transition = "";
						if (potentialTransition.size() > 0 ) {
							String theTransition = potentialTransition.pop();
							transition = currentState + " -> " + m.group(4) + " : " + theTransition;
						} else  {//no label for transition
							transition = currentState + " -> " + m.group(4) + " : NO TRANSITION GIVEN";
						}
						if (!addedTransitions.contains(transition)) {
							result.append(transition);
							result.append("\n");
							addedTransitions.add(transition);
						}
						
						
						
						if (!currentBlock.contains("case-state")) 
							currentState = m.group(4);
						if (currentState != "[*]") {
							result.append("state " + currentState);
							result.append("\n");
						}
						

					}
					break;
				case 4: //closed }
					System.out.println("found a closed } popping transition off");
					if (potentialTransition.size() > 0) {
						potentialTransition.pop();
					}
					if (currentBlock.size() > 0) {
						currentBlock.pop();
						System.out.println("found a }, popping a block");
					} else System.out.println("currentblock empty therefore ignoring }");
					break;
				case 5: //decleration
					System.out.println("decleration: " + line);
					
					break;
				case 6: //simpleMethodCal
					System.out.println("simple method call: " + line + "method: " + m.group(1));
					
					break;
				case 7: //complexMethodcall
					System.out.println("complex method call: " + line + "method: " + m.group(3));
					
					break;
				case 8: //case state
					System.out.println("caseState: " + line + "the stateName: " + m.group(2));
					if (currentBlock.contains("switch-state")) { //means we are in a state switch set 
						currentState = m.group(2);
						
						if (currentState.equals("INIT"))
							currentState = "[*]";
						else {
							result.append("state " + currentState);
							result.append("\n");
						}
						stateLog.add(currentStates.add(currentState));
						currentBlock.push("case-state");
						
						
					}
					
					
					break;
				case 9:  //break regex
					System.out.println("found a break statement: " + line);
					String saveBlock = currentBlock.pop();
					if (saveBlock != "case-state") {
						currentBlock.push(saveBlock);
					}
					break;
				case 11: //methodDecleration
					System.out.println("method decleration: " + line);
					
					break;
				case 12: //method decleration exception
					System.out.println("method dec with exception: " + line);
					
					break;
				default: 
					System.out.println("no match found");
				}
			} else if (m.find() && info.identifier == 10) { //fsm comment
				System.out.println("fsm comment " + line);
			}
		}