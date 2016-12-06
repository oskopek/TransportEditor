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
)
(:init
(= (total-cost) 0)
; 456,221 -> 273,425
(road city-loc-1 city-loc-4)
(= (road-length city-loc-1 city-loc-4) 28)
; 456,221 -> 566,552
(road city-loc-1 city-loc-5)
(= (road-length city-loc-1 city-loc-5) 35)
; 742,542 -> 564,783
(road city-loc-2 city-loc-3)
(= (road-length city-loc-2 city-loc-3) 30)
; 742,542 -> 566,552
(road city-loc-2 city-loc-5)
(= (road-length city-loc-2 city-loc-5) 18)
; 564,783 -> 742,542
(road city-loc-3 city-loc-2)
(= (road-length city-loc-3 city-loc-2) 30)
; 564,783 -> 566,552
(road city-loc-3 city-loc-5)
(= (road-length city-loc-3 city-loc-5) 24)
; 273,425 -> 456,221
(road city-loc-4 city-loc-1)
(= (road-length city-loc-4 city-loc-1) 28)
; 273,425 -> 566,552
(road city-loc-4 city-loc-5)
(= (road-length city-loc-4 city-loc-5) 32)
; 566,552 -> 456,221
(road city-loc-5 city-loc-1)
(= (road-length city-loc-5 city-loc-1) 35)
; 566,552 -> 742,542
(road city-loc-5 city-loc-2)
(= (road-length city-loc-5 city-loc-2) 18)
; 566,552 -> 564,783
(road city-loc-5 city-loc-3)
(= (road-length city-loc-5 city-loc-3) 24)
; 566,552 -> 273,425
(road city-loc-5 city-loc-4)
(= (road-length city-loc-5 city-loc-4) 32)
(at package-1 city-loc-4)
(at package-2 city-loc-4)
(at truck-1 city-loc-4)
(at truck-2 city-loc-5)
)
(:goal (and
(preference delivery-1 (at package-1 city-loc-5))
(preference delivery-2 (at package-2 city-loc-2))
))
(:metric maximize
(- 260
(+ (total-cost)
(* (is-violated delivery-1) 115)
(* (is-violated delivery-2) 145)
)
)
)
)
