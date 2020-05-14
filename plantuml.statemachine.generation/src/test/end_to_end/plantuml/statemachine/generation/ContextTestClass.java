
public class ExampleClass {
	//@start_OSM_generation
	state = valid_states.ExampleState;
	c.call();
	
	if (anEvent) {
		state = valid_states.AnotherExampleState;
		c.call();
	}
	
	state = valid_states.AThirdState;
	c.call();
	//@end_OSM_generation
	
	////////////ONE STATE////////////////////////
	
	//@start_OSM_generation
	public void call() {
	  }
	state = valid_states.ExampleState;
	c.call();
	
	//@end_OSM_generation
	
	//@start_OSM_generation
	state = valid_states.ExampleState;
	
	//@end_OSM_generation
	
	//@start_OSM_generation
	public void call() {
	  }
	public void additionalAction() {
	  }
	state = valid_states.ExampleState;
	c.call();
	additionalAction();
	//@end_OSM_generation
	
	/////////////TWO_STATES////////////////////////
	
	//@start_OSM_generation
	public void call() {
	  }
	state = valid_states.ExampleState;
	c.call();
	state = valid_states.AnotherState;
	c.call();
	
	//@end_OSM_generation
	
	//@start_OSM_generation
	public void call() {
	  }
	state = valid_states.ExampleState;
	c.call();
	state = valid_states.AnotherState;
	
	//@end_OSM_generation
	
	//@start_OSM_generation
	public void call() {
	  }
	state = valid_states.ExampleState;
	state = valid_states.AnotherState;
	c.call();

	//@end_OSM_generation
	
	//@start_OSM_generation
	state = valid_states.ExampleState;
	state = valid_states.AnotherState;

