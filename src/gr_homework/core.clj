(ns gr-homework.core
  (:gen-class))

(defn -main [& args]
  (if (seq args)
    (doseq [arg args]
      (println arg))
    (println "No args provided")))

(comment
  (-main)
  (-main "arg1")
  (-main "arg1" "arg2"))