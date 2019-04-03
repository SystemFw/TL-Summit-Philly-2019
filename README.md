# Ref + Deferred: from counters to concurrent FSMs

Slides for my talk at the Typelevel Summit 2019, in Philadelphia. You can look at them online at https://systemfw.github.io/TL-Summit-Philly-2019/#/ (use the spacebar to advance). The video for the talk is coming soon. Have a look at `Examples.scala` for the code.

## Description


fs2 offers a very powerful and composable set of concurrent combinators and data structures, which are all built out of two deceptively simple primitives: Ref and Deferred.

This talk will explain what they are, the design principles behind them, and how to use them to build your own business logic abstractions. In the process, we will discover a general pattern in the form of concurrent state machines, and see how it integrates with final tagless on one hand, and streaming control flow on the other.
