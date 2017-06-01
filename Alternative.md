cats.Alternative
===

A monoid on applicative functors.

**Possible intuition: "Recovery"** 

1. Single result of a type is needed
2. Multiple (alternative) computations providing result exist, 
   such that subsequent computations are evaluated 
   if preceding has failed.

**Examples:**
* Example: `Option.orElse`
* Example: parser combinators
* Example: HTTP parameters taken from body, query, header, config.
* Example: fetching?

**Laws**
 * TBD

**Notes:**

* Lazyness is crucial
* No syntax in cats
```
