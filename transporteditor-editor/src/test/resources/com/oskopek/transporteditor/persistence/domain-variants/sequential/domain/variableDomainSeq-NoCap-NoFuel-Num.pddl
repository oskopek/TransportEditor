;; Transport sequential
;;

(define (domain transport)
(:requirements
:typing
:numeric-fluents
:goal-utilities
)
(:types
location target locatable - object
vehicle package - locatable
)

(:predicates
(road ?l1 ?l2 - location)
(at ?x - locatable ?y - location)
(in ?x - package ?y - vehicle)
)

(:functions
(road-length ?l1 ?l2 - location)
(total-cost)
)

(:action drive
:parameters (?v - vehicle ?l1 ?l2 - location)
:precondition (and
(at ?v ?l1)
(road ?l1 ?l2)
)
:effect (and
(not (at ?v ?l1))
(at ?v ?l2)
(increase (total-cost) (road-length ?l1 ?l2))
)
)

(:action pick-up
:parameters (?v - vehicle ?l - location ?p - package)
:precondition (and
(at ?v ?l)
(at ?p ?l)
)
:effect (and
(not (at ?p ?l))
(in ?p ?v)
(increase (total-cost) 1)
)
)

(:action drop
:parameters (?v - vehicle ?l - location ?p - package)
:precondition (and
(at ?v ?l)
(in ?p ?v)
)
:effect (and
(not (in ?p ?v))
(at ?p ?l)
(increase (total-cost) 1)
)
)

)
