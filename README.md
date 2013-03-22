# lein-release

Leniningen task to help you cut a new release of your git-based project. In detail, these are the steps:

1. check for uncommitted changes and refuse continue in case of any;
2. edit `project.clj`: remove the "-SNAPSHOT" suffix. If the suffix is not there, don't touch the version;
3. deploy the project using the configured deployment strategy (see below);
4. in case of a failed deployment, revert to the original project version;
5. commit the edited `project.clj` and tag it with `<project>-<version>`;
6. edit `project.clj`: move to the next SNAPSHOT version;
7. commit the edited `project.clj`.

In step 6. the task will always bump the rightmost integer occuring in the project version, for example:

- `0.1.0` -> `0.1.1`
- `2.0.0-alpha1` -> `2.0.0-alpha2`
- `3.0.0.RC1` -> `3.0.0-RC2`

You are free to manually edit the resulting SNAPSHOT version before your next release.


# Installation and usage

Add `[com.ingemark/lein-release "2.0.18"]` to the `:plugins` vector of your `project.clj` or `profiles.clj`. This will give you the `release` leiningen task:

```shell
$ lein release
```

# Configuration

Configure the task with a map under the `:lein-release` key. There is only one option: `:deploy-via`, specifying the deployment strategy.

```clojure
(defproject imagine "1.0.0-SNAPSHOT"
  :lein-release {:deploy-via :clojars}
  :plugins [[com.inge-mark/lein-release "2.0.0"]]

  ;; more stuff
)
```
You can `:deploy-via`:

- `:clojars`;
- `:lein-deploy`;
- `:lein-install`;
- `:none`.

`:none` is useful on non-dependency projects, those that get released as a custom downloadable bundle. Such projects still need versioning. The default is `:lein-deploy` if there are `:repositories` defined for the project; else it is `:none`.

# Author

Marko Topolnik <marko.topolnik@inge-mark.hr>

# License

Copyright © 2013 Inge-mark d.o.o.

Distributed under the Eclipse Public License, the same as Clojure.
