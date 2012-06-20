(ns sbbs.dbmap
  (:use [com.ashafa.clutch :only [get-database bulk-update get-document]])
  (:require [sbbs.records])
  (:import [sbbs.records Comment])
  (:import [sbbs.records User])
  (:import [sbbs.records Category]))

;;; connect to the category database
;;;     TODO: read from env
(def -sbbs-categorydb-name "sbbs_dev1_categories")
(def sbbs-categorydb (get-database -sbbs-categorydb-name))

(def -sbbs-commentdb-name "sbbs_dev1_comments")
(def sbbs-commentdb (get-database -sbbs-commentdb-name))

(def -sbbs-userdb-name "sbbs_dev1_users")
(def sbbs-userdb (get-database -sbbs-userdb-name))

;;; currently a stubbed pass-through, enables us to easily support
;;; encrypting comments.
(defn restore-comment [comment]
  comment)

;;; determine whether a comment is the thread leader
(defn thread-leader? [comment]
  (= 0 (:parent comment)))

;;; load the comment from the database
(defn load-comment [id]
  (let [raw-comment (get-document sbbs-commentdb id)
        comment (restore-comment raw-comment)]
    (sbbs.records.Comment. (:id comment)
                           (:userid comment)
                           (:posted_at comment)
                           (:edited_at comment)
                           (:title comment)
                           (:text comment)
                           (:parent comment)
                           (:category comment))))

;;; store a comment in the database
(defn store-comment [comment]
  (:id
   (bulk-update sbbs-commentdb [
                                 {:userid (:userid comment)
                                  :posted_at (:posted_at comment)
                                  :title (:title comment)
                                  :text (:text comment)
                                  :parent (:parent comment)
                                  :category (:category comment) }
                                 ])))
