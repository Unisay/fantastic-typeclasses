cats.kernel.Group
===
Monoid where each element has an inverse

**Intuition**
* Accumulation with "undo"
* Generalized multiplication

Operations:
* inverse: `-42.inverse === 42`
* remove: `100 |-| 58 === 42`
* combineN: `100 combineN -2 === -200`

**Laws:**
```haskell
a <> invert a == mempty
invert a <> a == mempty
```
  
**Notes:**
  * Example: Real numbers R with addition
  * Example: Nonzero real numbers R \ {0} with multiplication.
  * Example: The group of permutations of 52 objects can be identified
             with shuffling a deck of cards. Every time someone shuffles 
             a pack they are performing a group operation.
  * Groups are used in physics, molecular biology, encryption.
  * CommutativeGroup (Abelian) 
