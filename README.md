# UML State Machines as Annotations for State-based Software
A dissertation project contributing to my BSc degree at The University of Sheffield. It outlines the design and implementation of a plugin for the Eclipse IDE which extends a well known tool called PlantUML https://plantuml.com/, enhancing its ability to annotate state-based software.

*The plugin is writen 100% in Java*.

# Introduction

The goal behind the plugin made in this project is to simplyify the complexities developers face when attempting to understand state-based code, whether their aim is to extend it, write tests for it, or simply adapt it. Within the confinements of this project this has been accomplished in a variety of ways:

* Automatic generation of a state-machine diagram based on state-based code. 
* Linking between components in a diagram and lines of code (and vice versa) via navigation (see below).

Below, an overview of the results and uses of the plugin will be illustrated. For a substantially more detailed examination that stretches from research into exisiting tools, all the way to the design and implementation of the plugin, see the attached thesis 'Annotations_of_Software.pdf'.

# The plugin

## <div align="center"> Automatic Generation of a Diagram </div>

Undeniably, the most useful feature of the plugin is the automatic generation of a state machine diagram when given state-based code. The generated diagram appears in a view within Eclipse next to the text editor for the users convenience:

<br>
<div align = "center">
  <img src="images/eclipsevie.PNG" width="750"/>
</div>
<br>

As shown, this provides the developer with an easy way to comprehend the code they are writing/reading within the same environment.

As can be seen in the figure above, state declerations must be prefaced with 'valid_states'. *It should be noted that this is the only requirement the user must satisfy within their code to enable the diagrammatic generation to occur.* 

### Functionality 

The plugin provides the functionality of inferring the following programming practices:

* Conditionals (guards)
* Actions
* Switch statements
* While loops

Suffice to say that a combination of these working in conjunction can also be inferred. 

### Conditionals

As shown previously, simple if statements can be detected, yet in addition to this, if/else and if/else if/ else can also be implied:

#### If/Else

<br>
<div align = "center">
  <img src="images/ifelse.PNG" width="500"/>
</div>
<br>

#### If/Else if*/Else

<br>
<div align = "center">
  <img src="images/ifelseifelse.PNG" width="600"/>
</div>
<br>

I'm sure you're thinking - what if some of this hypothetical systems behaviour is guarded behind a plethora of nested conditionals? Utilizing a tree structure and a fairly complex algorithm for traversal, the plugin is able to autonomously generate a diagram which accurately depicts the logic:

<br>
<div align = "center">
  <img src="images/compelx.PNG" width="850"/>
</div>
<br>

### Actions 

As well as inferring guards the plugin can also detect actions, which taken in the context of state machines, essentially means method calls that affect an objects behaviour. For instance, if the system encapsulated an autopilot for a plane and the method was called to alter the rudder roatation, it would be known as an action. 

To detect an action, the method decleration simply needs to be inserted within the auto-generation statements (@start_OSM_generation). Then, whenever the method is called within the flow of the system it will be inferred appropriately:

<br>
<div align = "center">
  <img src="images/action.PNG" width="500"/>
</div>
<br>

### Switch statements

As these are commonplace within state-based systems it made sense to provide the functionality to infer them. In order to do so (and to ensure consistency) you must use the variable 'state' as the variable you switch like so:

<br>
<div align = "center">
  <img src="images/switch.PNG" width="500"/>
</div>
<br>

### While loops

Again, loops are an integral component of state-based systems as behaviour is often constantly monitored:

<br>
<div align = "center">
  <img src="images/while.PNG" width="600"/>
</div>
<br>

*Note: There are no requirements for this peiece of functionality.*

### Removing undesired inference

Like with any software there is bound to be bugs. Although rare, this is particually true with this plugin because various assumptions must be made. To overcome this, there are built-in commands to remove unwanted inference. 

To remove a state entirely (along with all transitions flowing in/out):
  //FSM: REMOVE - stateName

To remove a single transition:
  //FSM: REMOVE - state -> anotherState : thelabel

## <div align="center"> Linking between diagram and code </div>

An aspect that stood out to me when reading state-based code when designing this plugin was that it can be difficult to relate components of a state-machine to the aspects of the software it illustrates. This prompted me to implement a feature that constructs links between the two via navigating the user between them in the hope that this would improve clarity. The results of this were more beneficial than anticipated. 

# Installation

PlantUML for Eclipse must be installed - http://hallvard.github.io/plantuml/

This plugin also requires GraphViz to be installed, you can install this here - https://graphviz.org/download/. You must then link the dot executable to Eclipse via the PlantUML preference page. 

Then simply install my plugin - statemachine.generation.site

To generate a diagram you must insert the state-based code between the following statements:

@start_OSM_generation
//Insert code here
@end_OSM_generation

*OSM in this context stands for Object state machine*

Then, if the PlantUML view is open in Eclispe, a diagram will be automatically generated for you.



