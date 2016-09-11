;; Transport ${domain.name}
;;

(define (domain transport)
(:requirements
:typing
<#if domain.getDomainLabels()?seq_contains(actionCost)>
:action-costs
<#elseif domain.getDomainLabels()?seq_contains(numeric)>
:numeric-fluents
:goal-utilities
<#elseif domain.getDomainLabels()?seq_contains(temporal)>
:durative-actions
:numeric-fluents
<#else>
</#if>
)
(:types
location target locatable - object
vehicle package - locatable
<#if domain.getDomainLabels()?seq_contains(actionCost) || domain.getDomainLabels()?seq_contains(numeric)>
capacity-number - object
</#if>
)

(:predicates
<#if domain.getPredicateMap()["road"]??>
(road ?l1 ?l2 - location)
</#if>
<#if domain.getPredicateMap()["at"]??>
(at ?x - locatable ?y - location)
</#if>
<#if domain.getPredicateMap()["in"]??>
(in ?x - package ?y - vehicle)
</#if>
<#if domain.getPredicateMap()["has-petrol-station"]??>
(has-petrol-station ?l - location)
</#if>
<#if domain.getPredicateMap()["capacity"]??>
(capacity ?v - vehicle ?s1 - capacity-number)
(capacity-predecessor ?s1 ?s2 - capacity-number)
</#if>
<#if domain.getPredicateMap()["ready-loading"]??>
(ready-loading ?v - vehicle)
</#if>
)

(:functions
<#if domain.getFunctionMap()["capacity"]??>
(capacity ?v - vehicle)
</#if>
<#if domain.getFunctionMap()["road-length"]??>
    <#if domain.getDomainLabels()?seq_contains(actionCost)>
    (road-length ?l1 ?l2 - location) - number
    <#else>
    (road-length ?l1 ?l2 - location)
    </#if>
</#if>
<#if domain.getFunctionMap()["fuel-demand"]??>
(fuel-demand ?l1 ?l2 - location)
</#if>
<#if domain.getFunctionMap()["fuel-left"]??>
(fuel-left ?v - vehicle)
</#if>
<#if domain.getFunctionMap()["fuel-max"]??>
(fuel-max ?v - vehicle)
</#if>
<#if domain.getFunctionMap()["package-size"]??>
(package-size ?p - package)
</#if>
<#if domain.getFunctionMap()["total-cost"]??>
    <#if domain.getDomainLabels()?seq_contains(actionCost)>
    (total-cost) - number
    <#else>
    (total-cost)
    </#if>
</#if>
)

<#if domain.getDomainLabels()?seq_contains(temporal)>
(:durative-action drive
<#else>
(:action drive
</#if>
:parameters (?v - vehicle ?l1 ?l2 - location)
<#if domain.getDomainLabels()?seq_contains(temporal)>
:duration (= ?duration (road-length ?l1 ?l2))
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
:condition (and
<#else>
:precondition (and
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
(at start (at ?v ?l1))
<#else>
(at ?v ?l1)
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
(at start (road ?l1 ?l2))
<#else>
(road ?l1 ?l2)
</#if>
<#if domain.getFunctionMap()["fuel-left"]?? && domain.getFunctionMap()["fuel-demand"]??>
    <#if domain.getDomainLabels()?seq_contains(temporal)>
    (at start (>= (fuel-left ?v) (fuel-demand ?l1 ?l2)))
    <#else>
    (>= (fuel-left ?v) (fuel-demand ?l1 ?l2))
    </#if>
</#if>
)
:effect (and
<#if domain.getDomainLabels()?seq_contains(temporal)>
(at start (not (at ?v ?l1)))
<#else>
(not (at ?v ?l1))
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
(at end (at ?v ?l2))
<#else>
(at ?v ?l2)
</#if>
<#if domain.getFunctionMap()["fuel-left"]?? && domain.getFunctionMap()["fuel-demand"]??>
    <#if domain.getDomainLabels()?seq_contains(temporal)>
    (at start (decrease (fuel-left ?v) (fuel-demand ?l1 ?l2)))
    <#else>
    (decrease (fuel-left ?v) (fuel-demand ?l1 ?l2))
    </#if>
</#if>
<#if domain.getFunctionMap()["total-cost"]??>
(increase (total-cost) (road-length ?l1 ?l2))
</#if>
)
)

<#if domain.getDomainLabels()?seq_contains(temporal)>
(:durative-action pick-up
<#else>
(:action pick-up
</#if>
<#if domain.getPredicateMap()["capacity"]??>
:parameters (?v - vehicle ?l - location ?p - package ?s1 ?s2 - capacity-number)
<#else>
:parameters (?v - vehicle ?l - location ?p - package)
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
:duration (= ?duration 1)
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
:condition (and
<#else>
:precondition (and
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
(at start (at ?v ?l))
(over all (at ?v ?l))
<#else>
(at ?v ?l)
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
(at start (at ?p ?l))
<#else>
(at ?p ?l)
</#if>
<#if domain.getPredicateMap()["capacity"]??>
(capacity-predecessor ?s1 ?s2)
(capacity ?v ?s2)
</#if>
<#if domain.getFunctionMap()["capacity"]??>
    <#if domain.getDomainLabels()?seq_contains(temporal)>
    (at start (>= (capacity ?v) (package-size ?p)))
    <#else>
    (>= (capacity ?v) (package-size ?p))
    </#if>
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal) && domain.getPredicateMap()["ready-loading"]??>
(at start (ready-loading ?v))
</#if>
)
:effect (and
<#if domain.getDomainLabels()?seq_contains(temporal)>
(at start (not (at ?p ?l)))
<#else>
(not (at ?p ?l))
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
(at end (in ?p ?v))
<#else>
(in ?p ?v)
</#if>
<#if domain.getPredicateMap()["capacity"]??>
(capacity ?v ?s1)
(not (capacity ?v ?s2))
</#if>
<#if domain.getFunctionMap()["capacity"]??>
    <#if domain.getDomainLabels()?seq_contains(temporal)>
    (at start (decrease (capacity ?v) (package-size ?p)))
    <#else>
    (decrease (capacity ?v) (package-size ?p))
    </#if>
</#if>
<#if domain.getFunctionMap()["total-cost"]??>
(increase (total-cost) 1)
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>

(at start (not (ready-loading ?v)))
(at end (ready-loading ?v))
</#if>
)
)

<#if domain.getDomainLabels()?seq_contains(temporal)>
(:durative-action drop
<#else>
(:action drop
</#if>
<#if domain.getPredicateMap()["capacity"]??>
:parameters (?v - vehicle ?l - location ?p - package ?s1 ?s2 - capacity-number)
<#else>
:parameters (?v - vehicle ?l - location ?p - package)
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
:duration (= ?duration 1)
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
:condition (and
<#else>
:precondition (and
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
(at start (at ?v ?l))
(over all (at ?v ?l))
<#else>
(at ?v ?l)
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
(at start (in ?p ?v))
<#else>
(in ?p ?v)
</#if>
<#if domain.getPredicateMap()["capacity"]??>
(capacity-predecessor ?s1 ?s2)
(capacity ?v ?s1)
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal) && domain.getPredicateMap()["ready-loading"]??>
(at start (ready-loading ?v))
</#if>
)
:effect (and
<#if domain.getDomainLabels()?seq_contains(temporal)>
(at start (not (in ?p ?v)))
<#else>
(not (in ?p ?v))
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>
(at end (at ?p ?l))
<#else>
(at ?p ?l)
</#if>
<#if domain.getPredicateMap()["capacity"]??>
(capacity ?v ?s2)
(not (capacity ?v ?s1))
</#if>
<#if domain.getFunctionMap()["capacity"]??>
    <#if domain.getDomainLabels()?seq_contains(temporal)>
    (at end (increase (capacity ?v) (package-size ?p)))
    <#else>
    (increase (capacity ?v) (package-size ?p))
    </#if>
</#if>
<#if domain.getFunctionMap()["total-cost"]??>
(increase (total-cost) 1)
</#if>
<#if domain.getDomainLabels()?seq_contains(temporal)>

(at start (not (ready-loading ?v)))
(at end (ready-loading ?v))
</#if>
)
)
<#if domain.getFunctionMap()["fuel-left"]?? && domain.getFunctionMap()["fuel-max"]?? && domain.getPredicateMap()["has-petrol-station"]??>

    <#if domain.getDomainLabels()?seq_contains(temporal)>
    (:durative-action refuel
    <#else>
    (:action refuel
    </#if>
:parameters (?v - vehicle ?l - location)
    <#if domain.getDomainLabels()?seq_contains(temporal)>
    :duration (= ?duration 10)
    </#if>
    <#if domain.getDomainLabels()?seq_contains(temporal)>
    :condition (and
    <#else>
    :precondition (and
    </#if>
    <#if domain.getDomainLabels()?seq_contains(temporal)>
    (at start (at ?v ?l))
    (over all (at ?v ?l))
    <#else>
    (at ?v ?l)
    </#if>
    <#if domain.getDomainLabels()?seq_contains(temporal)>
    (at start (has-petrol-station ?l))
    <#else>
    (has-petrol-station ?l)
    </#if>
)
:effect (and
    <#if domain.getDomainLabels()?seq_contains(temporal)>
    (at end (assign (fuel-left ?v) (fuel-max ?v)))
    <#else>
    (assign (fuel-left ?v) (fuel-max ?v))
    </#if>
    <#if domain.getFunctionMap()["total-cost"]??>
    (increase (total-cost) 10)
    </#if>
)
)
</#if>

)
