; Transport city-sequential-5nodes-1000size-2degree-100mindistance-2trucks-2packages-2008seed

(define (problem transport-city-sequential-5nodes-1000size-2degree-100mindistance-2trucks-2packages-2008seed)
(:domain transport)
(:objects
city-loc-1 - location
city-loc-2 - location
city-loc-3 - location
city-loc-4 - location
city-loc-5 - location
truck-1 - vehicle
truck-2 - vehicle
package-1 - package
package-2 - package
capacity-0 - capacity-number
capacity-1 - capacity-number
capacity-2 - capacity-number
capacity-3 - capacity-number
capacity-4 - capacity-number
)
(:init
(= (total-cost) 0)
(capacity-predecessor capacity-0 capacity-1)
(capacity-predecessor capacity-1 capacity-2)
(capacity-predecessor capacity-2 capacity-3)
(capacity-predecessor capacity-3 capacity-4)
; 456,221 -> 273,425
(road city-loc-1 city-loc-4)
(= (road-length city-loc-1 city-loc-4) 28)
(= (fuel-demand city-loc-1 city-loc-4) 55)
; 456,221 -> 566,552
(road city-loc-1 city-loc-5)
(= (road-length city-loc-1 city-loc-5) 35)
(= (fuel-demand city-loc-1 city-loc-5) 70)
; 742,542 -> 564,783
(road city-loc-2 city-loc-3)
(= (road-length city-loc-2 city-loc-3) 30)
(= (fuel-demand city-loc-2 city-loc-3) 60)
; 742,542 -> 566,552
(road city-loc-2 city-loc-5)
(= (road-length city-loc-2 city-loc-5) 18)
(= (fuel-demand city-loc-2 city-loc-5) 37)
; 564,783 -> 742,542
(road city-loc-3 city-loc-2)
(= (road-length city-loc-3 city-loc-2) 30)
(= (fuel-demand city-loc-3 city-loc-2) 60)
; 564,783 -> 566,552
(road city-loc-3 city-loc-5)
(= (road-length city-loc-3 city-loc-5) 24)
(= (fuel-demand city-loc-3 city-loc-5) 47)
; 273,425 -> 456,221
(road city-loc-4 city-loc-1)
(= (road-length city-loc-4 city-loc-1) 28)
(= (fuel-demand city-loc-4 city-loc-1) 55)
; 273,425 -> 566,552
(road city-loc-4 city-loc-5)
(= (road-length city-loc-4 city-loc-5) 32)
(= (fuel-demand city-loc-4 city-loc-5) 65)
; 566,552 -> 456,221
(road city-loc-5 city-loc-1)
(= (road-length city-loc-5 city-loc-1) 35)
(= (fuel-demand city-loc-5 city-loc-1) 70)
; 566,552 -> 742,542
(road city-loc-5 city-loc-2)
(= (road-length city-loc-5 city-loc-2) 18)
(= (fuel-demand city-loc-5 city-loc-2) 37)
; 566,552 -> 564,783
(road city-loc-5 city-loc-3)
(= (road-length city-loc-5 city-loc-3) 24)
(= (fuel-demand city-loc-5 city-loc-3) 47)
; 566,552 -> 273,425
(road city-loc-5 city-loc-4)
(= (road-length city-loc-5 city-loc-4) 32)
(= (fuel-demand city-loc-5 city-loc-4) 65)
(at package-1 city-loc-4)
(at package-2 city-loc-4)
(at truck-1 city-loc-4)
(capacity truck-1 capacity-2)
(= (fuel-left truck-1) 323)
(= (fuel-max truck-1) 323)
(at truck-2 city-loc-5)
(capacity truck-2 capacity-4)
(= (fuel-left truck-2) 323)
(= (fuel-max truck-2) 323)
)
(:goal (and
(at package-1 city-loc-5)
(at package-2 city-loc-2)
))
(:metric minimize (total-cost))
)
