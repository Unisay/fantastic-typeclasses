cats.kernel.Semilattice
===

Semilattices are useful in concurrent and distributed systems:
* CRDTs
* LVars
* Propagators

**Laws:**
* Associativity: `(x \/ that) \/ z === x \/ (that \/ z)`
* Commutativity: `x \/ that === that \/ x`
* Idempotentence: `x \/ x === x`

Associativity lets chunk and do in parallel:
 
`(a \/ b) \/ (c \/ d) \/ (e \/ f)`

Idempotence lets us use at-least-once messaging:

`a \/ a \/ a \/ a === a`

Commutativity means not caring about the ordering of events:

`a \/ b \/ c === c \/ a \/ b`



**Example: Max (Min)**
* Associative: `(x max that) max z === x max (that max z)`
* Commutative: `x max that === that max x`
* Idempotent: `x max x === x`


**CRDTs:**
* Convergent Replicated Data Types (associative, commutative, idempotent). 
* Commutative Replicated Data Types (associative, commutative) - requires at-most once delivery.


**Notes:**
* We can't use `Semilattice` with `Foldable` (only with `Reducible` aka `Foldable1`) 
  because it doesn't have an `empty`.
