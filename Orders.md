cats.kernel.Eq
===

A type class used to determine equality between 2 instances of the same type.
 
**Functions:**
* `def eqv(x: A, y: A): Boolean`
* `def neqv(x: A, y: A): Boolean = !eqv(x, y)`
* `on`
* `and`
* `or`

cats.kernel.Order
===

Defines a total ordering on some type `A`.
 
 An order is defined by a relation `<=` and 3 laws:

* Transitivity: _if x comes before y, and y comes before z, then x comes before z._ 
* Totality: _either x comes before y, or y comes before x._
* Asymmetry: _if x comes before y, then y doesn't come before x._


**Examples:**
* Integers
* Ordering users by the time of their registration.
* Ordering articles by their price (assuming that each article has a price defined).

cats.kernel.PartialOrder
===

Defines a partial ordering on some type `A`.

**Laws**
Transitivity, asymmetry but no totality.

**Examples:**

* Ordering users by the median price of the articles they've bought.
* Ordering articles by their price (assuming that each article has a price defined).
* A collection of people ordered by genealogical descendancy.
  Some pairs of people bear the descendant-ancestor relationship, 
  but other pairs of people are incomparable, with neither being a
  descendent of the other.

**Intuition:**
Ordering something by the property that might not be defined. 
