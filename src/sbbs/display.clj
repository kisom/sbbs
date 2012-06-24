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
    (if (empty? leader)
      (printf "---------\n%s\n" (:title comment)))
    (printf "%sauthor: %s at %s\n"
            leader
            (sbbs.dbmap/user-name-from-id (:userid comment))
            (format-timestamp (:posted_at comment)))
    (printf "%s%s\n" leader (:text comment))
    (printf "\n")
    (flush)))

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
  []
  (doseq [category (filter #(sbbs.util/authorised-category? %)
                           (sbbs.dbmap/get-category-list))]
    (printf "%s (%d)\n"
            category
            (sbbs.dbmap/category-thread-count
             (sbbs.dbmap/category-id-from-name category)))))

(defn print-thread-list
  [category]
  (let [sorted-threads (sort-by :posted_at >
                                (flatten
                                 (sbbs.dbmap/get-parents-for-category
                                  (sbbs.dbmap/category-id-from-name category))))
        thread-select (map #(hash-map :num %1
                                      :id (:id %2)
                                      :comment %2)
                           (map str (take 10 (iterate inc 1)))
                           sorted-threads)]
    (doseq [thread thread-select]
      (printf "%s: %s %s - %s\n"
              (:num thread)
              (format-timestamp (:posted_at (:comment thread)))
              (sbbs.dbmap/user-name-from-id (:userid (:comment thread)))
              (:title (:comment thread))))
    thread-select))