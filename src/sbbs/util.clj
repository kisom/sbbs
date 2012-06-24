(ns sbbs.util
  (:use [sbbs.records]
        [sbbs.dbmap :only [user-id-from-name]]))

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
        group-regex (format "%s:[\\*x!]:\\d+:[\\w,]*" group)
        groups (re-seq (re-pattern group-regex) group-file)]
    (if (empty? groups)
      false
      (in?
       (clojure.string/split
        (last
         (clojure.string/split (first groups) #":"))
        #",")
       (System/getenv "LOGNAME")))))

(defn authorised-user?
  "Check the group file for authorised users."
  []
  (and
   (not (nil? (sbbs.dbmap/user-id-from-name (System/getenv "LOGNAME"))))
   (user-in-group? "sbbs")))

(defn authorised-admin?
  "Determine whether the current user is an admin."
  []
  (user-in-group? "sbbs-admin"))

(defn authorised-category?
  "Determine whether the current user is authorised to view the category."
  [category]
  (user-in-group? (sbbs.records/category-to-group category)))