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

## What to do.

Start with a change to the name space `(C-c M-n)`, load the code `(C-c C-k)`. Change to the repl `(C-c C-z)`.  

run `(codes 3)`

See crash.  Go back to code, reload, return to repl.
`(C-c C-z) (C-c C-k) (C-c C-z)`

Run `(codes 3)`  again. 

Alternatively run `(cats foo-catv)` 

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
    cascalog-check.core> 
```

## .lein/profiles.clj

See profiles.clj.

## nrepl messages.

See the nrepl-messages.txt file for the messages from this session.

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
