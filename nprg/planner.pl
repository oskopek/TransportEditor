% Simple forward BFS planner for IPC 08 Transport (STRIPS)

% PDDL actions (planner output):
 % (drive ?v-vehicle ?l1-location ?l2-location)
 % (pick-up ?v-vehicle ?l-location ?p-package ?s1-capacity-predecessor ?s2-capacity-predecessor)
 % (drop ?v-vehicle ?l-location ?p-package ?s1-capacity-predecessor ?s2-capacity-predecessor)

% Prolog data structures:
 % s(Vehicles, Packages, Graph) ... state
 % v(name, carrying_packages, location, availableSpace) ... vehicle
 % p(name, location, destination) ... package
 % r(location1, location2, cost) ... road

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Action predicates
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Backtrackable predicate for selecting the next action to be put into the plan.
% action(+State, -NewState, -PlanAction, -Cost)

% force-drop all packages which are at their destination
action(s(Vehicles, Packages, Graph), NewState, PlanAction, Cost) :-
    Cost = 1,
    select(V, Vehicles, NewVehicles),
    V = v(Name, Carrying, Location, AvailableNum),
    select(p(PName, _, Location), Carrying, NewCarrying),
    NewAvailableNum is AvailableNum + 1,
    NewV = v(Name, NewCarrying, Location, NewAvailableNum),
    ReturnVehicles = [NewV|NewVehicles],
    NewState = s(ReturnVehicles, Packages, Graph),
    PlanAction = drop(Name, Location, PName, AvailableNum, NewAvailableNum).

% drop (only packages where dest != loc ... due to ordering of clauses)
action(s(Vehicles, Packages, Graph), NewState, PlanAction, Cost) :-
    Cost = 1,
    select(V, Vehicles, NewVehicles),
    V = v(Name, Carrying, Location, AvailableNum),
    select(P, Carrying, NewCarrying),
    P = p(PName, _, PDestination),
    NewP = p(PName, Location, PDestination),
    NewPackages = [NewP|Packages],
    NewAvailableNum is AvailableNum + 1,
    NewV = v(Name, NewCarrying, Location, NewAvailableNum),
    ReturnVehicles = [NewV|NewVehicles],
    NewState = s(ReturnVehicles, NewPackages, Graph),
    PlanAction = drop(Name, Location, PName, AvailableNum, NewAvailableNum).

% pick-up
action(s(Vehicles, Packages, Graph), NewState, PlanAction, Cost) :-
    Cost = 1,
    select(V, Vehicles, NewVehicles),
    V = v(Name, Carrying, Location, AvailableNum),
    AvailableNum > 0,
    NewAvailableNum is AvailableNum - 1,
    select(P, Packages, NewPackages),
    P = p(PName, Location, PDestination),
    NewP = p(PName, in_transport, PDestination),
    NewCarrying = [NewP|Carrying],
    NewV = v(Name, NewCarrying, Location, NewAvailableNum),
    ReturnVehicles = [NewV|NewVehicles],
    NewState = s(ReturnVehicles, NewPackages, Graph),
    PlanAction = pickup(Name, Location, PName, NewAvailableNum, AvailableNum).

% drive
action(s(Vehicles, Packages, Graph), NewState, PlanAction, Cost) :-
    select(V, Vehicles, NewVehicles),
    V = v(Name, Carrying, Location1, AvailableNum),
    selectShortestRoad(Location1, Graph, Road), % needed to preserve optimality of the first found plan
    Road = r(Location1, Location2, Cost),
    NewV = v(Name, Carrying, Location2, AvailableNum),
    ReturnVehicles = [NewV|NewVehicles],
    NewState = s(ReturnVehicles, Packages, Graph),
    PlanAction = drive(Name, Location1, Location2).

% Select the nearest location. Backtrackable over the list of all possible target locations.
% selectNearestLocation(+SortedDifList, -Cost, -Location)
selectNearestLocation([Cost-To|_], Cost, To).
selectNearestLocation([_|Tail], Cost, To) :-
    selectNearestLocation(Tail, Cost, To).

