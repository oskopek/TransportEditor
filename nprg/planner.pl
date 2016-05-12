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
    member(r(Location1, Location2, Cost), Graph),
    select(V, Vehicles, NewVehicles),
    V = v(Name, Carrying, Location1, AvailableNum),
    NewV = v(Name, Carrying, Location2, AvailableNum),
    ReturnVehicles = [NewV|NewVehicles],
    NewState = s(ReturnVehicles, Packages, Graph),
    PlanAction = drive(Name, Location1, Location2).

% delete all packages which are at their destination
% preparedPackages(+Packages, -NewPackages)
preparePackages([], []).
preparePackages([p(_, Location, Location)|Tail], NewPackages) :-
    preparePackages(Tail, NewPackages).
preparePackages([H|Tail], [H|NewPackages]) :-
    H = p(_, Location, Location2),
    Location \= Location2,
    preparePackages(Tail, NewPackages).

% prepare state for planning
% prepare(+State, -PreparedState)
prepare(s(Vehicles, Packages, Graph), PreparedState) :-
    preparePackages(Packages, NewPackages),
    floydWarshall(Graph, Graph2),
    PreparedState = s(Vehicles, NewPackages, Graph2).

% goal(+state) -- is the state a goal state?
goal(s([], [], _)) :- !.
goal(s([Vehicle|Tail], [], _)) :-
    Vehicle = v(_, [], _, _),
    goal(s(Tail, [], _)).

% undoAction(+Action, ?UndoAction)
undoAction(drive(Name, Location1, Location2), drive(Name, Location2, Location1)).
undoAction(pickup(Name, Location, PName, _, _),
             drop(Name, Location, PName, _, _)).
undoAction(drop(Name, Location, PName, _, _),
         pickup(Name, Location, PName, _, _)).

