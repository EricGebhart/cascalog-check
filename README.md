# cascalog-check

This is a simple example to reproduce a problem between cascalog and
cider.  After a load or reload there is frequently a stack-trace
from a clojure query with a filter. Even when that code has not changed.
This example works much better than my larger production project, but
it does break sometimes. 

It doesn't have to be cascalog, I've seen the same thing with an arity exception on a call.
fix the code, reload the function, the error persists. Change the call in 'filter test` to
use the wrong fiter function and you can see it in action.
But it's super easy to reproduce with cascalog. There is no need to create an error 
in the code to see it.  I just run `(codes 3)`.

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

### kibit, eastwood and core.typed

I have tried versions recommended by squiggly and also the newest versions.  
Eastwood 2.1/2.3 and core-typed 3.7/3.23. kibit is unchanged.

The message *"user-level profile defined in project files"*  makes no sense to me as there are clearly not any other than the
one in my .lein/profiles.clj

```
└─(1:18:43:%)── lein typed check                                                                                            ──#(Mon,Jun20)─┘
(WARNING: user-level profile defined in project files.)
Initializing core.typed ...
Building core.typed base environments ...
Finished building base environments
"Elapsed time: 21094.552088 msecs"
core.typed initialized.
Start collecting cascalog-check.core
Finished collecting cascalog-check.core
Collected 1 namespaces in 7741.133651 msecs
Not checking cascalog-check.core (does not depend on clojure.core.typed)
Checked 1 namespaces  in 7765.093461 msecs
:ok

└─(18:44:%)── lein kibit                                                                                                    
(WARNING: user-level profile defined in project files.)

└─(18:45:%)── lein eastwood                                                                                                 
(WARNING: user-level profile defined in project files.)
== Eastwood 0.2.3 Clojure 1.8.0 JVM 1.8.0_91
Directories scanned for source files:
  dev env/dev/clj src test
== Linting cascalog-check.core ==
== Linting cascalog-check.core-test ==
== Warnings: 0 (not including reflection warnings)  Exceptions thrown: 0
```

Manually running the commands which precede the failure from nrepl messages shows no errors.

```
    cascalog-check.core> (do (require 'squiggly-clojure.core) (squiggly-clojure.core/check-kb 'cascalog-check.core "/Users/eric/Projects/cascalog-check/src/cascalog_check/core.clj"))
    []
    cascalog-check.core> (do (require 'squiggly-clojure.core) (squiggly-clojure.core/check-tc 'cascalog-check.core))
    []
    cascalog-check.core> (do (require 'squiggly-clojure.core) (squiggly-clojure.core/check-ew 'cascalog-check.core))
    ()
    []```

### Additional behavior.

I have established through lots of iteration that adding a new function `(defn a [x] x)` and evaluating that new code causes the unrelated
code to fail. Loading the function, and reloading the entire file does not always work.  What does always work is
the file reload after the file has been saved. But not the save following the cider prompt which asks to save the file before reloading.
It takes one more reload after that to get the code working again.  

Doing everything at once can streamline the path to success.  Load the new function `(C-c C-c)`, Load the file with a save `C-c C-k`, 
Load the file again with no save `C-c C-k`.  Avoiding the cider save prompt makes it faster.  Add a function, save the file, reload the file with `C-c C-k`.    

Loading just the function will allow the function to work, but other code will stacktrace until the file is reloaded without a 
cider prompt to save.

### Who's is it?

I'm not sure. But it's pointing down the squiggly clojure trail.  Taking squiggly clojure out will cause
everything to work just fine.  Although what does that have to do with saving the file ?  That seems to point back to cider.

Commenting out this code in my emacs setup causes everything to work just fine, except I lose squiggly lines.

    (eval-after-load 'flycheck '(flycheck-clojure-setup))
    (add-hook 'after-init-hook #'global-flycheck-mode)
    
    (eval-after-load 'flycheck
       '(setq flycheck-display-errors-function #'flycheck-pos-tip-error-me

### Cascalog logic ops only? um No.

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

There is a minimalist **.emacs** file in the repository. It automatically installs
these packages: cider, flycheck-clojure, clojure-mode, and flycheck-pos-tip.
The problem replicates with that simple emacs setup.

    Connected to nREPL server - nrepl://localhost:51662
    ;; CIDER 0.13.0snapshot (package: 20160612.1230), nREPL 0.2.12
    ;; Clojure 1.7.0, Java 1.8.0_73
    
    leiningen 2.6.1
    Cascalog 3.0
    emacs 24.5.1  - completely refreshed, all new packages.
    OS X 10.11.5
    

## Reproduction.

The short story:  
jack-in, load the code, change the namespace, > `(codes 3)`. 
add a function, (defn t [x] x), load the function, run `(codes 3)` again.
It will fail.  reload again, function, file, it doesn't matter.  

The only fix is to reload the function and then the 
entire file. It may not even matter which function you load.
This creates a scenario where squiggly is not invoked and the code actually 
loads completely.

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

## Wait there's more.

I tried running my hybrid clojure / cascalog function `(filter-test foo-catv)`, but got the name wrong.
I got the name right and got an arity exception. I fixed it to call the proper function and reloaded just
the function.  It failed with the same arity exception. I reloaded it again and it failed again.
Finally I reloaded the entire file `(C-c C-k)` and it worked.

## What that looked like.

Remember, the code we are running _never_ changes.

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

### No squiggly, stuff works.

This is pretty obvious, In the nrepl messages, every stack trace has some squiggly clojure before it.  The submissions that work don't.

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
function name for the filter in filter-test.  Then gave the right name to get an arityexception.  I then fixed the function, reloadad just the function and re-ran `(filter-test foo-catv)` which is the clojure/cascalog hybrid version of `(cats foo-catv)`.  I continued to get the same arity exception until I reloaded all of the code with (C-c C-k).


Bizarre!


## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