% selectShortestRoad(+From, +Graph, -Road)
selectShortestRoad(From, Graph, Road) :-
    findall(TmpCost-Location2, member(r(From, Location2, TmpCost), Graph), DifList), % build a dif list
    keysort(DifList, SortedDifList),
    selectNearestLocation(SortedDifList, Cost, To),
    Road = r(From, To, Cost).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Planning predicates
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Deletes all packages which are at their destination already.
% preparedPackages(+Packages, -NewPackages)
preparePackages([], []).
preparePackages([p(_, Location, Location)|Tail], NewPackages) :-
    preparePackages(Tail, NewPackages).
preparePackages([H|Tail], [H|NewPackages]) :-
    H = p(_, Location, Location2),
    Location \= Location2,
    preparePackages(Tail, NewPackages).

% Prepare and simplify the initial state.
% prepare(+State, -PreparedState)
prepare(s(Vehicles, Packages, Graph), PreparedState) :-
    preparePackages(Packages, NewPackages),
    floydWarshall(Graph, Graph2),
    PreparedState = s(Vehicles, NewPackages, Graph2).

% Is the state a goal state?
% goal(+state)
goal(s([], [], _)) :- !.
goal(s([Vehicle|Tail], [], _)) :-
    Vehicle = v(_, [], _, _),
    goal(s(Tail, [], _)).

% Is the new action an undo action w.r.t. the previous action?
% undoAction(+Action, ?UndoAction)
undoAction(drive(Name, Location1, Location2), drive(Name, Location2, Location1)).
undoAction(pickup(Name, Location, PName, _, _),
             drop(Name, Location, PName, _, _)).
undoAction(drop(Name, Location, PName, _, _),
         pickup(Name, Location, PName, _, _)).

% Returns true if the last action in Actions is *not* an undo action w.r.t. Action.
% isNotUndoAction(+Action, +Actions)
isNotUndoAction(_, []).
isNotUndoAction(Action, [H|_]) :-
    % compare only the first two
    % this gets done each step and if an action happened in between, it may be correct in the optimal plan
    \+undoAction(Action, H).

% Find the optimal plan of a given length (counted in the number of actions).
% If no such plan exists, is unsatisfiable.
% findNActions(+StateList, +Actions, -NewActions, +Depth, +Cost, -NewCost)
findNActions([State|_], Actions, RevActions, 0, Cost, Cost) :-
    goal(State), reverse(Actions, RevActions), !.
findNActions(States, Actions, NewActions, Depth, Cost, NewCost) :-
    Depth > 0,
    States = [State|_],
    action(State, CurState, PlanAction, ActionCost),
    isNotUndoAction(PlanAction, Actions), % skip undo actions, they never lead to a optimal plan (all costs are > 0)
    \+member(CurState, States),
    CurCost is Cost + ActionCost,
    NewDepth is Depth - 1,
    findNActions([CurState|States], [PlanAction|Actions], NewActions, NewDepth, CurCost, NewCost).

% Find the optimal plan along with it's cost from the given initial state.
% findActions/3 (+InitState, -Plan, -TotalCost)
findActions(InitState, Plan, TotalCost) :-
    findActions(InitState, 0, Plan, TotalCost).

% Find the optimal plan along with it's cost from the given initial state and minimum depth.
% findActions/4 (+InitState, +Depth, -Plan, -TotalCost)
findActions(InitState, Depth, Plan, TotalCost) :-
    findNActions([InitState], [], Plan, Depth, 0, TotalCost).
findActions(InitState, Depth, Plan, TotalCost) :-
    NewDepth is Depth + 1,
    write(increasingDepth),
    write(NewDepth),
    nl,
    findActions(InitState, NewDepth, Plan, TotalCost).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Main predicates
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Read the input, find the optimal plan and return it.
% plan(-Plan, -TotalCost)
plan(Plan, TotalCost) :-
    write(reading),
    nl,
    %problem(InitState),
    stdioproblem(InitState),
    write(preparing),
    nl,
    prepare(InitState, PreparedState),
    write(planning),
    nl,
    findActions(PreparedState, Plan, TotalCost),
    !,
    writef('Plan = %w\n', [Plan]),
    writef('TotalCost = %w\n', [TotalCost]).

