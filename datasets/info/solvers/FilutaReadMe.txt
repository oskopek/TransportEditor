Filuta v. 0.2.7  - AI planning system with time and resources
Author: Filip Dvořák (filip.dvorak@runbox.com)
(C) Copyright 2008-2009 Filip Dvořák
********************************************************************************
* Requirements
********************************************************************************
There are almost no minimal requirements on processing power, e.g. a mobile
phone could run Filuta. The largest problem instances we have encountered 
require less than 90MB of RAM. Of course the processing power directly effects
the performance, where the amount of available cache seems to be the most
significant factor.

To run Filuta you only need Java Runtime Environment (www.java.com).

********************************************************************************
* Input
********************************************************************************
Filuta accepts planning problems defined in PDDL 2.1+, it also requires
translation of these problems into SAS+ representation, for which purpose we
use the translation module from Temporal Fast Downward that can be found at
tfd.informatik.uni-freiburg.de (see below).

********************************************************************************
* Usage
********************************************************************************
java -jar filuta.jar [-t time] -d directory

where time is the runtime limit for the planner in minutes, if no time is
specified, the planner runs once (it won't improve solutions).

directory is the path to directory, where following input files are expected:
 - domain.pddl ~ definition of the problem domain in PDDL language
 - problem.pddl ~ definition of the problem instance in PDDL language
 - output.sas ~ translation of the problem into SAS+ representation (done by TFD) 
 - variables.groups ~ state variables of the translated problem (done by TFD)
 
Example: java -jar filuta.jar -t 2 -d IPC2008/elevators/10/
 
********************************************************************************
* Output
********************************************************************************
If run without specified time, the planner produces single solution into the
given directory and names it "final_plan.txt".

If the time is specified, the planner keeps producing solutions whenever a 
better one is found. The solutions are named "planX.txt", where X corresponds to
X-th improvement of the plan starting with plan0.txt. Once the planner runs out
of specified time, it produces "final_plan.txt" corresponding to the best
solution found so far.

Notice: the planner is still in prototype, we have succesfully translated 
several domains from IPC2008 into our representation, however the translation
expects certain behavior from numeric fluents to be able to translate it.
Basicaly, numeric fluents are more general than our concept of resources and
since PDDL does not contain explicit resource representation we have to raly
only on our ability to "recognize" resources among fluents; e.g. Filuta won't
interpret correctly resources with variable capacity.

The planner also produces information about its progress to the standard output, 
each (improving) plan found is written down, e.g.:

ms: 138, ops: 69, cuts: 548, des:98, w: 916, a: 975, b: 629, tm: 0.064

where:
"ms" is the makespan of the plan
"ops" is the number of operators
"cuts" is the number of prunings of the search space
"des" is the number of encoutered dead-ends
"w", "a", "b" are the branching counters for the search procedures
"tm" is the consumed real-time in seconds

The produced plan-files contain scheduled actions sorted by their start times:
"time: (action parameters) [duration]"
e.g.:
35.061: (MOVE-UP-FAST FAST0 F4 F8) [13]
43.063: (LEAVE P6 SLOW1-0 F5) [1]
43.065: (BOARD P5 SLOW1-0 F5) [1]

This format is also directly accepted by PDDL validator VAL (see below).

********************************************************************************
* Notes on usage of translation module in Temporal Fast Downward
********************************************************************************
The translation modul is located in /TFD/translate; it can be run by 
executing "python translate.py Problem", where Problem is the name of the 
problem instance in PDDL. 

The translation produces three files into the default directory: output.sas, 
variables.groups and all.groups. For our planner we need output.sas and 
variables.groups.

Notice: to sucessfully run the translation script you need Python (or Jython)
version higher than 2.5.0 and lower than 3.0.0.

Notice: by the time you are reading this text, the translation will be probably
outdated (the Temporal Fast Downward system is being continously extended).

********************************************************************************
* VAL, The Automatic Validation Tool For PDDL
********************************************************************************
The instructions for compilation of VAL can be found at 
http://planning.cis.strath.ac.uk/VAL/. However we have encountered some problems
during compilation, hence we provide our experience with building VAL.

We have run Ubuntu 9.04 (32-bit) OS with the latest bison and flex available to
30th of June and GCC 4.4.0. Following steps needed to be done:
1) We have regenerated "pddl+.cpp" with bison.
2) We have regenerated "lex.yy.cc" with flex.
3) Now we have iteratively run "make" of VAL and searched for problems.
3a) Most problems were caused by missing includes in header files, we have
resolved them by including the libraries (usually library for strings).
4) Finally we had to turn off treatment of warnings as errors in the make file.
The warnings mostly consisted of deprecated manipulation with char pointers; 
hence we didn't spent further time on tracking and resolving all of the issues,
and we have settled with less strict compilation.