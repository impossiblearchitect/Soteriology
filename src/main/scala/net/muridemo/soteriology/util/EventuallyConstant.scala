package net.muridemo.soteriology.util

object EventuallyConstant:
  extension [F[X] <: Seq[X], T](c: F[T]) 
    /**
      * Gets from the transformed collection in an eventually-constant manner.
      *
      * @param i Element index
      * @return The element at the given index, or the last element if the index is greater than the length of the collection.
      */
    def ec(i: Int): T = {
        if (i >= c.length)
          // An `EventuallyConstant` collection behaves as if it were constant after its length,
          // so `apply` always returns the last element.
          c(c.length - 1)
        else c(i)
    }
