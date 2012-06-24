(ns sbbs.util)

(defn timestamp-now  []
  (. java.lang.System currentTimeMillis))

(defn in?
  "Check whether val is in coll."
  [coll val]
  (if (map? coll)
    (val coll)
    (not= -1 (.indexOf coll val))))

(defn- user-in-group?
  "Determine whether the current user is in the specified group."
  [group]
  (let [group-file (slurp "/etc/group")
        group-regex (format "%s:\\*:\\d+:\\w*" group)]
    (in?
     (clojure.string/split
      (last
       (clojure.string/split
        (first
         (re-seq (re-pattern group-regex) group-file))
        #":"))
      #",")
     (System/getenv "LOGNAME"))))

(defn authorised-user?
  "Check the group file for authorised users."
  []
  (user-in-group? "sbbs"))

(defn authorised-admin?
  "Determine whether the current user is an admin."
  []
  (user-in-group? "sbbs-admin"))