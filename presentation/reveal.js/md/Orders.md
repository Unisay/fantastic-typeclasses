## Fantastic Typeclasses
#### Part 1
Exploring a [cats.kernel.*](https://github.com/typelevel/cats/tree/master/kernel/src/main/scala/cats/kernel)

>
![cats.kernel.*](img/cats.kernel.png)

>
## Equality and orders
![cats.kernel.*](img/cats.kernel.orders.png)

>
## cats.kernel.Eq

Determines equality between  
instances of the same type

^
### Laws

* **Reflexivity**    
  `x == x`  
* **Symmetry**    
  `x == y` iff `y == x`  
* **Transitivity**  
  If `x == y` and `y == z`, then `x == z`  
* **Substitution**  
  If `x == y`, then `f(x) == f(y)` for all f.  

^
> Ultimately it's the developer who provides implementations of equality
methods and who is therefore best placed to characterize which equalities 
make sense.

>\-  [Martin Odersky](http://www.scala-lang.org/blog/2016/05/06/multiversal-equality.html)

^
### Example
```scala
sealed trait Quality
object Bad extends Quality
object Good extends Quality

case class Name(value: String)
```
^
```scala
trait Article {
  def code: Int
  def name: Name
  def quality: Quality
}

case class MadeInGermany(code: Int, name: Name) extends Article {
  def quality: Quality = Good
}

case class MadeInChina(code: Int, name: Name) extends Article {
  def quality: Quality = Bad
}
```
^
### Equalities compose:
```scala
implicit val equalityOfArticles: Eq[Article] = 
  equalityByCode or (equalityByName and equalityByQuality)
```
^
### Equalities delegate (contramap):
```scala
val equalityByCode: Eq[Article] = Eq[Int].on(_.code)

val equalityByName: Eq[Article] = Eq[Name].on(_.name)

val equalityByQuality: Eq[Article] = Eq[Quality].on(_.quality)
```
^
### Define primitive equalities:
```scala
implicit val qualitativeEquality: Eq[Quality] = 
  Eq.fromUniversalEquals

implicit val caseInsensitiveNameEquality: Eq[Name] = 
  Eq.by(_.value.toLowerCase)

// Equalities for the primitive types like Int, String 
// are already defined in cats
```
^
### Syntax
```scala
MadeInGermany(code = 1, Name("f")) === // by code 
  MadeInGermany(code = 1, Name("z"))

MadeInGermany(code = 1, Name("f")) === // by name 
  MadeInGermany(code = 2, Name("F"))

MadeInGermany(code = 1, Name("f")) =!= 
  MadeInChina(code = 2, Name("f"))
```
>
## cats.kernel.PartialOrder

Formalizes and generalizes the intuitive concept   
of an ordering, sequencing, or arrangement  
of the elements of a set.

^
### Laws

* **Reflexivity**  
  `x <= x`  
* **Antisymmetry**  
  if `x <= y` and `y <= x`, then `x === y`  
* **Transitivity**  
  If `x <= y` and `y <= z`, then `x <= z`

^
### Usages

* Graph reduction  
* Topological sorting
* Partial Order Planning Algoritms

^
### Example 

Consider the renovation of Zalando BMO building.
In this process several things have to be done:

* Remove office furniture
* Replace Windows
* Paint Walls
* Refinish Floors
* Assign Offices
* Move in office furniture

^
### Constraints
Clearly, some things had to be done before others could even
begin. 

On the other hand, several things could have been done
concurrently.

^
### The task

Return a sorted list of actions   
such that all constraints are satisfied. 

Note:
Such a scenario can be nicely modeled using   
partial orders and topological sorting.

^
```scala
sealed trait Action

object RemoveFurniture  extends Action
object PaintWalls       extends Action
object ReplaceWindows   extends Action
object RefinishFloors   extends Action
object AssignOffices    extends Action
object MoveFurniture    extends Action
```
^
```scala
implicit val actionPartialOrder: PartialOrder[Action] =
  (x: Action, y: Action) => (x, y) match {
  
    // Furniture had to be removed before anything
    case (RemoveFurniture, _) => -1.0
    case (_, RemoveFurniture) => +1.0
  
    // Painting had to be done before the floors
    case (PaintWalls, RefinishFloors) => -1.0
    case (RefinishFloors, PaintWalls) => +1.0
  
    case _ => Double.NaN
  }
```
^
```scala
@tailrec
def min(as: List[A])(implicit PO: PartialOrder[A]): List[A] = 
  if (as.size < 2) as
  else min { 
    as
    .combinations(2)                     // Iterator[List[A]]
    .map(pair => pair(0) pmin pair(1))   // Iterator[Option[A]]
    .collect(unlift(identity))           // Iterator[A]
    .toList                              // List[A]
  }
```
^
```scala
def topoSort[A](as: List[A])
               (implicit PO: PartialOrder[A]): List[A] =
  if (as.size < 2) as else {
    val minimal = min(as)
    val remainder = as diff minimal
    val tail = if (minimal.isEmpty) as 
               else topoSort(remainder)
    minimal ++ tail
  }
```
^
```scala
val actions = List( 
  RefinishFloors, PaintWalls, ReplaceWindows,
  RemoveFurniture, AssignOffices, MoveFurniture 
)

val sorted: List[Action] = topoSort(actions)

sorted.head must_=== RemoveFurniture

val paintWallsIndex = sorted.indexOf(PaintWalls)
val refinishFloorsIndex = sorted.indexOf(RefinishFloors)
paintWallsIndex must be_< refinishFloorsIndex
```
>

## cats.kernel.Order

Defines a total ordering on some type

^
### Laws

Partial Order Laws + 

**Totality:** either `x <= y` or `y <= x`

^
### Example

```scala
val elements = List((0, 1), (2, 2), (1, 0), (2, 1), (0, 0))
val expected = List((0, 0), (1, 0), (0, 1), (2, 1), (2, 2))
```
^
### Orders delegate (contramap)
```scala
val orderByFirst: Order[(Int, Int)] = Order.by(_._1)
val orderBySecond: Order[(Int, Int)] = Order.by(_._2)
```
^
### Orders compose (statically)
```scala
implicit val orderSemigroup: Semigroup[Order[(Int, Int)]] = 
  Order.whenEqualMonoid

implicit val ordering = 
  (orderBySecond |+| orderByFirst).toOrdering

elements.sorted must_=== expected
```
^
### Orders compose (dynamically)
```scala
val orders = List(orderBySecond, orderByFirst)

implicit val orderTuples: Ordering[(Int, Int)] =
    orders.combineAll(Order.whenEqualMonoid).toOrdering

elements.sorted must_=== expected
```
