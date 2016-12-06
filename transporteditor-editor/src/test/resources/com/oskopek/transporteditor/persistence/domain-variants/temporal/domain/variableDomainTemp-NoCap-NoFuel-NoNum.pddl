;; Transport
;;

(define (domain transport)
(:requirements
:typing
:durative-actions
:numeric-fluents
)
(:types
location target locatable - object
vehicle package - locatable
)

(:predicates
(road ?l1 ?l2 - location)
(at ?x - locatable ?y - location)
(in ?x - package ?y - vehicle)
(ready-loading ?v - vehicle)
)

(:functions
(road-length ?l1 ?l2 - location)
)

(:durative-action drive
:parameters (?v - vehicle ?l1 ?l2 - location)
:duration (= ?duration (road-length ?l1 ?l2))
:condition (and
(at start (at ?v ?l1))
(at start (road ?l1 ?l2))
)
:effect (and
(at start (not (at ?v ?l1)))
(at end (at ?v ?l2))
)
)

(:durative-action pick-up
:parameters (?v - vehicle ?l - location ?p - package)
:duration (= ?duration 1)
:condition (and
(at start (at ?v ?l))
(over all (at ?v ?l))
(at start (at ?p ?l))
(at start (ready-loading ?v))
)
:effect (and
(at start (not (at ?p ?l)))
(at end (in ?p ?v))
; lock vehicle
(at start (not (ready-loading ?v)))
(at end (ready-loading ?v))
)
)

(:durative-action drop
:parameters (?v - vehicle ?l - location ?p - package)
:duration (= ?duration 1)
:condition (and
(at start (at ?v ?l))
(over all (at ?v ?l))
(at start (in ?p ?v))
(at start (ready-loading ?v))
)
:effect (and
(at start (not (in ?p ?v)))
(at end (at ?p ?l))
; lock vehicle
(at start (not (ready-loading ?v)))
(at end (ready-loading ?v))
)
)

)
