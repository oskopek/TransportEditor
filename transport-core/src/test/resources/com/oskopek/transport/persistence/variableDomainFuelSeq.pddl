;; Transport sequential
;;

(define (domain transport)
(:requirements
:typing
:action-costs
)
(:types
location target locatable - object
vehicle package - locatable
)

(:predicates
(road ?l1 ?l2 - location)
(at ?x - locatable ?y - location)
(in ?x - package ?y - vehicle)
(has-petrol-station ?l - location)
(capacity ?v - vehicle ?s1 - capacity-number)
(capacity-predecessor ?s1 ?s2 - capacity-number)
)

(:functions
(road-length ?l1 ?l2 - location) - number
(fuel-demand ?l1 ?l2 - location) - number
(fuel-left ?v - vehicle) - number
(fuel-max ?v - vehicle) - number
(total-cost) - number
)

(:action drive
:parameters (?v - vehicle ?l1 ?l2 - location)
:precondition (and
(at ?v ?l1)
(road ?l1 ?l2)
(>= (fuel-left ?v) (fuel-demand ?l1 ?l2))
)
:effect (and
(not (at ?v ?l1))
(at ?v ?l2)
(decrease (fuel-left ?v) (fuel-demand ?l1 ?l2))
(increase (total-cost) (road-length ?l1 ?l2))
)
)

(:action pick-up
:parameters (?v - vehicle ?l - location ?p - package ?s1 ?s2 - capacity-number)
:precondition (and
(at ?v ?l)
(at ?p ?l)
(capacity-predecessor ?s1 ?s2)
(capacity ?v ?s2)
)
:effect (and
(not (at ?p ?l))
(in ?p ?v)
(capacity ?v ?s1)
(not (capacity ?v ?s2))
(increase (total-cost) 1)
)
)

(:action drop
:parameters (?v - vehicle ?l - location ?p - package ?s1 ?s2 - capacity-number)
:precondition (and
(at ?v ?l)
(in ?p ?v)
(capacity-predecessor ?s1 ?s2)
(capacity ?v ?s1)
)
:effect (and
(not (in ?p ?v))
(at ?p ?l)
(capacity ?v ?s2)
(not (capacity ?v ?s1))
(increase (total-cost) 1)
)
)

(:action refuel
:parameters (?v - vehicle ?l - location)
:precondition (and
(at ?v ?l)
(has-petrol-station ?l)
)
:effect (and
(assign (fuel-left ?v) (fuel-max ?v))
(increase (total-cost) 10)
)
)

)