% A sample problem (p01) in prolog terms.
% problem(-InitState)
problem(InitState) :-
    Packages = [p(package1, cityloc3, cityloc2), p(package2, cityloc3, cityloc2)],
    T1 = v(truck1, [], cityloc3, 4),
    T2 = v(truck2, [], cityloc1, 3),
    Vehicles = [T1, T2],
    Graph = [r(cityloc3, cityloc1, 22),
             r(cityloc1, cityloc3, 22),
             r(cityloc3, cityloc2, 50),
             r(cityloc2, cityloc3, 50)
             ],
    InitState = s(Vehicles, Packages, Graph).

% Read the problem for standard input.
% stdioproblem(-InitState)
stdioproblem(InitState) :-
    read(roads(Graph)),
    read(packages(Packages)),
    read(vehicles(Vehicles)),
    InitState = s(Vehicles, Packages, Graph).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Floyd-Warshall predicates
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Fill initialized matrix with values from the edges of the graph.
% fillInRoads(+Graph, +SortedNodes, +BaseMatrix, -Matrix)
fillInRoads([], _, Matrix, Matrix) :- !.
fillInRoads([Road|Graph], Nodes, Base, NewMatrix) :-
    Road = r(From, To, Dist),
    indexOf(From, Nodes, I),
    indexOf(To, Nodes, J),
    setElement(Base, I, J, Dist, Matrix),
    fillInRoads(Graph, Nodes, Matrix, NewMatrix).

% Generate the adjacency matrix for a graph, to be used in the Floyd-Warshall algorithm.
% generateAdjMatrix(+Graph, +Nodes, -Matrix)
generateAdjMatrix(Graph, Nodes, Matrix) :-
    Infinity = 1000000000,
    length(Nodes, N),
    generateMatrixRows(BaseMatrix, N, N, Infinity),
    fillInRoads(Graph, Nodes, BaseMatrix, Matrix),
    !.

% Generate a matrix of size Rows x Cols filled with the given element and zero-s on the diagonal.
% generateMatrixRows(-Matrix, +Rows, +Cols, +Fill)
generateMatrixRows([], 0, _, _) :- !.
generateMatrixRows([NewRow|Matrix], N, Cols, Fill) :-
    N1 is N - 1,
    emptyList(Cols, Fill, Row),
    Diff is Cols - N,
    setElement(Row, Diff, 0, NewRow), % set dist(a, a) = 0
    generateMatrixRows(Matrix, N1, Cols, Fill).

% Parses all edges and returns a set of unique nodes, unsorted.
% parseNodes(+Graph, -NodesSet)
parseNodes([], []) :- !.
parseNodes([Road|Graph], NewNodesSet) :-
    parseNodes(Graph, Nodes),
    Road = r(Node1, Node2, _),
    NewNodes = [Node1, Node2],
    union(Nodes, NewNodes, NewNodesSet).

% Translates a row of the distance matrix back into roads (r(_, _, _) terms).
% Does not add loop-roads (a->a).
% translateRowToGraph(+Row, +RowIndex, +ColIndex, +Nodes, +Graph, -NewGraph)
translateRowToGraph([], _, _, _, Graph, Graph) :- !.
translateRowToGraph([Dist|Tail], RowIndex, ColIndex, Nodes, Graph, NewGraph) :-
    nth0(RowIndex, Nodes, From),
    nth0(ColIndex, Nodes, To),
    From \= To, % skip road a -> a
    Graph2 = [r(From, To, Dist)|Graph],
    ColIndex1 is ColIndex + 1,
    translateRowToGraph(Tail, RowIndex, ColIndex1, Nodes, Graph2, NewGraph).
translateRowToGraph([_|Tail], RowIndex, ColIndex, Nodes, Graph, NewGraph) :-
    ColIndex1 is ColIndex + 1,
    translateRowToGraph(Tail, RowIndex, ColIndex1, Nodes, Graph, NewGraph).

% Translates a distance matrix back into roads.
% translateToGraph/5(+Matrix, +RowIndex, +Nodes, +Graph, -NewGraph)
translateToGraph([], _, _, Graph, Graph) :- !.
translateToGraph([Row|Tail], RowIndex, Nodes, Graph, NewGraph) :-
    translateRowToGraph(Row, RowIndex, 0, Nodes, Graph, Graph2),
    RowIndex1 is RowIndex + 1,
    translateToGraph(Tail, RowIndex1, Nodes, Graph2, NewGraph).

