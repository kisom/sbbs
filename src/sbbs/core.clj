(ns sbbs.core
  (:gen-class)
  (use [sbbs.util]
       [sbbs.interface]))

(defn- not-authorised
  "The user is not authorised to run sbbs."
  []
  (println "You are not authorised to run sbbs.")
  (System/exit 1))

(defn -main [& args]
  (if (not (sbbs.util/authorised-user?))
    (not-authorised)
    (sbbs.interface/toplevel-view)))
