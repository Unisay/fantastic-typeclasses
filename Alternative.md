cats.Alternative
===

A monoid on applicative functors.

Both Option and List can represent computations with a varying number of results. 
We use `Option` to indicate a computation can fail somehow (that is, it can have 
either zero results or one result), and we use `List` for computations that can 
have many possible results (ranging from zero to arbitrarily many results). 

In both of these cases, one useful operation is amalgamating all possible results 
from multiple computations into a single computation.
 
 With lists, for instance, that would amount to concatenating lists of possible results. 
 The Alternative class captures this amalgamation in a general way.

```scala
import cats.Applicative
import cats.MonoidK

trait Alternative[F[_]] extends Applicative[F] with MonoidK[F] {
  def compose[G[_]: Applicative]: Alternative[λ[α => F[G[α]]]]
}
```
