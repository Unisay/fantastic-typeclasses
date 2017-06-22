## Magma

Is an algebraic structure that consists of a set, M,
equipped with a single binary operation, M × M → M. 

In scala set corresponds to the data type and its elements
correspond to the values that inhabit it.
 
^
### Laws

![cats.kernel.*](img/cats.kernel.magmas.png)

^
### Associativity   

`(x |+| y) |+| z === x |+| (y |+| z)`

Note: Order of evaluation

^
### Idempotence   

`x |+| x === x`

Note: 
This is a very useful property in many situations, 
as it means that an operation can be repeated or retried
as often as necessary without causing unintended effects. 
With non-idempotent operations, the algorithm may have to 
keep track of whether the operation was already performed or not.
Example: at-least-once delivery
 
^
### Commutativity  

`x |+| y === y |+| x`

Note: parallel execution

Putting on socks resembles a commutative operation since which sock is put on first is unimportant. Either way, the result (having both socks on), is the same. 
^
### Identity 

`x |+| empty === x`
  
`empty |+| x === x`  

Note: default

^
### Inverse

`a |+| inverse(a) === empty`  

`inverse(a) |+| a === empty`

Note: substraction, reverse.

^

Structure | Assoc | Idemp | Comm | Ident | Inverse |
--------- | ------------- | ----------- | ------------- | -------- | ------- |
Magma | | | | | 
Semigroup | ✔ | | | |   
Band | ✔ | ✔ | | |
CommutativeSemigroup | ✔ | | ✔ | |  
Semilattice | ✔ | ✔ | ✔ | | 
BoundedSemilattice | ✔ | ✔ | ✔ | ✔ | 
Monoid | ✔ | | | ✔ |
CommutativeMonoid | ✔ | | ✔ | ✔ |
Group | ✔ | | | ✔ | ✔
CommutativeGroup | ✔ | | ✔ | ✔ | ✔

^
### Example

**CRDTs**

> In distributed computing, a conflict-free replicated data type (CRDT) is a data structure which can be replicated across multiple computers in a network, where the replicas can be updated independently and concurrently without coordination between the replicas, and where it is always mathematically possible to resolve inconsistencies which might result.
  
^
**Commutative Replicated Data Types (CmRDT)**    
aka "Operation-based"

Replicas propagate state by transmitting only the update operations 
that has to commute: `f . g == g . f`

^
**Convergent Replicated Data Types (CvRDT)**    
aka "State-based"

Replicas send their full local state to other replicas, where the states are merged. 
 
^
Type | Associativity | Commutativity | Idempotency | Delivery
--------- | ------------- | ------------- | ----------- | --------
CmRDT |   | ✔ |   | at-most-once |
CvRDT | ✔ | ✔ | ✔ | at-least-once |

^
```scala
case class GCounter(state: IntMap[Int] = IntMap.empty) {
  def increment(node: Node): GCounter = {
    val newValue = state.getOrElse(node.id, 0) + 1
    copy(state = state.updated(node.id, newValue))
  }
  def value: Int = state.values.sum
}
```
^
```scala
case class PNCounter(pos: GCounter = GCounter(), 
                     neg: GCounter = GCounter()) {
  def increment(node: Node): PNCounter = 
    copy(pos = pos.increment(node))
  def decrement(node: Node): PNCounter = 
    copy(neg = neg.increment(node))
  def value: Int = 
    pos.value - neg.value
}
```
^
```scala
class Node(val id: Int) {
  var counter = PNCounter()
  def increment(): Unit =
    counter = counter increment this
  def decrement(): Unit =
    counter = counter decrement this
  def ->:(other: Node)
         (implicit S: BoundedSemilattice[PNCounter]): Unit =
    counter = S.combine(counter, other.counter)
}
```
^
```scala
implicit val gCounterBoundedSemilattice =
  new BoundedSemilattice[GCounter] {
    def empty: GCounter = GCounter()
    def combine(x: GCounter, y: GCounter): GCounter = {
      val choose = (_, v1, v2) => max(v1, v2)
      GCounter(x.state.unionWith(y.state, choose))
    }
  }
```
^
```scala
implicit val pnCounterBoundedSemilattice =
  new BoundedSemilattice[PNCounter] {
    def empty: PNCounter = PNCounter()
    def combine(x: PNCounter, y: PNCounter) = {
      val bs = BoundedSemilattice[GCounter]
      val pos = bs.combine(x.pos, y.pos)
      val neg = bs.combine(x.neg, y.neg)
      PNCounter(pos, neg)
    }
  }
```
^
```scala
val node1 = new Node(id = 1)
val node2 = new Node(id = 2)
val node3 = new Node(id = 3)

node2 increment()
node3 increment()
node3 increment()
node3 decrement()

node3 ->: node2
          node2 ->: node1
          node2 ->: node1 // demonstrating idempotency

node1.counter.value must_=== 2
```
