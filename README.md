# Scoped-ref

A Clojure library designed to add scoping capabilities to the `clojure.lang.Atom` and possibly other reference types and some point.

This library only adds a protocol called `IAssociativeAtom` with a single function called `scope`. I implemented these for `clojure.lang.Atom` and `scoped-ref.atom.ScopedAtom`.

`[scoped-ref "0.1.0-SNAPSHOT"]` in lein dependencies to add it.

## Usage

The usage then is as follows:
```
(ns my.awesome.project
  (:require [scoped-ref.atom :refer [scope]]))

(def app-state (atom {:this {:should [:be :a :nested :structure]}
                      :example {:path 0}}))
;; this is common in Clojure and you might already have something like this.

(def scoped-state (scope app-state [:example :path]))
```

Now `scoped-state` is usable like any other atom but all actions that happen to it are scoped to its path.
```
(swap! scoped-state inc) ;; result=> 1

@scoped-state ;; result=> 1

@app-state ;; result=> {:example {:path 1} :etc :etc}
```

This ultimately allows you to thread together multiple state changing tasks in 1 atom. Which you might want, or you might not. I made it up because I disliked the idea of putting atoms within one another. Also it allows you to make separate tasks that can coordinate state change on shared state without being forced to know the structure in its entirety.

## Notes

#### On validators
Don't use `set-validator!`, in short.

The issues with validators, from an implementation stand-point, arises from the validator not being atomically swappable like the atom itself. Making it unable for me to compose a validator for multiple scopes. This made me think about it and I believe you should try to not use `set-validator!` at all. The legal states of your identity, to my idea, should never depend on input that is received after instantiating the identity.

I might add a function to compose validators from more scopes manually so they can at least be set at instantiation time. Or feel free to fork and try that yourself. For now `set-validator!` will keep its normal functionality so it doesn't immediately break a project you give a `scoped-ref.atom.ScopedAtom`. But prefer to not use it.

#### On parallelism
Great now I can run my 10000 components all in 1 managed ref, and it's simple...

Well, Originally I started this project where the idea was to make a new reference for every scope. So then you might be able to run everything from 1 state and still have a ton of parallelism.

In this project though, you run it from 1 atom. This means you still get the concurrency benefits of atoms themselves. For example running all `swap` calls in different threads and not having to worry about anything going wrong. But when it comes time to `compareAndSet` and the actions are orthogonal (because their paths are subsets of one another) it will still retry.

I am still thinking about how to implement behavior where those actions that are orthogonal never have to retry, but they are still in 1 managed ref and can be dereferenced to a value. Not sure if the gain in concurrency is really worth it though. Haven't looked into databases that much but I suspect they have a good way to handle these kinds of things and at that scale you're probably already using that.

#### On printing
I tried making printing properly work, couldn't really find what the idiomatic way to do that in Clojure is. So if someone knows, please let me know. It does work well enough for now I suppose.

#### On tests
I haven't finished testing all the features, you also might want to check the test for some more examples.


#### Last notes
Still learning programming, so if you have any comments at all, I'd be happy to know.

Got inspiration/knowledge from [cgrand/megaref](https://github.com/cgrand/megaref), [omcljs' Cursors](https://github.com/omcljs/om/wiki/Cursors) and [jammii/bigatom](https://github.com/jamii/bigatom)

## License

Copyright © Sander Kolman, All Rights Reserved

Distributed under the [Eclipse Public License v1.0](https://opensource.org/licenses/eclipse-1.0.php)
