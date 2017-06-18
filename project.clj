
(def cljs-main "fusion360.core") ;; we'll use this in  templates & build profiles

(defproject fusion360-repl "0.1.0-SNAPSHOT"
  :description "Cojure repl for Fusion 360 3d modelling"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.562"]
                 [instaparse "1.4.7"]
                 [sockjs-clojure "0.1.1"]
                 [http-kit "2.2.0"]
                 [cheshire "5.7.1"]
                 [ring/ring "1.6.1"]
                 [compojure "1.6.0"]
                 [org.clojure/core.async "0.3.443"]]

  :plugins [[lein-resource "17.06.1"]
            [lein-cljsbuild "1.1.6"]]

  :hooks [leiningen.resource]

  :source-paths ["src/clj" "src/cljc" "src/cljs"] 

  
  :cljsbuild {:builds {:app {:source-paths ["src/cljs" "src/cljc"]
                             :compiler {:main          ~cljs-main
                                        :output-to     "target/fusion-plugin/out/clojure_repl.js"
                                        :output-dir    "target/fusion-plugin/out"
                                        :asset-path    "out"
                                        :infer-externs true
                                        :optimizations :none ;; :simple ;; :none
                                        }}}}
  :cljs-main ~cljs-main ;; just used in resource templates
  :resource {:resource-paths [["resources/fusion" {:target-path "target/fusion-plugin/"}]]
             :excludes [#".*\.DS_Store"]
             :skip-stencil [#".*\.png"]}

  :profiles {:dev {:source-paths ["env/dev"]
                   :main dev
                   :dependencies [[com.cemerick/piggieback "0.2.1"]]}})
