# restless - get less from your REST!

## restless-play-json

This is an implementation of the query language from `restless-core` that works with documents in the
form of Play json objects.

**Warning - this implementation is very memory intensive!** It will potentially load your entire data set into memory, depending on how it is sourced and what filter you are applying to it.

