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
