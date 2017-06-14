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

Note: parallel fold

^
### Idempotence   

`x |+| x === x`

Note: at-least-once delivery
 
^
### Commutativity  

`x |+| y === y |+| x`

Note: parallel execution

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
