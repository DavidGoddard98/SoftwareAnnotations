
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

import plantuml.statemachine.generation.PatternIdentifier;
import plantuml.statemachine.generation.PatternIdentifier.RegexInfo;


public class PatternIdentifierTest {
	
	@Test
	public void checkAddAppendsNewPatternToPatternStore() {
		Pattern samplePattern = Pattern.compile("Apattern");
		PatternIdentifier patternIdentifier = new PatternIdentifier();
		patternIdentifier.add(samplePattern, 15);
		boolean bool = false;
		for (RegexInfo regex : patternIdentifier.patternStore) {
			if (regex.pattern.equals(samplePattern)) bool = true;
		}
		assertTrue(bool);
	}
	
	@Test
	public void checkAddDoesntAddIfIdentifierInUse() {
		Pattern samplePattern = Pattern.compile("Apattern");
		PatternIdentifier patternIdentifier = new PatternIdentifier();
		patternIdentifier.add(samplePattern, 14);
		
		assertEquals(15, patternIdentifier.patternStore.size());
	}
//	
	@Test
	public void checkConstructorInitializesAllPatterns() {
		PatternIdentifier patternIdentifier = new PatternIdentifier();
		assertEquals(15, patternIdentifier.patternStore.size());

	}
	

}
