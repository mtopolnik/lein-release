(defproject com.ingemark/lein-release "2.1.3-SNAPSHOT"
  :description "Cut a new release: deploy, tag, bump to new snapshot version."
  :url "https://github.com/Inge-mark/lein-release"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :lein-release {:deploy-via :lein-deploy-clojars}
  :eval-in :leiningen)
