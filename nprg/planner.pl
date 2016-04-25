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
    PreparedState = s(Vehicles, NewPackages, Graph).

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
    % NewDepth < 25, % TODO whats with the limit?
    findactions(InitState, NewDepth, Plan, TotalCost).

% plan(-Plan, -TotalCost)
plan(Plan, TotalCost) :-
    %problem(InitState),
    stdioproblem(InitState),
    prepare(InitState, PreparedState),
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

