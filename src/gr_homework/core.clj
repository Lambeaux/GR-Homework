(ns gr-homework.core
  (:use [ring.adapter.jetty])
  (:gen-class))

(defn handler [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(defn -main [& args]
  (if (seq args)
    (doseq [arg args]
      (println arg))
    (println "No args provided")))

(comment
  (-main)
  (-main "arg1")
  (-main "arg1" "arg2")
  (run-jetty handler {:port  3000
                      :join? false}))