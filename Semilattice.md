Semilattice
===

A CRDT (short for Conflict-free replicated datatype) is a data structure 
that supports an operation join :: a -> a -> a where join is associative, 
commutative and idempotent. 

These three attributes together are usually referred to as a semilattice
in literature. 

CRDTs have monotonically increasing state, where clients never observe 
state rollback. The set of states is partially ordered.
