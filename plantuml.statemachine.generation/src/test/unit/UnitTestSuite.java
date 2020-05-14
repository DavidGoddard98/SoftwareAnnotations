
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import plantuml.statemachine.generation.StateTextDiagramHelperTest;




@RunWith(Suite.class)
@SuiteClasses({
    	NodeTest.class,
        EventTest.class,
        PatternIdentifierTest.class,
        StateTextDiagramHelperUnitTest.class,
        StateTreeTest.class})
public class UnitTestSuite {

}