% isNotUndoAction(+Action, +Actions)
isNotUndoAction(_, []).
isNotUndoAction(Action, [H|_]) :- % compare only the first two (as this gets done each step and if an action happened in between, it may be optimal
    \+undoAction(Action, H).

% Find plans of a length Depth
% findnactions(+StateList, +Actions, -NewActions, +Depth, +Cost, -NewCost)
findnactions([State|_], Actions, RevActions, 0, Cost, Cost) :-
    goal(State), reverse(Actions, RevActions), !.
findnactions(States, Actions, NewActions, Depth, Cost, NewCost) :-
    Depth > 0,
    States = [State|_],
    action(State, CurState, PlanAction, ActionCost),
    isNotUndoAction(PlanAction, Actions), % skip undo actions, they never lead to a optimal plan
    \+member(CurState, States),
    CurCost is Cost + ActionCost,
    NewDepth is Depth - 1,
    findnactions([CurState|States], [PlanAction|Actions], NewActions, NewDepth, CurCost, NewCost).

% findactions/3 (+InitState, -Plan, -TotalCost)
findactions(InitState, Plan, TotalCost) :-
    findactions(InitState, 0, Plan, TotalCost).

% findactions/4 (+InitState, +Depth, -Plan, -TotalCost)
findactions(InitState, Depth, Plan, TotalCost) :-
    findnactions([InitState], [], Plan, Depth, 0, TotalCost).
findactions(InitState, Depth, Plan, TotalCost) :-
    NewDepth is Depth + 1,
    findactions(InitState, NewDepth, Plan, TotalCost).

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
    findactions(PreparedState, Plan, TotalCost),
    !,
    writef('Plan = %w\n', [Plan]),
    writef('TotalCost = %w\n', [TotalCost]).

% A sample problem (p01)
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

% Read problem for standard input
% stdioproblem(-InitState)
stdioproblem(InitState) :-
    read(roads(Graph)),
    read(packages(Packages)),
    read(vehicles(Vehicles)),
    InitState = s(Vehicles, Packages, Graph).

%%%%%%%%%%%%%%%%%%%%

% emptyList(+Length, +FillElement, -List)
emptyList(0, _, []) :- !.
emptyList(N, Fill, [Fill|Tail]) :-
    N1 is N - 1,
    emptyList(N1, Fill, Tail).

% setElement(+List, +N, +Elem, -NewList)
setElement(List, N, Elem, NewList) :-
    nth0(N, List, _, List2), % remove old elem
    nth0(N, NewList, Elem, List2), % insert new elem
    !.

% setElement(+Matrix, +I, +J, +Elem, -NewMatrix)
setElement(Matrix, I, J, Elem, NewMatrix) :-
    nth0(I, Matrix, Row),
    setElement(Row, J, Elem, NewRow),
    setElement(Matrix, I, NewRow, NewMatrix),
    !.

% indexOf(+Element, +List, -Index)
indexOf(Elem, List, Index) :-
    nth0(Index, List, Elem),
    !.

% fillInRoads(+Graph, +SortedNodes, +BaseMatrix, -Matrix)
fillInRoads([], _, Matrix, Matrix) :- !.
fillInRoads([Road|Graph], Nodes, Base, NewMatrix) :-
    Road = r(From, To, Dist),
    indexOf(From, Nodes, I),
    indexOf(To, Nodes, J),
    setElement(Base, I, J, Dist, Matrix),
    fillInRoads(Graph, Nodes, Matrix, NewMatrix).

% generateAdjMatrix(+Graph, +Nodes, -Matrix)
generateAdjMatrix(Graph, Nodes, Matrix) :-
    Infinity = 1000000000,
    length(Nodes, N),
    generateMatrixRows(BaseMatrix, N, N, Infinity),
    fillInRoads(Graph, Nodes, BaseMatrix, Matrix),
    !.

% generateMatrixRows(-Matrix, +Rows, +Cols, +Fill)
generateMatrixRows([], 0, _, _) :- !.
generateMatrixRows([NewRow|Matrix], N, Cols, Fill) :-
    N1 is N - 1,
    emptyList(Cols, Fill, Row),
    Diff is Cols - N,
    setElement(Row, Diff, 0, NewRow), % set dist(a, a) = 0
    generateMatrixRows(Matrix, N1, Cols, Fill).

parseNodes([], []) :- !.
parseNodes([Road|Graph], NewNodesSet) :-
    parseNodes(Graph, Nodes),
    Road = r(Node1, Node2, _),
    NewNodes = [Node1, Node2],
    union(Nodes, NewNodes, NewNodesSet).

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


translateToGraph([], _, _, Graph, Graph) :- !.
translateToGraph([Row|Tail], RowIndex, Nodes, Graph, NewGraph) :-
    translateRowToGraph(Row, RowIndex, 0, Nodes, Graph, Graph2),
    RowIndex1 is RowIndex + 1,
    translateToGraph(Tail, RowIndex1, Nodes, Graph2, NewGraph).

translateToGraph(Matrix, Nodes, Graph) :-
    translateToGraph(Matrix, 0, Nodes, [], Graph),
    !.

% Floyd-Warshall % TODO translate actions into graph
floydWarshall(Graph, NewGraph) :-
    parseNodes(Graph, NodesOld),
    sort(NodesOld, Nodes),
    generateAdjMatrix(Graph, Nodes, Matrix),
    length(Matrix, NodeCount), % the matrix is NxN where N = node count
    floydWarshallK(Matrix, NewMatrix, 0, NodeCount),
    translateToGraph(NewMatrix, Nodes, NewGraph),
    !.

floydWarshallK(Matrix, Matrix, N, N) :- !.
floydWarshallK(Matrix, NewMatrix, K, NodeCount) :-
    floydWarshallI(Matrix, Matrix2, K, 0, NodeCount),
    K1 is K + 1,
    floydWarshallK(Matrix2, NewMatrix, K1, NodeCount).

floydWarshallI(Matrix, Matrix, _, N, N) :- !.
floydWarshallI(Matrix, NewMatrix, K, I, NodeCount) :-
    floydWarshallJ(Matrix, Matrix2, K, I, 0, NodeCount),
    I1 is I + 1,
    floydWarshallI(Matrix2, NewMatrix, K, I1, NodeCount).

floydWarshallJ(Matrix, Matrix, _, _, N, N) :- !.
floydWarshallJ(Matrix, NewMatrix, K, I, J, NodeCount) :-
    floydWarshallStep(Matrix, Matrix2, K, I, J),
    J1 is J + 1,
    floydWarshallJ(Matrix2, NewMatrix, K, I, J1, NodeCount).

elementAt(Matrix, I, J, Elem) :-
    nth0(I, Matrix, Row),
    nth0(J, Row, Elem),
    !.

floydWarshallStep(Matrix, NewMatrix, K, I, J) :-
    elementAt(Matrix, I, J, DistIJ),
    elementAt(Matrix, I, K, DistIK),
    elementAt(Matrix, K, J, DistKJ),
    NewDist is DistIK + DistKJ,
    DistIJ > NewDist,
    setElement(Matrix, I, J, NewDist, NewMatrix),
    !.
floydWarshallStep(Matrix, Matrix, _, _, _).

