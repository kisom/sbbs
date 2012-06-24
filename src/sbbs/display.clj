(ns sbbs.display
  (:require [sbbs.dbmap])
  (:require [sbbs.records])
  (:import [sbbs.records Comment])
  (:use [clj-time.coerce]
        [clj-time.local]
        [clj-time.format]))

(defn format-timestamp
  "Given a timestamp, display it in human-readable format."
  [timestamp]
  (let [date-timestamp (clj-time.coerce/from-long timestamp)]
    (str date-timestamp)))

;;; display a comment
;;;; TODO: word wrapping
(defn print-comment
  "Pretty printing for a comment. Currently, doesn't word wrap."
  [comment]
  (let [leader (if (sbbs.dbmap/thread-parent? comment) "" "\t")]
    (if (sbbs.dbmap/thread-parent? comment)
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
  [parentid]
  (let [comment-thread (sbbs.dbmap/build-thread parentid)
        sorted-thread (sort-by :posted_at < comment-thread)]
    (doseq [comment sorted-thread]
      (print-comment comment))))

(defn print-categories
  "Pretty print a list of categories."
  []
  (doseq [category (sbbs.dbmap/get-category-list)]
    (println category)))

(defn print-categories-with-count
  "Pretty print a list of categories with the number of posts in each
category."
  []
  (doseq [category (sbbs.dbmap/get-category-list)]
    (printf "%s (%d)\n"
            category
            (sbbs.dbmap/category-thread-count
             (sbbs.dbmap/category-id-from-name category)))))

(defn print-thread-list
  "Print a list of threads in a category."
  [category]
  (let [parents (sbbs.dbmap/get-parents-for-category
                   (sbbs.dbmap/category-id-from-name category))
        sorted-parents (sort-by :posted_at < parents)]
    (doseq [thread sorted-parents]
      (printf "%s %s - %s (parent: %s)\n"
              (format-timestamp (:posted_at thread))
              (sbbs.dbmap/user-name-from-id (:userid thread))
              (:title thread)
              (:id thread)))))

(defn print-thread-from-title
  "Given a title, print the thread."
  [title]
  )