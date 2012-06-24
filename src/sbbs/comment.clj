(ns sbbs.comment
  (:use [sbbs.records])
  (:use [sbbs.dbmap])
  (:use [sbbs.util])
  (:import [sbbs.records Comment]))

(defn post
  "Post a new comment."
  [title text category]
  (store-comment
   (create-comment
    (sbbs.dbmap/get-userid)
    (sbbs.util/timestamp-now)
    title
    text
    0
    (category-id-from-name category))))

(defn reply
  "Reply to a comment."
  [text parentid]
  (sbbs.dbmap/reply-to-comment
   (sbbs.dbmap/get-userid)
   (sbbs.util/timestamp-now)
   parentid))
