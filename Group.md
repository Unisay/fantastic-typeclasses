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
  * Hard to find examples besides algebraic (History?, CRDT?)
  * CommutativeGroup (Abelian) 