% Translates a distance matrix back into roads.
% Calls translateToGraph/5 with initial parameters.
% translateToGraph/3(+Matrix, +Nodes, -Graph)
translateToGraph(Matrix, Nodes, Graph) :-
    translateToGraph(Matrix, 0, Nodes, [], Graph),
    !.

% Floyd-Warshall's algorithm.
% Calculates shortest paths from all to all nodes and translates the matrix back into a graph.
% Needed to preserve optimality of the first-found plan.
% Implemented according to https://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm#Algorithm
% floydWarshall(+Graph, -NewGraph)
floydWarshall(Graph, NewGraph) :-
    parseNodes(Graph, NodesOld),
    sort(NodesOld, Nodes),
    generateAdjMatrix(Graph, Nodes, Matrix),
    length(Matrix, NodeCount), % the matrix is NxN where N = node count
    floydWarshallK(Matrix, NewMatrix, 0, NodeCount),
    translateToGraph(NewMatrix, Nodes, NewGraph),
    !.

% The first loop of Floyd-Warshall.
% floydWarshallK(+Matrix, -NewMatrix, +K, +NodeCount)
floydWarshallK(Matrix, Matrix, N, N) :- !.
floydWarshallK(Matrix, NewMatrix, K, NodeCount) :-
    floydWarshallI(Matrix, Matrix2, K, 0, NodeCount),
    K1 is K + 1,
    floydWarshallK(Matrix2, NewMatrix, K1, NodeCount).

% The second loop of Floyd-Warshall.
% floydWarshallI(+Matrix, -NewMatrix, +K, +I, +NodeCount)
floydWarshallI(Matrix, Matrix, _, N, N) :- !.
floydWarshallI(Matrix, NewMatrix, K, I, NodeCount) :-
    floydWarshallJ(Matrix, Matrix2, K, I, 0, NodeCount),
    I1 is I + 1,
    floydWarshallI(Matrix2, NewMatrix, K, I1, NodeCount).

% The third loop of Floyd-Warshall.
% floydWarshallJ(+Matrix, -NewMatrix, +K, +I, +J, +NodeCount)
floydWarshallJ(Matrix, Matrix, _, _, N, N) :- !.
floydWarshallJ(Matrix, NewMatrix, K, I, J, NodeCount) :-
    floydWarshallStep(Matrix, Matrix2, K, I, J),
    J1 is J + 1,
    floydWarshallJ(Matrix2, NewMatrix, K, I, J1, NodeCount).

% Step of Floyd-Warshall. If the distance is shorter via K, overwrite.
% floydWarshallStep(+Matrix, -NewMatrix, +K, +I, +J)
floydWarshallStep(Matrix, NewMatrix, K, I, J) :-
    elementAt(Matrix, I, J, DistIJ),
    elementAt(Matrix, I, K, DistIK),
    elementAt(Matrix, K, J, DistKJ),
    NewDist is DistIK + DistKJ,
    DistIJ > NewDist,
    setElement(Matrix, I, J, NewDist, NewMatrix),
    !.
floydWarshallStep(Matrix, Matrix, _, _, _).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Utility predicates
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Creates an empty list of a given length filled with the given element.
% emptyList(+Length, +FillElement, -List)
emptyList(0, _, []) :- !.
emptyList(N, Fill, [Fill|Tail]) :-
    N1 is N - 1,
    emptyList(N1, Fill, Tail).

% Sets the Nth element the list to the given value.
% setElement(+List, +N, +Elem, -NewList)
setElement(List, N, Elem, NewList) :-
    nth0(N, List, _, List2), % remove old elem
    nth0(N, NewList, Elem, List2), % insert new elem
    !.

% Sets the element at [i, j] in the matrix to the given value.
% setElement(+Matrix, +I, +J, +Elem, -NewMatrix)
setElement(Matrix, I, J, Elem, NewMatrix) :-
    nth0(I, Matrix, Row),
    setElement(Row, J, Elem, NewRow),
    setElement(Matrix, I, NewRow, NewMatrix),
    !.

% Get the index of an element in a list.
% indexOf(+Element, +List, -Index)
indexOf(Elem, List, Index) :-
    nth0(Index, List, Elem),
    !.

% Get the [I, J] element of a matrix.
% elementAt(+Matrix, +I, +J, -Elem)
elementAt(Matrix, I, J, Elem) :-
    nth0(I, Matrix, Row),
    nth0(J, Row, Elem),
    !.

