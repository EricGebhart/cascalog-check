# cascalog-check

This is a simple example to reproduce a problem between cascalog and
cider.  After a load or reload there is frequently a stack-trace
from a clojure query with a filter. Even when that code has not changed.
This example works much better than my larger production project, but
it does break sometimes. 

This is happening with cider 10, cider 12 and cider 13. I have no idea
what might have changed to cause this it could have been anything as I
have just returned to coding cascalog after several months.

I have updated all emacs packages and worked through numerous errors 
with squiggly clojure, eastwood etc. The last complaint I fixed was
changing (:use cascalog.api)  to [cascalog.api :refer :all].
That did seem to make it better at least in the bigger project, in
that the code did work the first time, but not after edits.

The nrepl messages look clean to me at this point.

This code seems to work just fine in the `lein repl`.  I have
not seen it break there the few times I have tried it.

### Cascalog logic ops only ?

This seems to only effect cascalog code with a filterfn and maybe others,
but there is a simple function, `filter` which uses the same filter function as cascalog, but uses clojure's filter on the results from a simpler
query which actually works.

```(filter-test foo-catv)```

Is a clojure / cascalog hybrid equivalent to the full cascalog
function call:

```(cats foo-catv)```

When `(cats foo-catv)` or `(codes 3)` is failing, `(filter-test foo-catv)` 
will continue to work using the same filter function that `cats` uses.

Well, Now I'm not so sure. I had an error in filter-test using the wrong filter function with the wrong number of args.  I repaired it, reloaded the function to no effect. Still wrong number of args.  Reload the function again, failure.  - 3 times. Finally reload entire file.  Success.
See **nrepl-messages4.txt**

Very strange. This has got to be an environment problem.

## Environment.

I started last week with cider 10. I upgraded everything to cider 12,
and today switched to cider 13 to see if it was better. The only difference
between the cider 12 and 13 environment is the cider package.

    Connected to nREPL server - nrepl://localhost:51662
    ;; CIDER 0.13.0snapshot (package: 20160612.1230), nREPL 0.2.12
    ;; Clojure 1.7.0, Java 1.8.0_73
    
    leiningen 2.6.1
    Cascalog 3.0
    emacs 24.4.1
    OS X 10.11.5
    

## Usage

Cider jack in.  I let cider do all the injections although I do
specify squiggly clojure 1.5 explicitly as it was not injected with
cider 12.  See my profiles.clj in the repo.

Load core.clj into the repl, change to that namespace and run
(codes 3)  or (cats foo-catv)

If it results in a `FlowException local step failed`,  reload
the code and try again. Repeat as necessary until it works.

To break it again, sometimes just adding a blank line and reloading will
do it.  Sometimes I add a stupid function (defn foo [x] x), or delete
the same and then reload the code in order to break it.

This simple example does not break as often as my bigger project. I do
not know why. Obviously.

## How to Reproduce

Start with a change to the name space `(C-c M-n)`, load the code `(C-c C-k)`. Change to the repl `(C-c C-z)`.  

run `(codes 3)`

See crash.  Go back to code, reload, return to repl.
`(C-c C-z) (C-c C-k) (C-c C-z)`

Run `(codes 3)`  again. 

Alternatively run `(cats foo-catv)` 

### To reintroduce the crash.

Add a function.  ```(defn s [x] x)```

Load just the function `(C-c C-c)`.

Try combinations of load file and load defun to get success again.

## What that looked like.

```
    ;; You can disable it from appearing on start by setting
    ;; `cider-repl-display-help-banner' to nil.
    ;; ======================================================================
    user> 
    cascalog-check.core> (codes 3)
    FlowException local step failed  cascading.flow.planner.FlowStepJob.blockOnJob (FlowStepJob.java:219)
    cascalog-check.core> (codes 3)
    (["foo" 1 3]
     ["foo" 2 100]
     ["bar" 1 5]
     ["bar" 2 22]
     ["baz" 1 3]
     ["baz" 2 100])
    cascalog-check.core> (codes 3)
    FlowException local step failed  cascading.flow.planner.FlowStepJob.blockOnJob (FlowStepJob.java:219)
    cascalog-check.core> (codes 3)
    FlowException local step failed  cascading.flow.planner.FlowStepJob.blockOnJob (FlowStepJob.java:219)
    cascalog-check.core> (codes 3)
    FlowException local step failed  cascading.flow.planner.FlowStepJob.blockOnJob (FlowStepJob.java:219)
    cascalog-check.core> (codes 3)
    (["foo" 1 3]

     ["foo" 2 100]
     ["bar" 1 5]
     ["bar" 2 22]
     ["baz" 1 3]
     ["baz" 2 100])
    cascalog-check.core> 
    cascalog-check.core> (test-filter foo-catv)
    CompilerException java.lang.RuntimeException: Unable to resolve symbol: test-filter in this context, compiling:(*cider-repl cascalog-check*:80:21) 
    cascalog-check.core> (filter-test foo-catv)
    ArityException Wrong number of args (1) passed to: core/catv-filter  clojure.lang.AFn.throwArity (AFn.java:429)
    cascalog-check.core> (filter-test foo-catv)
    ArityException Wrong number of args (1) passed to: core/catv-filter  clojure.lang.AFn.throwArity (AFn.java:429)
    cascalog-check.core> (filter-test foo-catv)
    ArityException Wrong number of args (1) passed to: core/catv-filter  clojure.lang.AFn.throwArity (AFn.java:429)
    cascalog-check.core> (filter-test foo-catv)
    (["foo" [3 100 8 12]])
    cascalog-check.core> 
```

## .lein/profiles.clj

See profiles.clj.

## nrepl messages.

### nrepl-messages.txt
This just from the beginning to the first success.

### nrepl-messages2.txt
This from the beginning to failure after adding a function
and loading just the new function, and then failure.

### nrepl-messages3.txt
This the complete session with a transcript and paste of
the repl session at the top.

It starts from the beginning with a failure, then success, 
success after adding a blank line and loading,
success after deleting a blank line and loading.
then failure after adding a function and loading just it, 
failures after subsequent loads and eventually success after 
loading just the function and doing a file load immediately after.

### nrepl-messages4.txt

This one starts at end of messages3.txt where I first gave the wrong
filename for filter-test.  Then gave the right name to get an arityexception.  I then fixed the function, reloadad just the function and re-ran `(filter-test foo-catv)` which is the clojure/cascalog hybrid version of `(cats foo-catv)`.  I continued to get the same arity exception until I reloaded all of the code with (C-c C-k).


Bizarre!


## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
