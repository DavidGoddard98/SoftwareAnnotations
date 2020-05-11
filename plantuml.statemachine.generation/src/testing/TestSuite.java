package testing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import testing_unit.StateLinkOpenerTest;
import testing_unit.StateTextDiagramHelperTest;

@RunWith(Suite.class)
@SuiteClasses({
        StateLinkOpenerTest.class,
        StateTextDiagramHelperTest.class })
public class TestSuite {
	
}
