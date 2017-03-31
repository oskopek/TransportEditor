; Transport p01-10-city-5nodes-1000size-3degree-100mindistance-2trucks-2packagespercity-2008seed

(define (problem transport-p01-10-city-5nodes-1000size-3degree-100mindistance-2trucks-2packagespercity-2008seed)
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
; 890,543 -> 748,385
(road city-loc-1 city-loc-3)
(= (road-length city-loc-1 city-loc-3) 22)
; 890,543 -> 912,799
(road city-loc-1 city-loc-4)
(= (road-length city-loc-1 city-loc-4) 26)
; 890,543 -> 977,899
(road city-loc-1 city-loc-5)
(= (road-length city-loc-1 city-loc-5) 37)
; 384,50 -> 748,385
(road city-loc-2 city-loc-3)
(= (road-length city-loc-2 city-loc-3) 50)
; 748,385 -> 890,543
(road city-loc-3 city-loc-1)
(= (road-length city-loc-3 city-loc-1) 22)
; 748,385 -> 384,50
(road city-loc-3 city-loc-2)
(= (road-length city-loc-3 city-loc-2) 50)
; 748,385 -> 912,799
(road city-loc-3 city-loc-4)
(= (road-length city-loc-3 city-loc-4) 45)
; 912,799 -> 890,543
(road city-loc-4 city-loc-1)
(= (road-length city-loc-4 city-loc-1) 26)
; 912,799 -> 748,385
(road city-loc-4 city-loc-3)
(= (road-length city-loc-4 city-loc-3) 45)
; 912,799 -> 977,899
(road city-loc-4 city-loc-5)
(= (road-length city-loc-4 city-loc-5) 12)
; 977,899 -> 890,543
(road city-loc-5 city-loc-1)
(= (road-length city-loc-5 city-loc-1) 37)
; 977,899 -> 912,799
(road city-loc-5 city-loc-4)
(= (road-length city-loc-5 city-loc-4) 12)
(at package-1 city-loc-3)
(= (package-size package-1) 23)
(at package-2 city-loc-4)
(= (package-size package-2) 55)
(at truck-1 city-loc-3)
(ready-loading truck-1)
(at truck-2 city-loc-4)
(ready-loading truck-2)
)
(:goal (and
(at package-1 city-loc-2)
(at package-2 city-loc-3)
))
(:metric minimize (total-time))
)
