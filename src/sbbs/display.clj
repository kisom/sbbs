(ns sbbs.display
  (:require [sbbs.dbmap])
  (:require [sbbs.records])
  (:import [sbbs.records Comment])
  (:import [sbbs.records User])
  (:import [sbbs.records Category]))

(defn format-timestamp
  "Given a timestamp, display it in human-readable format."
  [timestamp]
  (format "%d" timestamp))

;;; display a comment
;;;; TODO: word wrapping
(defn print-comment
  "Pretty printing for a comment. Currently, doesn't word wrap."
  [comment]
  (let [leader (if (sbbs.dbmap/thread-leader? comment) "" "\t")]
    (if (sbbs.dbmap/thread-leader? comment)
      (printf "---------\n%s\n" (:title comment)))
    (printf "%sauthor: %s at %s\n"
            leader
            (sbbs.dbmap/user-name-from-id (:userid comment))
            (format-timestamp (:posted_at comment)))
    (printf "%s%s\n" leader (:text comment))
    (printf "\n")))

;;; print out a thread of comments
(defn print-thread
  "Pretty prints a thread of comments in order of posted_at timestamps."
  [comment-thread]
  (let [sorted-thread (sort-by :posted_at < comment-thread)]
    (doseq [comment sorted-thread]
      (print-comment comment))))

(defn print-categories
  "Pretty print a list of categories."
  []
  (doseq [category (get-category-list)]
    (println category)))

(defn print-categories-with-count
  "Pretty print a list of categories with the number of posts in each
category."
  []
  (doseq [category (get-category-list)]
    (printf "%s (%d)"
            category
            (category-thread-count (category-id-from-name category)))))

  