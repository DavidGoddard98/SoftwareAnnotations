# UML State Machines as Annotations for State-based Software
A dissertation project contributing to my BSc degree at The University of Sheffield. It outlines the design and implementation of a plugin for the Eclipse IDE which extends a well known tool called PlantUML https://plantuml.com/, enhancing its ability to annotate state-based software.

*The plugin is writen 100% in Java*.

# Introduction

The goal behind the plugin made in this project is to simplyify the complexities developers face when attempting to understand state-based code, whether their aim is to extend it, write tests for it, or simply adapt it. Within the confinements of this project this has been accomplished in a variety of ways:

* Automatic generation of a state-machine diagram based on state-based code. 
* Linking between components in a diagram and lines of code (and vice versa) via navigation (see below).

Below, an overview of the results and uses of the plugin will be illustrated. For a substantially more detailed examination that stretches from research into exisiting tools, all the way to the design and implementation of the plugin, see the attached thesis 'Annotations_of_Software.pdf'.

# The plugin

Undeniably, the most useful feature of the plugin is the automatic generation of a state machine diagram when given state-based code. The generated diagram appears in a view within Eclipse next to the text editor for the users convenience:

<br>
<div align = "center">
  <img src="images/eclipsevie.PNG" width="500"/>
</div>
<br>


As shown, this provides the developer with an easy way to comprehend the code they are writing/reading within the same environment. 

A simple demonstration of this is shown below:



An aspect that stood out to me when reading state-based code when designing this plugin was that it can be difficult to relate components of a state-machine to the aspects of the software it illustrates. This prompted me to implement a feature that constructs links between the two via navigating the user between them in the hope that this would improve clarity. The results of this were more beneficial than anticipated. 

# Installation

PlantUML for Eclipse must be installed - http://hallvard.github.io/plantuml/

This plugin also requires GraphViz to be installed, you can install this here - https://graphviz.org/download/. You must then link the dot executable to Eclipse via the PlantUML preference page. 

Then simply install my plugin - statemachine.generation.site

To generate a diagram you must insert the state-based code between the following statements:

@start_OSM_generation
//Insert code here
@end_OSM_generation

Then, if the PlantUML view is open in Eclispe, a diagram will be automatically generated for you.



