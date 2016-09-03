;; Transport ${domain.domainType.toString()} - ${date?string("yy-MM-dd HH:mm:ss")}
;;

(define (domain transport)
(:requirements
:typing
<#if actionCost.equals(domain.domainType)>
:action-costs
<#elseif numeric.equals(domain.domainType)>
:numeric-fluents
:goal-utilities
<#elseif temporal.equals(domain.domainType)>
:durative-actions
:numeric-fluents
<#else>
</#if>
)
(:types
location target locatable - object
vehicle package - locatable
<#if actionCost.equals(domain.domainType) || numeric.equals(domain.domainType)>
capacity-number - object
</#if>
)

(:predicates
<#if domain.getPredicateList()?seq_contains(IsRoad)>
(road ?l1 ?l2 - location)
</#if>
<#if domain.getPredicateList()?seq_contains(At)>
(at ?x - locatable ?y - location)
</#if>
<#if domain.getPredicateList()?seq_contains(In)>
(in ?x - package ?y - vehicle)
</#if>
<#if domain.getPredicateList()?seq_contains(HasPetrolStation)>
(has-petrol-station ?l - location)
</#if>
<#if domain.getPredicateList()?seq_contains(HasCapacity)>
(capacity ?v - vehicle ?s1 - capacity-number)
(capacity-predecessor ?s1 ?s2 - capacity-number)
</#if>
<#if domain.getPredicateList()?seq_contains(ReadyLoading)>
(ready-loading ?v - vehicle)
</#if>
)

(:functions
<#if domain.getFunctionList()?seq_contains(Capacity)>
(capacity ?v - vehicle)
</#if>
<#if domain.getFunctionList()?seq_contains(RoadLength)>
    <#if actionCost.equals(domain.domainType)>
    (road-length ?l1 ?l2 - location) - number
    <#else>
    (road-length ?l1 ?l2 - location)
    </#if>
</#if>
<#if domain.getFunctionList()?seq_contains(FuelDemand)>
(fuel-demand ?l1 ?l2 - location)
</#if>
<#if domain.getFunctionList()?seq_contains(FuelLeft)>
(fuel-left ?v - vehicle)
</#if>
<#if domain.getFunctionList()?seq_contains(FuelMax)>
(fuel-max ?v - vehicle)
</#if>
<#if domain.getFunctionList()?seq_contains(PackageSize)>
(package-size ?p - package)
</#if>
<#if domain.getFunctionList()?seq_contains(TotalCost)>
    <#if actionCost.equals(domain.domainType)>
    (total-cost) - number
    <#else>
    (total-cost)
    </#if>
</#if>
)

<#if temporal.equals(domain.domainType)>
(:durative-action drive
<#else>
(:action drive
</#if>
:parameters (?v - vehicle ?l1 ?l2 - location)
<#if temporal.equals(domain.domainType)>
:duration (= ?duration (road-length ?l1 ?l2))
</#if>
<#if temporal.equals(domain.domainType)>
:condition (and
<#else>
:precondition (and
</#if>
<#if temporal.equals(domain.domainType)>
(at start (at ?v ?l1))
<#else>
(at ?v ?l1)
</#if>
<#if temporal.equals(domain.domainType)>
(at start (road ?l1 ?l2))
<#else>
(road ?l1 ?l2)
</#if>
<#if domain.getFunctionList()?seq_contains(FuelLeft) && domain.getFunctionList()?seq_contains(FuelDemand)>
    <#if temporal.equals(domain.domainType)>
    (at start (>= (fuel-left ?v) (fuel-demand ?l1 ?l2)))
    <#else>
    (>= (fuel-left ?v) (fuel-demand ?l1 ?l2))
    </#if>
</#if>
)
:effect (and
<#if temporal.equals(domain.domainType)>
(at start (not (at ?v ?l1)))
<#else>
(not (at ?v ?l1))
</#if>
<#if temporal.equals(domain.domainType)>
(at end (at ?v ?l2))
<#else>
(at ?v ?l2)
</#if>
<#if domain.getFunctionList()?seq_contains(FuelLeft) && domain.getFunctionList()?seq_contains(FuelDemand)>
    <#if temporal.equals(domain.domainType)>
    (at start (decrease (fuel-left ?v) (fuel-demand ?l1 ?l2)))
    <#else>
    (decrease (fuel-left ?v) (fuel-demand ?l1 ?l2))
    </#if>
</#if>
<#if domain.getFunctionList()?seq_contains(TotalCost)>
(increase (total-cost) (road-length ?l1 ?l2))
</#if>
)
)

<#if temporal.equals(domain.domainType)>
(:durative-action pick-up
<#else>
(:action pick-up
</#if>
<#if domain.getPredicateList()?seq_contains(HasCapacity)>
:parameters (?v - vehicle ?l - location ?p - package ?s1 ?s2 - capacity-number)
<#else>
:parameters (?v - vehicle ?l - location ?p - package)
</#if>
<#if temporal.equals(domain.domainType)>
:duration (= ?duration 1)
</#if>
<#if temporal.equals(domain.domainType)>
:condition (and
<#else>
:precondition (and
</#if>
<#if temporal.equals(domain.domainType)>
(at start (at ?v ?l))
(over all (at ?v ?l))
<#else>
(at ?v ?l)
</#if>
<#if temporal.equals(domain.domainType)>
(at start (at ?p ?l))
<#else>
(at ?p ?l)
</#if>
<#if domain.getPredicateList()?seq_contains(HasCapacity)>
(capacity-predecessor ?s1 ?s2)
(capacity ?v ?s2)
</#if>
<#if domain.getFunctionList()?seq_contains(Capacity)>
    <#if temporal.equals(domain.domainType)>
    (at start (>= (capacity ?v) (package-size ?p)))
    <#else>
    (>= (capacity ?v) (package-size ?p))
    </#if>
</#if>
<#if temporal.equals(domain.domainType) && domain.getPredicateList()?seq_contains(ReadyLoading)>
(at start (ready-loading ?v))
</#if>
)
:effect (and
<#if temporal.equals(domain.domainType)>
(at start (not (at ?p ?l)))
<#else>
(not (at ?p ?l))
</#if>
<#if temporal.equals(domain.domainType)>
(at end (in ?p ?v))
<#else>
(in ?p ?v)
</#if>
<#if domain.getPredicateList()?seq_contains(HasCapacity)>
(capacity ?v ?s1)
(not (capacity ?v ?s2))
</#if>
<#if domain.getFunctionList()?seq_contains(Capacity)>
    <#if temporal.equals(domain.domainType)>
    (at start (decrease (capacity ?v) (package-size ?p)))
    <#else>
    (decrease (capacity ?v) (package-size ?p))
    </#if>
</#if>
<#if domain.getFunctionList()?seq_contains(TotalCost)>
(increase (total-cost) 1)
</#if>
<#if temporal.equals(domain.domainType)>

(at start (not (ready-loading ?v)))
(at end (ready-loading ?v))
</#if>
)
)

<#if temporal.equals(domain.domainType)>
(:durative-action drop
<#else>
(:action drop
</#if>
<#if domain.getPredicateList()?seq_contains(HasCapacity)>
:parameters (?v - vehicle ?l - location ?p - package ?s1 ?s2 - capacity-number)
<#else>
:parameters (?v - vehicle ?l - location ?p - package)
</#if>
<#if temporal.equals(domain.domainType)>
:duration (= ?duration 1)
</#if>
<#if temporal.equals(domain.domainType)>
:condition (and
<#else>
:precondition (and
</#if>
<#if temporal.equals(domain.domainType)>
(at start (at ?v ?l))
(over all (at ?v ?l))
<#else>
(at ?v ?l)
</#if>
<#if temporal.equals(domain.domainType)>
(at start (in ?p ?v))
<#else>
(in ?p ?v)
</#if>
<#if domain.getPredicateList()?seq_contains(HasCapacity)>
(capacity-predecessor ?s1 ?s2)
(capacity ?v ?s1)
</#if>
<#if temporal.equals(domain.domainType) && domain.getPredicateList()?seq_contains(ReadyLoading)>
(at start (ready-loading ?v))
</#if>
)
:effect (and
<#if temporal.equals(domain.domainType)>
(at start (not (in ?p ?v)))
<#else>
(not (in ?p ?v))
</#if>
<#if temporal.equals(domain.domainType)>
(at end (at ?p ?l))
<#else>
(at ?p ?l)
</#if>
<#if domain.getPredicateList()?seq_contains(HasCapacity)>
(capacity ?v ?s2)
(not (capacity ?v ?s1))
</#if>
<#if domain.getFunctionList()?seq_contains(Capacity)>
    <#if temporal.equals(domain.domainType)>
    (at end (increase (capacity ?v) (package-size ?p)))
    <#else>
    (increase (capacity ?v) (package-size ?p))
    </#if>
</#if>
<#if domain.getFunctionList()?seq_contains(TotalCost)>
(increase (total-cost) 1)
</#if>
<#if temporal.equals(domain.domainType)>

(at start (not (ready-loading ?v)))
(at end (ready-loading ?v))
</#if>
)
)
<#if domain.getFunctionList()?seq_contains(FuelLeft) && domain.getFunctionList()?seq_contains(FuelLeft) && domain.getPredicateList()?seq_contains(HasPetrolStation)>

    <#if temporal.equals(domain.domainType)>
    (:durative-action refuel
    <#else>
    (:action refuel
    </#if>
:parameters (?v - vehicle ?l - location)
    <#if temporal.equals(domain.domainType)>
    :duration (= ?duration 10)
    </#if>
    <#if temporal.equals(domain.domainType)>
    :condition (and
    <#else>
    :precondition (and
    </#if>
    <#if temporal.equals(domain.domainType)>
    (at start (at ?v ?l))
    (over all (at ?v ?l))
    <#else>
    (at ?v ?l)
    </#if>
    <#if temporal.equals(domain.domainType)>
    (at start (has-petrol-station ?l))
    <#else>
    (has-petrol-station ?l)
    </#if>
)
:effect (and
    <#if temporal.equals(domain.domainType)>
    (at end (assign (fuel-left ?v) (fuel-max ?v)))
    <#else>
    (assign (fuel-left ?v) (fuel-max ?v))
    </#if>
    <#if domain.getFunctionList()?seq_contains(TotalCost)>
    (increase (total-cost) 10)
    </#if>
)
)
</#if>

)
