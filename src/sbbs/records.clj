(ns sbbs.records)
;;;; contains the data types used in sbbs and functions for
;;;; manipulating those data types.

;;; the prefix for group names
(def group-prefix "sbbs")

;;; a comment consists of the following fields:
;;;    id
;;;    userid          the userid of the user posting the comment
;;;    posted_at       the date and time the comment was first posted
;;;    edited_at       the date and time the comment was last edited
;;;                    (or 0 if not edited)
;;;    text            the body of the comment
;;;    parent          the id of the first comment in the thread; zero
;;;                    if the comment is the first comment in the thread
;;;    category        which category does the comment belong to;
;;;                    primarily useful for the parent comment
(defrecord Comment
    #^{ :doc "representation of a comment in a thread" }
  [id userid posted_at edited_at title text parent category])

;;; a User is tied to the underlying UNIX host; ergo, all permissions
;;; and user names are those assigned by the system
;;; administrator. Consequently, all access control is done via UNIX
;;; groups. This record exists solely to tie a user's login name to
;;; the database.
(defrecord User
    #^{ :doc "database representation of a user, tied to UNIX logname"}
  [id name])

;;; A category (sometimes called a subforum) is simply an
;;; organisational label to logically group a collection of threads.
(defrecord Category
    #^{ :doc "Representation of a category or subforum, a collection of
threads" }
  [id name description])

;;; convert category to group name
(defn category-to-group [category]
  "Convert a category name to the expected UNIX group name"
  (format "%s-%s" group-prefix category))

;;; shortcut for creating comments with a nil id, i.e. for storing
(defn create-comment [userid posted_at edited_at title text parent category]
  (Comment. nil userid posted_at edited_at title text parent category))
