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
* "is an ancestor of" on the set of all humans that have ever lived. 
  My grandfather is an ancestor of my father, and my father is an ancestor of me, 
  so my grandfather is an ancestor of me (by transitivity). 
  Also, because my father is an ancestor of me, 
  I'm not an ancestor of my father (by asymmetry). 
  There might be people who are not ancestors of each other at all - 
  such as my father and my mother.

**Intuition:**
Ordering something by the property that might not be defined. 
