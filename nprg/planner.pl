% Simple forward DFS planner for IPC 08 Transport

% PDDL actions (planner output):
% (drive ?v-vehicle ?l1-location ?l2-location)
% (pick-up ?v-vehicle ?l-location ?p-package ?s1-capacity-predecessor ?s2-capacity-predecessor)
% (drop ?v-vehicle ?l-location ?p-package ?s1-capacity-predecessor ?s2-capacity-predecessor)
% ; cost = 54 (general cost)

% Prolog data structures:
% s(Vehicles, Packages) ... state
% v(name, carrying_packages, location, destinations, capacity) ... vehicle
% p(name, location, destination) ... package
% r(location1, location2, cost) ... road



% action() % drive
action(s(Vehicles, Packages), NewState, PlanAction, Cost) :-
    r(Location1, Location2, Cost),
    select(V, Vehicles, NewVehicles),
    V = v(Name, Carrying, Location, [Location2|NewDestinations], Cap),
    NewV = v(Name, Carrying, Location2, NewDestinations, Cap),
    append(NewVehicles, NewV, ReturnVehicles),
    NewState = s(ReturnVehicles, Packages),
    PlanAction = drive(Name, Location1, Location2). % TODO is?



% action() % pick-up
action(s(Vehicles, Packages), NewState, PlanAction, Cost) :-
    Cost = 1,



% action() % drop
action(s(Vehicles, Packages), NewState, PlanAction, Cost) :-
    Cost = 1,
    select(V, Vehicles, NewVehicles),
    V = v(Name, Carrying, Location, Dest, Capacity),

    NewState = s(ReturnVehicles, Packages),
    PlanAction = drop(Name, Location, PName, Capacity, NewCapacity).


% goal(+state) -- is the state a goal state?
goal(s(Vehicles, Packages)) :-
    Packages = [],
    Vehicles = [H|T],
    H = v(_, []),
    goal(s(T, Packages)).


% plan([action])

