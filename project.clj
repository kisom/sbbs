(defproject sbbs "0.9.1-beta"
  :description "simple bulletin board system"
  :license {:name "ISC license"
            :url "http://www.tyrfingr.is/licenses/LICENSE.ISC"
            :distribution :repo}
  :url "https://github.com/kisom/sbbs"
  :main sbbs.core
  :dependencies [
                 [org.clojure/clojure "1.4.0"]
                 [com.ashafa/clutch "0.4.0-SNAPSHOT"]
                 [cheshire "4.0.0"]
                 [clj-time "0.4.3"]])
