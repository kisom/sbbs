(ns sbbs.dbmap
  (:use [com.ashafa.clutch :only [get-database bulk-update get-document]])
  (:use [cheshire.core :as json])
  (:require [sbbs.records])
  (:import [sbbs.records Comment]))

;;;; dbmap.clj
;;;; interface between database and data types specified in record.clj; also
;;;; provides functions for accessing the database.

;;; connect to the component databases
;;;     TODO: read from env

;;; right now, I don't know how to read env vars in clojure, so
;;; dbnames are hardcoded
(def -sbbs-categorydb-name (System/getenv "SBBS_CATEGORYDB"))
(def -sbbs-commentdb-name (System/getenv "SBBS_COMMENTDB"))
(def -sbbs-userdb-name (System/getenv "SBBS_USERDB"))

(def sbbs-categorydb (get-database -sbbs-categorydb-name))
(def sbbs-commentdb (get-database -sbbs-commentdb-name))
(def sbbs-userdb (get-database -sbbs-userdb-name))

;;; utility functions to translate ids to friendly names
(defn- name-from-id [id db]
  (:name (get-document db id)))

(defn category-name-from-id
  "Translate a category ID into its human-readable name."
  [categoryid]
  (name-from-id categoryid sbbs-categorydb))

(defn user-name-from-id
  "Translate a user ID into its human-readable name."
  [userid]
  (name-from-id userid sbbs-userdb))

(defn add-user
  "Add a user to the database."
  [username]
  (bulk-update sbbs-userdb [{:name username}]))

;;; currently a stubbed pass-through, enables us to easily support
;;; encrypting comments.
(defn restore-comment
  "Perform any manipulation required to get comment into readable form, i.e. if
a BBCode filter is to be used or the comment is encrypted, it should be
proccessed here. Currently, the comment is just passed through with any
manipulation."
  [comment]
  comment)

;;; load the comment from the database
(defn load-comment
  "Given a comment ID, grab the revelant document from the database and load it
into an sbbs.records.Comment record."
  [id]
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
(defn store-comment
  "Store an instance of an sbbs.records.Comment record in the database."
  [comment]
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

(defn reply-to-comment
  "Given the user ID of the user replying, timestamp of the reply, text of the
reply, and ID of the comment being replied to, create an appropriate instance of
an sbbs.records.Comment record."
  [userid posted_at text parentid]
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

;;; create a new category
(defn create-category
  "Create a new category."
  [category-name category-description]
  (:id
   (first
    (bulk-update
              sbbs-categorydb [
                               { :name category-name
                                :description category-description }]))))

;;; determine whether a comment is the thread leader
(defn thread-parent?
  "Given an instance of an sbbs.records.Comment record, determine if it is the
parent comment in a thread. Returns true if the comment is the parent, and
false otherwise."
  [comment]
  (or (= 0 (:parent comment)) (nil? (:parent comment))))

(defn id-thread-leader?
  "Given the ID of a comment, determine if it is the parent comment in a
thread. Returns true if the comment is the parent, and false otherwise."
  [commentid]
  (let [comment (load-comment commentid)]
      (or (= 0 (:parent comment)) (nil? (:parent comment)))))

;;; is the comment in the db
(defn valid-comment?
  "Returns true if the given comment ID belongs to a comment in the database."
  [commentid]
  (not (nil? (load-comment commentid))))

;;; functions for building urls to access the various Couch views

(defn- get-db-base-url
  "Returns the base url for the comment database. Useful as a building block
in checking views."
  []
  (format "%s://%s:%d%s/_design/comments/_view"
          (:protocol sbbs-commentdb)
          (:host sbbs-commentdb)
          (:port sbbs-commentdb)
          (:path sbbs-commentdb)))

(defn- get-category-db-base-url
  "Returns the base url for the category database. Useful as a building block
in checking views."
  []
  (format "%s://%s:%d%s/_design/categories/_view"
          (:protocol sbbs-categorydb)
          (:host sbbs-categorydb)
          (:port sbbs-categorydb)
          (:path sbbs-categorydb)))

(defn- get-user-db-base-url
  "Returns the base url for the user database. Useful as a building block in
checking views."
  []
    (format "%s://%s:%d%s/_design/users/_view"
          (:protocol sbbs-userdb)
          (:host sbbs-userdb)
          (:port sbbs-userdb)
          (:path sbbs-userdb)))

(defn- parent-view-url
  "Returns the url for the Couch view that returns all parents."
  []
  (format "%s/parents" (get-db-base-url)))

(defn- retrieve-couch-view-results
  "Given the url for a particular view (with all parameters filled in),
retrieve the results and decode it from JSON to a Clojure map."
  [url]
  ((cheshire.core/decode
    (slurp url)) "rows"))

(defn get-parents-for-category
  "Returns all parents in a given category."
  [categoryid]
  (let [parents (retrieve-couch-view-results (parent-view-url))]
    (filter #(= 0 (:parent %))
            (filter #(= categoryid (:category %))
                    (map #'load-comment
                         (map #(% "id") parents))))))

(defn user-id-from-name
  "Translate a username to its respective ID."
  [username]
  (first
   (map #(% "id")
        (retrieve-couch-view-results
         (format "%s/list_users?key=\"%s\""
                 (get-user-db-base-url)
                 username)))))

;;; get the user ID from the current user
(defn get-userid
  "Get the current user's ID."
  []
  (user-id-from-name
   (System/getenv "LOGNAME")))

(defn- reply-view-url
  "Returns the url for the Couch view that returns all replies for a given
parent comment ID."
  [parentid]
  (format "%s/replies?key=\"%s\""
          (get-db-base-url)
          parentid))

(defn get-replies
  "Given a parent ID, retrieve a list of all replies to that comment."
  [parentid]
  (retrieve-couch-view-results (reply-view-url parentid)))

(defn build-thread
  "Given a parent ID, create a list of sbbs.records.Comment records for the
correspondng document in the database and all corresponding replies."
  [parentid]
  (sort-by :posted_at <
           (flatten
            (vector
             (load-comment parentid)
             (map #'load-comment
                  (map #(% "id")
                       (get-replies parentid)))))))

(defn category-list-view-url
  "Returns the url for the Couch view that returns all the categories for which
there exist comments."
  [categoryid]
  (format "%s/catlist?key=\"%s\""
          (get-db-base-url)
          categoryid))

(defn build-category
  "Given a category ID, build a list of all the threads in that category."
  [categoryid]
  (map #'build-thread
       (map #(% "id")
            (retrieve-couch-view-results
             (category-list-view-url categoryid)))))

(defn category-thread-count
  "Get a count of the number of threads in a category."
  [categoryid]
  (count
   (get-parents-for-category categoryid)))

(defn get-category-list
  "Retrieve a list of all categories."
  []
  (map
    #(% "key")
    (retrieve-couch-view-results
     (format "%s/list_categories" (get-category-db-base-url)))))

(defn category-id-from-name
  "Translate a category name to its respective ID."
  [category]
  (first
   (map #(% "id")
        (retrieve-couch-view-results
         (format "%s/list_categories?key=\"%s\""
                 (get-category-db-base-url)
                 category)))))
