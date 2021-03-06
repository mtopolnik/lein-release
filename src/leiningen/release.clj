(ns leiningen.release
  (require (leiningen.core [main :refer [apply-task abort]] [eval :as eval])
           (clojure.java [shell :as sh] [io :as io])
           [clojure.string :as s]))

(set! *warn-on-reflection* true)

(defn fail [& args] (throw (Exception. (s/join " " args))))

(defn sh! [& cmd]
  (apply println "$" cmd)
  (let [res (apply eval/sh cmd)]
    (when-not (zero? res) (abort "Command failed with exit code %s: %s" res cmd))
    res))

(defn deploy-via [project]
  (let [deploy-via (-> project :lein-release :deploy-via)
        supported #{:lein-deploy :lein-deploy-clojars :lein-install :none}]
    (cond
     deploy-via (or (supported deploy-via)
                    (abort ":deploy-via" deploy-via "is not supported. Use one of" supported))
     (:repositories project) :lein-deploy
     :else :none)))

(defn ->release [v] (s/replace v #"-SNAPSHOT$" ""))

(defn ->new-snapshot [v]
  (str (s/replace v #"\d+(?!.*\d)" #(str (inc (Long/parseLong %)))) "-SNAPSHOT"))

(defn updated-version [project update-fn]
  (let [curr-version (:version project), new-version (update-fn curr-version)]
    (when (not= new-version curr-version) new-version)))

(defn update-project! [project new-version]
  (if new-version
    (do (println "Edit project.clj: set version to" new-version)
        (-> (slurp "project.clj")
            (s/replace #"(defproject\s+\S+\s+)\"(.+?)\"" (format "$1\"%s\"" new-version))
            (->> (spit "project.clj")))
        (-> (assoc project :version new-version)
            (vary-meta assoc-in [:without-profiles :version] new-version)))
    project))

(defmacro lein-do [project & cmds]
  `(do ~@(for [c cmds :let [c (if (coll? c) c [c]), [cmd args] [(first c) (vec (rest c))]]
               cc [`(println "lein" ~@(map str c))
                   `(apply-task (name '~cmd) ~project ~args)]] cc)))

(defn release
  "Cut a new release of a git-based project: deploy, tag, bump version, commit."
  [project & args]
  (when-not (= 0 (:exit (sh/sh "git" "diff-index" "--quiet" "HEAD")))
    (abort "Cannot release: the project has uncommited changes."))
  (let [deploy-via (deploy-via project)
        current-version (:version project)
        release-version (updated-version project ->release)
        project (update-project! project release-version)]
    (try
      (println "Deploying via" deploy-via)
      (case deploy-via
        :lein-deploy (lein-do project clean deploy)
        :lein-deploy-clojars (lein-do project clean (deploy "clojars"))
        :lein-install (lein-do project clean install)
        :none nil)
      (catch Throwable t
        (sh! "git" "checkout" "project.clj")
        (throw t)))
    (when release-version
      (sh! "git" "add" "project.clj")
      (sh! "git" "commit" "-m" (format "Release %s (by lein-release)" release-version)))
    (sh! "git" "tag" (format "%s-%s" (:name project) (or release-version current-version)))
    (when-let [new-snapshot-version (updated-version project ->new-snapshot)]
      (update-project! project new-snapshot-version)
      (sh! "git" "add" "project.clj")
      (sh! "git" "commit" "-m" (format "Move to %s (by lein-release)" new-snapshot-version))
      (sh! "git" "push")
      (sh! "git" "push" "--tags"))))
