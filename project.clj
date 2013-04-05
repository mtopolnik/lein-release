(defproject com.ingemark/lein-release "2.1.2-SNAPSHOT"
  :description "Cut a new release: deploy, tag, bump to new snapshot version."
  :url "https://github.com/Inge-mark/lein-release"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :lein-release {:deploy-via :clojars}
  :eval-in :leiningen)
