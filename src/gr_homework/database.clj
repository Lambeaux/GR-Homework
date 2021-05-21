(ns gr-homework.database)

(def records-atom (atom {}))

(defn readall "Returns the current state of the record db."
  []
  @records-atom)

(defn insert! "Inserts a record into the db."
  [record]
  (swap! records-atom
         (fn [records-map rec] (assoc records-map (:email rec) rec))
         record))

(comment
  (readall)
  (insert! {:email "test@example.net"})
  (insert! {:email "test2@example.net"}))