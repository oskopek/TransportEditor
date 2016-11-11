;; Transport ${problem.name}
;;

(define (problem ${problem.name})
(:domain transport)
(:objects
<#list locationList as location>
${location.name} - location
</#list>
<#list vehicleList as vehicle>
${vehicle.name} - vehicle
</#list>
<#list packageList as package>
${package.name} - package
</#list>
<#if domain.getPddlLabels()?seq_contains(actionCost) || domain.getPddlLabels()?seq_contains(numeric)>
    <#list 0..maxCapacity as i>
    capacity-${i} - capacity-number
    </#list>
</#if>
)
(:init
<#if domain.getPddlLabels()?seq_contains(actionCost) || domain.getPddlLabels()?seq_contains(numeric)>
(= (total-cost) 0)
</#if>
<#if domain.getPddlLabels()?seq_contains(actionCost) || domain.getPddlLabels()?seq_contains(numeric)>
    <#list 1..maxCapacity as i>
    (capacity-predecessor capacity-${i-1} capacity-${i})
    </#list>
</#if>
<#list roads as road>
    <#assign l1=road._1(), l2=road._2(), r=road._3()>
; ${l1.xCoordinate},${l1.yCoordinate} -> ${l2.xCoordinate},${l2.yCoordinate}
(road ${l1.name} ${l2.name})
(= (road-length ${l1.name} ${l2.name}) ${r.length})
<#if domain.getFunctionMap()["fuel-demand"]??>
(= (fuel-demand ${l1.name} ${l2.name}) ${r.fuelCost!0})
</#if>
</#list>
<#list packageList as package>
(at ${package.name} ${package.location.name})
    <#if domain.getPddlLabels()?seq_contains(temporal)>
    (= (package-size ${package.name}) ${package.size.cost})
</#if>
</#list>
<#if domain.getPredicateMap()["has-petrol-station"]??>
    <#list petrolLocationList as location>
    (has-petrol-station ${location.name})
    </#list>
</#if>
<#list vehicleList as vehicle>
(at ${vehicle.name} ${vehicle.location.name})
    <#if domain.getPddlLabels()?seq_contains(temporal)>
    (ready-loading ${vehicle.name})
    (= (capacity ${vehicle.name}) ${vehicle.maxCapacity})
    <#else>
    (capacity ${vehicle.name} capacity-${vehicle.maxCapacity})
</#if>
    <#if domain.getFunctionMap()["fuel-left"]??>
    (= (fuel-left ${vehicle.name}) ${vehicle.curFuelCapacity})
    (= (fuel-max ${vehicle.name}) ${vehicle.maxFuelCapacity})
    </#if>
</#list>
)
(:goal (and
<#list packageList as package>
(at ${package.name} ${package.target.name})
</#list>
))
<#if domain.getPddlLabels()?seq_contains(temporal)>
(:metric minimize (total-time))
)
<#else>
(:metric minimize (total-cost))
)
</#if>
