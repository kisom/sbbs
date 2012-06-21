(ns sbbs.dbmap
  (:use [com.ashafa.clutch :only [get-database bulk-update get-document]])
  (:use [cheshire.core :as json])
  (:require [sbbs.records])
  (:import [sbbs.records Comment])
  (:import [sbbs.records User])
  (:import [sbbs.records Category]))

;;; connect to the component databases
;;;     TODO: read from env

;;; right now, I don't know how to read env vars in clojure, so
;;; dbnames are hardcoded
(def -sbbs-categorydb-name "sbbs_dev1_categories")
(def -sbbs-commentdb-name "sbbs_dev1_comments")
(def -sbbs-userdb-name "sbbs_dev1_users")

(def sbbs-categorydb (get-database -sbbs-categorydb-name))
(def sbbs-commentdb (get-database -sbbs-commentdb-name))
(def sbbs-userdb (get-database -sbbs-userdb-name))

;;; utility functions to translate ids to friendly names
(defn- name-from-id [id db]
  (:name (get-document db id)))

(defn category-name-from-id [categoryid]
  (name-from-id categoryid sbbs-categorydb))

(defn user-name-from-id [userid]
  (name-from-id userid sbbs-userdb))

;;; currently a stubbed pass-through, enables us to easily support
;;; encrypting comments.
(defn restore-comment [comment]
  comment)

;;; load the comment from the database
(defn load-comment [id]
  (let [raw-comment (get-document sbbs-commentdb id)
        comment (restore-comment raw-comment)
        loaded-comment (sbbs.records.Comment.
                        (:_id comment)
                        (:userid comment)
                        (:posted_at comment)
                        (:edited_at comment)
                        (:title comment)
                        (:text comment)
                        (:parent comment)
                        (:category comment))]
    (if (nil? (:id loaded-comment))
      nil
      loaded-comment)))


;;; store a comment in the database
(defn store-comment [comment]
  (let [parent (if (nil? (:parent comment)) 0 (:parent comment))]
   (:id (first
         (bulk-update sbbs-commentdb [
                                      {:userid (:userid comment)
                                       :posted_at (:posted_at comment)
                                       :edited_at (:edited_at comment)
                                       :title (:title comment)
                                       :text (:text comment)
                                       :parent (:parent comment)
                                       :category (:category comment) }
                                      ])))))

(defn reply-to-comment [userid posted_at text parentid]
  (if (= 0 parentid)
    nil
    (let [parent (load-comment parentid)]
      (sbbs.records/create-comment
       userid
       posted_at
       (:title parent)
       text
       (:id parent)
       (:category parent)))))

;;; determine whether a comment is the thread leader
(defn thread-leader? [comment]
  (or (= 0 (:parent comment)) (nil? (:parent comment))))

(defn id-thread-leader? [commentid]
  (let [comment (load-comment commentid)]
      (or (= 0 (:parent comment)) (nil? (:parent comment)))))

;;; is the comment in the db
(defn valid-comment? [commentid]
  (not (nil? (load-comment commentid))))

;;; retrieve a sorted vector of comments in a thread
(defn load-thread [parentid]
   (sort-by :posted_at <
            (vector nil)))

 ;; (defn get-thread-parent [comment-thread]
 ;;   (let [parent (filter #(= (:parent %) 0) comment-thread)]
 ;;     (if (= (count parent) 1)
 ;;       (first parent)
 ;;       nil)))

;;; assume we are dealing with a vector of comments that match a
;;; particular category; find all the parent threads
(defn get-comment-threads [category]
  (filter #(= (:parent %) 0) category))

(defn get-comment-thread-titles [category]
  (map :title (get-comment-threads category)))

(defn get-db-base-url []
  (format "%s://%s:%d%s/_design/comments/_view"
          (:protocol sbbs-commentdb)
          (:host sbbs-commentdb)
          (:port sbbs-commentdb)
          (:path sbbs-commentdb)))

(defn reply-view-url [parentid]
  (format "%s/replies?key=\"%s\""
          (get-db-base-url)
          parentid))

(defn retrieve-couch-view-results [url]
  ((cheshire.core/decode
    (slurp url)) "rows"))

(defn get-replies [parentid]
  (retrieve-couch-view-results (reply-view-url parentid)))

(defn build-thread [parentid]
  (flatten
   (vector
    (load-comment parentid)
    (map #'load-comment (map #(% "id") (get-replies parentid)))
    )))

(defn category-list-view-url [categoryid]
  (format "%s/catlist?key=\"%s\""
          (get-db-base-url)
          categoryid))

(defn build-category [categoryid]
  (map #'build-thread
       (map #(% "id")
            (retrieve-couch-view-results
             (category-list-view-url categoryid)))))