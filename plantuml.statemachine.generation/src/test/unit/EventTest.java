

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import utils.Event;

public class EventTest {
	
	@Test
	public void checkSetLineEnd() {
		Event event = new Event("Event", "eventLine", 0, 0, 0);
		event.setLineEnd(50);
		assertEquals(50, event.multiLineEnd);
		
	}
}
