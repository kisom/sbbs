(ns sbbs.interface
  (:use [sbbs.comment]
        [sbbs.dbmap]
        [sbbs.display]
        [sbbs.util]))

(declare invalid-category)
(declare category-view)
(declare toplevel-view)
(declare invalid-category)
(declare toplevel-view)
(declare category-view-input-invalid)
(declare category-view-input)
(declare post-new-thread)
(declare select-thread-from-category)
(declare invalid-thread-selection)
(declare thread-view)

(defn print-with-flush
  [fmt & args]
  (let [print-args (flatten [fmt args])]
    (apply #'printf print-args)
    (flush)))

(defn prompt
  [fmt & args]
  (print-with-flush fmt args)
  (read-line))

(defn goto-category
  []
  (let [category-list (sbbs.dbmap/get-category-list)
        category (prompt "category> ")]
    (if (sbbs.util/in? category-list category)
      (category-view category)
      (invalid-category))))

;;; top-level display of groups
(defn toplevel-view
  []
  (println "\nsbbs: toplevel\n---------------")
  (sbbs.display/print-categories-with-count)
  (let [user-in (prompt "\ng(oto category) q(uit) |> ")]
    (cond (= user-in "q") (System/exit 0)
          (= user-in "g") (goto-category)
          true (toplevel-view))))

(defn- invalid-category
  [category]
  (print-with-flush "%s is not a valid category!\n")
  (toplevel-view))

(defn category-view
  [category]
  (println (format "\nsbbs: %s\n------%s\n"
                   category
                   (clojure.string/join (repeat (count category) "-"))))
  (category-view-input category (sbbs.display/print-thread-list category)))

(defn category-view-input-invalid
  [category threads]
  (println "invalid input!")
  (category-view-input category threads))

(defn category-view-input
  [category threads]
  (let [user-in
        (prompt "l(ist threads) n(ew thread) r(ead thread) t(oplevel) q(uit) |> ")]
    (cond (= user-in "t") (toplevel-view)
          (= user-in "r") (select-thread-from-category category threads)
          (= user-in "n") (post-new-thread category)
          (= user-in "q") (System/exit 0)
          (= user-in "l") (category-view category)
          true (category-view-input-invalid category category threads))))

(defn post-new-thread
  [category]
  (let [title (prompt "title: ")
        text (prompt "text: ")]
    (sbbs.comment/post title text category))
  (category-view category))

(defn select-thread-from-category
  [category threads]
  (let [user-in (prompt "thread> ")]
    (if (or (> 0 (Integer. user-in))
            (< 10 (Integer. user-in)))
      (invalid-thread-selection category threads)
      (thread-view category (first
                             (filter #(= user-in (:num %)) threads))))))


(defn invalid-thread-selection
  [category threads]
  (println "invalid thread!")
  (category-view-input category threads))

(defn thread-view
  [category thread]
  (printf "thread %s selected\n" thread)
  (print-thread (:id thread)))