	//@end_OSM_generation
	
	
	////////////TWO_STATES WITH GUARD////////////
	
	
	//@start_OSM_generation
	public void call() {
	  }
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();
	}
	
	//@end_OSM_generation
	
	//@start_OSM_generation
	public void call() {
	  }
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
	}
	
	//@end_OSM_generation
	
	//@start_OSM_generation
	public void call() {
	  }
	state = valid_states.ExampleState;
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();
	}
	
	//@end_OSM_generation
	
	//@start_OSM_generation
	state = valid_states.ExampleState;
	if (ExampleGuard) {
		state = valid_states.AnotherState;
	}
	
	//@end_OSM_generation
	
	
	//@start_OSM_generation
	public void call() {
	  }
	public void additionalAction() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();

	}
	additionalAction();
	//@end_OSM_generation
	
	//@start_OSM_generation
	public void call() {
	  }
	public void additionalAction() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;

	}
	additionalAction();
	//@end_OSM_generation

	
	//////////////THREE STATES/////////////
	//@start_OSM_generation
	public void call() {
	  }
	state = valid_states.ExampleState;
	c.call();
	state = valid_states.AnotherState;
	c.call();
	state = valid_states.AThirdState;
	c.call();

	//@end_OSM_generation
	
	//@start_OSM_generation
	public void call() {
	  }
	state = valid_states.ExampleState;
	c.call();
	state = valid_states.AnotherState;
	state = valid_states.AThirdState;

	//@end_OSM_generation
	
	//@start_OSM_generation
	public void call() {
	  }
	state = valid_states.ExampleState;
	state = valid_states.AnotherState;
	c.call();
	state = valid_states.AThirdState;

	//@end_OSM_generation
	
	//@start_OSM_generation
	public void call() {
	  }
	state = valid_states.ExampleState;
	state = valid_states.AnotherState;
	state = valid_states.AThirdState;
	c.call();

	//@end_OSM_generation
	
	/////////THREE STATES WITH GUARD
	//@start_OSM_generation
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();
		
	}
	state = valid_states.AThirdstate;
	c.call();


	//@end_OSM_generation
	
	//@start_OSM_generation
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		
	}
	state = valid_states.AThirdstate;
	c.call();


	//@end_OSM_generation
	
	//@start_OSM_generation
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();
	}
	state = valid_states.AThirdstate;


	//@end_OSM_generation
	
	//@start_OSM_generation
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
	}
	state = valid_states.AThirdstate;


	//@end_OSM_generation
	
	//THREE STATES W/ TWO UNDER SAME GUARD
	//@start_OSM_generation
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();
		state = valid_states.AThirdstate;
		c.call();
	}


	//@end_OSM_generation
	
	//@start_OSM_generation 
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		state = valid_states.AThirdstate;
		c.call();
	}
	

	//@end_OSM_generation
	
	//@start_OSM_generation
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();
		state = valid_states.AThirdstate;
	}


	//@end_OSM_generation

	///////THREE STATES TWO SEPERATE STATES//////////
	//@start_OSM_generation 
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();
	}
	
	if (AnotherExampleGuard) {
		state = valid_states.AThirdstate;
		c.call();
	}

	//@end_OSM_generation
	
	//@start_OSM_generation 
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
	}
	
	if (AnotherExampleGuard) {
		state = valid_states.AThirdstate;
		c.call();
	}

	//@end_OSM_generation
	//@start_OSM_generation 
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();
	}
	
	if (AnotherExampleGuard) {
		state = valid_states.AThirdstate;
	}

	//@end_OSM_generation
	//@start_OSM_generation 
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	if (ExampleGuard) {
		state = valid_states.AnotherState;
	}
	
	if (AnotherExampleGuard) {
		state = valid_states.AThirdstate;
	}

	//@end_OSM_generation
	
	////////////THREE STATES W/ NESTED GUARD
	//@start_OSM_generation 
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();

		if (AnotherExampleGuard) {
			state = valid_states.AThirdstate;
			c.call();
		}
	}
	
	//@end_OSM_generation
	
	//@start_OSM_generation 
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;

		if (AnotherExampleGuard) {
			state = valid_states.AThirdstate;
			c.call();
		}
	}
	
	//@end_OSM_generation
	
	//@start_OSM_generation 
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();

		if (AnotherExampleGuard) {
			state = valid_states.AThirdstate;
		}
	}
	
	//@end_OSM_generation
	
	//@start_OSM_generation 
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	if (ExampleGuard) {
		state = valid_states.AnotherState;

		if (AnotherExampleGuard) {
			state = valid_states.AThirdstate;
		}
	}
	
	//@end_OSM_generation
	
	///////////////NESTED LOOP W/ADDITIONAL ACTION
	//@start_OSM_generation 
	public void call() {
	  }
	public void additionalAction() {
	}
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();
		if (AnotherExampleGuard) {
			state = valid_states.AThirdstate;
			c.call();
		}
		additionalAction();
	}
	
	//@end_OSM_generation
	
	//@start_OSM_generation 
	public void call() {
	  }
	public void additionalAction() {
	}
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		if (AnotherExampleGuard) {
			state = valid_states.AThirdstate;
			c.call();
		}
		additionalAction();
	}
	
	//@end_OSM_generation
	
	//@start_OSM_generation 
	public void call() {
	  }
	public void additionalAction() {
	}
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();
		if (AnotherExampleGuard) {
			state = valid_states.AThirdstate;
		}
		additionalAction();
	}
	
	//@end_OSM_generation
	
	//@start_OSM_generation 
	public void call() {
	  }
	public void additionalAction() {
	}
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		if (AnotherExampleGuard) {
			state = valid_states.AThirdstate;
		}
		additionalAction();
	}
	
	//@end_OSM_generation
	
	
	////////////////REMOVE/////////////////
	//@start_OSM_generation
	//FSM: REMOVE - IfState
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.IfState;
		c.call();
	
	} 
		
	//@end_OSM_generation
	
	//@start_OSM_generation
	//FSM: REMOVE - ExampleState -> AnotherState : [ExampleGuard ] / call();
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();
	
	} 
	if (AnotherExampleGuard) {
		state = valid_states.AThirdState;
		c.call();
	
	} 
		
	//@end_OSM_generation
	
	///////////////////EXIT COMMAND/////////////////////////
	
	//@start_OSM_generation
	//FSM: EXIT - a.pause(true)
	public void call() {
	  }
	
	state = valid_states.ExampleState;
	c.call();
	if (ExampleGuard) {
		state = valid_states.AnotherState;
		c.call();
		a.pause(true);
	
	}
	state = valid_states.Unconditional;
	c.call();
	if (AnotherExampleGuard) {
		state = valid_states.AThirdState;
		c.call();
	
	} 
		
	//@end_OSM_generation
	
	//////////////////COMBATIBILITY WITH PLANTUML
}
