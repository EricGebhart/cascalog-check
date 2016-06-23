# cascalog-check

This is a simple example to reproduce a problem between cascalog and
cider.  After a load or reload there is frequently a stack-trace
from a clojure query with a filter. Even when that code has not changed.

It doesn't have to be cascalog, I've seen the same thing with an arity exception on a call.
fix the code, reload the function, the error persists. Change the call in 'filter test` to
use the wrong fiter function and you can see it in action.
But it's super easy to reproduce with cascalog. There is no need to create an error 
in the code to see it.  I just run `(codes 3)`.

This is happening with cider 10, cider 12 and cider 13. 

I have updated all emacs packages and made sure that the code has
no warnings or errors from flycheck-clojure.  There is a minimalist `.emacs`
which will create the minimum emacs configuration to replicate this.
The nrepl messages look clean at this point.

### Eastwood!

@pnf from squiggly-clojure:
I've found I can reproduce the FlowException by running the eastwood check manually, i.e.

    (cats foo-catv) ;; (["foo" [3 100 8 12] true])
    (eastwood.lint/lint {:source-paths ["src"], :namespaces ['cascalog-check.core], :continue-on-exception true, :exclude-linters [:unlimited-use]})
    (cats foo-catv) ;; throws
    ;; reload manually
    (cats foo-catv) ;; (["foo" [3 100 8 12] true])

Since this new understanding I have read a lot a explored Eastwood a bit. 
  * Changed to use Clojure 1.7.0  since Eastwood 2.1 needs that.
  * Excluded prismatic/schema from cascalog and added in the newest version which uses potemkin 0.4.1 instead of 0.3.2.
  * Tried each linter individually. Any linter causes the problem to appear.
  * Following the example above it is not necessary to disable flycheck. It just won't be loaded in the beginning.
  * To reproduce just change to the name space and run `(cats foo-catv)` if it doesn't work reload and try again. If it does work run eastwood.lint/lint and then run `(cats foo-catv)` again. It will get a Flow Exception.

Running Eastwood in the repl is experimental, based on how it works, it may not be surprising that something is happening to corrupt the session.

### kibit, eastwood and core.typed

I have tried versions recommended by squiggly and also the newest versions.  Or I thought I did.
Eastwood 2.1/2.3 and core-typed 3.7/3.23. Kibit is unchanged.

The message *"user-level profile defined in project files"*  makes no sense to me as there are clearly not any other than the
one in my .lein/profiles.clj


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

Manually running the commands which precede the failure in nrepl messages show no errors.

    cascalog-check.core> (do (require 'squiggly-clojure.core) (squiggly-clojure.core/check-kb 'cascalog-check.core "/Users/eric/Projects/cascalog-check/src/cascalog_check/core.clj"))
    []
    cascalog-check.core> (do (require 'squiggly-clojure.core) (squiggly-clojure.core/check-tc 'cascalog-check.core))
    []
    cascalog-check.core> (do (require 'squiggly-clojure.core) (squiggly-clojure.core/check-ew 'cascalog-check.core))
    ()
    []

### Additional behavior.

I have established through lots of iteration that adding a new function `(defn a [x] x)` will cause unrelated code to fail
whether the new function is evaluated or not. Loading the function, and reloading the entire file does not always work.  What does always work is a file reload after the file has been saved. But not the save following the cider prompt which asks to save the file before reloading. It takes one more reload after that to get the code working again.  

Doing everything at once can streamline the path to success.  Load the new function `(C-c C-c)`, Load the file with a save `C-c C-k`, 
Load the file again with no save `C-c C-k`.  Avoiding the cider save prompt makes it faster.  Add a function, save the file, reload the file with `C-c C-k`.    

Loading just the function will allow the function to work, but other code will stacktrace until the file is reloaded without a 
cider prompt to save.


If I add a new function `(defn a [x] x)`, save the file and load-file `C-c C-k`. There is no problem.  If I load-file and get a cider-prompt to save, that does not work.  If I load-file twice in a row, where the first one prompts for save, then that does work.  

It does not seem to matter if I eval the new function or not. 
Simply adding the function without any load of any kind and then going back to the repl and repeating the last eval `(codes 3)` will cause a crash.
Returning back to the code, saving and reloading will cause it to work the next time.
Returning back to the code, and reloading twice will also cause it to work the next time.
Or just doing that all at once works. Add a function, save, load, execute.

### Who's is it?

I have no idea.

It is somehow related to `flycheck-clojure`.  Disabling `flycheck-clojure` causes the problem to go away.

However it seems somehow related to whether the file is currently saved or not. 

Commenting out this code in my emacs setup causes everything to work just fine, except I lose squiggly lines.

    (eval-after-load 'flycheck '(flycheck-clojure-setup))
    (add-hook 'after-init-hook #'global-flycheck-mode)
    
    (eval-after-load 'flycheck
       '(setq flycheck-display-errors-function #'flycheck-pos-tip-error-me

### Cascalog logic ops only? um No.

This seems to only effect cascalog code with a filterfn and maybe others,
but there is a simple function, `filter-test` which uses the same filter function as cascalog, but uses clojure's filter on the results from a simpler
query which actually works.

```(filter-test foo-catv)```

Is a clojure / cascalog hybrid equivalent to the full cascalog
function call:

```(cats foo-catv)```

When `(cats foo-catv)` or `(codes 3)` is failing, `(filter-test foo-catv)` 
will continue to work using the same filter function that `cats` uses.

Well, Now I'm not so sure. I had an error in filter-test using the wrong filter function with the wrong number of args.  I repaired it, reloaded the function to no effect. Still wrong number of args.  Reload the function again, failure.  - 3 times. Finally reload entire file.  Success.
See **nrepl-messages4.txt**

## Environment.

I started last week with cider 10. I upgraded everything to cider 12,
and now I am using cider 13. 

There is a minimalist **.emacs** file in the repository. It automatically installs
these packages: cider, flycheck-clojure, clojure-mode, and flycheck-pos-tip.
The problem replicates with that simple emacs setup.

    Connected to nREPL server - nrepl://localhost:51662
    ;; CIDER 0.13.0snapshot (package: 20160612.1230), nREPL 0.2.12
    ;; Clojure 1.7.0, Java 1.8.0_73
    
    leiningen 2.6.1
    Cascalog 3.0
    emacs 24.5.1  - completely refreshed, all new packages. - It also failed with 24.4.1. 
                     Use the .emacs from the repo for a very fresh and minimalist emacs. 
                     **move your .emacs and .emacs.d out of the way first.**
    OS X 10.11.5
    

## Reproduction.

There is a very minimalist `.emacs` file in the repo, it will install
`cider, clojure-mode, flycheck-clojure and flycheck-pos-tip.` in your .emacs.d.

###The short story:  

jack-in, load the code - _it won't be_, change the namespace, > `(codes 3)`. 
add a function, (defn t [x] x), load the function, run `(codes 3)` again.
It will fail.  reload again, function, file, it doesn't matter.  

The only fix is to reload the entire file after a save, even if that save
happened as part of a reload. 

### The long description.

Cider jack in.  I let cider do all the injections.  See my profiles.clj in the repo.

Load core.clj into the repl, change to that namespace and run
`(codes 3)`  or `(cats foo-catv)`

If it results in a `FlowException local step failed`,  reload
the code and try again. Repeat as necessary until it works.

To break it again, sometimes just adding a blank line and reloading will
do it.  Sometimes I add a stupid function (defn foo [x] x), or delete
the same and then reload the code in order to break it.

## How to Reproduce

Start with a change to the name space `(C-c M-n)`, load the code `(C-c C-k)`. Change to the repl `(C-c C-z)`.  

run `(codes 3)`

See crash.  Go back to code, reload, return to repl.
`(C-c C-z) (C-c C-k) (C-c C-z)`

Run `(codes 3)`  again. 

Alternatively run `(cats foo-catv)` 

### To reintroduce the crash.

Add a function.  ```(defn s [x] x)```

Load just the function `(C-c C-c)`.  - this is not actually necessary.

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


## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
