import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import linkOpener.StateLinkOpener;
import plantuml.statemachine.generation.StateLinkOpenerTest;

import unit.UnitTestSuite;
import end_to_end.plantuml.statemachine.generation.End2EndTestSuite;
@RunWith(Suite.class)
@SuiteClasses({
		EndToEndTestSuite.class,
        UnitTestSuite.class })
public class TestAll {

}