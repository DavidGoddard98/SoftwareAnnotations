//package net.sourceforge.plantuml.text;
//
//public class OSMGenerationTest {
//	////////////////////////////////////////////////////EXAMPLE
//	enum valid_states { TO_TAXI, TO_TAXI_2, TO_AIR, INIT, CLIMB, DESCEND, TO_ALCA, TURN_TO_ALCA, OVER_ALCA, TO_GOLD, LOW, CLOSING, TURN_1,FLY_1,TURN_2,FLY_2,TURN_3, FLY_3, FLY_1_IGNORE, TURN_1_IGNORE }
//	
//	 double target_dir = 0; //ignore this
//	 
//	 //targetDir is a mad one   //ignore this
//	 
//	 
//	 target_dir = 2; //ignore this
//	 
//	 
//	//look for initial decleration
//	state = valid_states.TO_TAXI;
//	
//	public void roll(int anInt, int anotherInt) { //ADD AS SELF TRANSITION 
//		 pitchDifferenceIntegral+=a.getPitchDeg()-pitchAngle;
//	}
//	
//	public void yaw(int randomInt) { //ADD AS TRANSITIUON TO DESCEND
//		state = valid_states.DESCEND;
//	}
//	
//	
//	if (alt>200) {
//		target_dir = 2;
//	} else if (alt >300) {
//		state = valid_states.OVER_ALCA;
//	} else {
//		state = valid_states.LOW;
//	}
//	
//	
//	switch(state) {
//	case CLIMB: 
//		roll(0, 80);
//		airSpeed85();
//		
//		if (alt > 100) {
//			state = valid_states.LOW;
//		}
//		break;
//	case TURN_TO_ALCA:
//		
//		holdAlt();
//	}
//
//}
//	
//
//	//MY CODE//////////////////////////////////////////////////////////////
//
//	private static Set<String> guardIdentifiers = Sets.newHashSet{"if", "else if", "else");
//	
//	}
//	
//	boolean state_initialized;
//	
////TAKE A LINE
//	//if whitespace ignore
//	//if comment ignore
//	//if field/declaration ignore
//	// if method - Scan for a state decleration
//		//NONE -> add 2 selfs trans
//		//Yes - > add to trans with target state
//	
//	//if guard, - Scan for state decleration / method?
//		//None -> ignore
//		//Yes  -> create transition
//					
//				//CREATE DUPLICATE TRAISL IF GUARD AND THEN CARRY ON AS USUAL...
//				
//	//if switch(state)
//		//create case regions (this will be all that can happen to a case
//		//if method go to method lists and add necessary transitions
//		///if guard, - Scan for state decleration
//			//None -> ignore
//			//Yes  -> create transition
//	//do nothing ? add to list of suggested?