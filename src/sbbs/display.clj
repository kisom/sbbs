(ns sbbs.display
  (:require [sbbs.dbmap])
  (:require [sbbs.records])
  (:import [sbbs.records Comment])
  (:import [sbbs.records User])
  (:import [sbbs.records Category]))

(defn format-timestamp [timestamp]
  (format "%d" timestamp))

;;; display a comment
;;;; TODO: word wrapping
(defn print-comment [comment]
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
(defn print-thread [comment-thread]
  (let [sorted-thread (sort-by :posted_at < comment-thread)]
    (doseq [comment sorted-thread]
      (print-comment comment))))