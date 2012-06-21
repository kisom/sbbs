(ns sbbs.samples
  (:use [sbbs.dbmap])
  (:use [sbbs.records])
  (:use [com.ashafa.clutch :only [get-document]]))

;;; first comment is in projects
(def test-comment
  (create-comment "ff7895ff985a18872e584c53dc0001d6" 1340225233
                  "I think we should start doing commenty things"
                  "Just wanted to test some of the comment functions."
                  0 "ff7895ff985a18872e584c53dc000a1c"))

(def parent-id (store-comment test-comment))

(def reply-id
  (store-comment (reply-to-comment
                  "ff7895ff985a18872e584c53dc0001d6"
                  1340227128
                  "I second that motion!"
                  parent-id)))

(def test-thread
  [(load-comment reply-id)
   (load-comment parent-id)])

(def test-comment2
  (create-comment "ff7895ff985a18872e584c53dc0001d6" 1340227833
                  "server now operational"
                  "clojure dev server with private couchdb instance is up."
                  0 "ff7895ff985a18872e584c53dc001704"))

(def parent-id2 (store-comment test-comment2))

(def reply-id2
  (store-comment (reply-to-comment
                  "ff7895ff985a18872e584c53dc0001d6"
                  13406040210
                  "what are the access credentials?"
                  parent-id2)))

(def test-thread2
  [(load-comment reply-id2)
   (load-comment parent-id2)])

(def test-comment3
  (create-comment "ff7895ff985a18872e584c53dc0001d6"
                  1340256087
                  "doppelganger running on kali"
                  "(kali is a sheeva plug)"
                  0
                  "ff7895ff985a18872e584c53dc001704"))

(def parent-id3 (store-comment test-comment3))

(def reply-id3a
  (store-comment
   (reply-to-comment
    "8795883be09db16fe3013fa0c2000600"
    1340256105
    "what's kali's IP address again?"
    parent-id3)))

(def reply-id3b
  (store-comment
   (reply-to-comment
    "ff7895ff985a18872e584c53dc0001d6"
    1340256191
    "it's 192.168.5.32"
    parent-id3)))

(def comment-thread1
  (vector
   (load-comment parent-id2)
   (load-comment reply-id2)
   (load-comment parent-id3)
   (load-comment reply-id3a)
   (load-comment reply-id3b)))

(def comment-thread2
  (vector
   (load-comment parent-id)
   (load-comment reply-id)))

(def comment-board
  (vector
   comment-thread1
   comment-thread2))