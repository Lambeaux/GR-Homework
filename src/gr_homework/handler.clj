(ns gr-homework.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.request :as ring])
  (:import (java.text SimpleDateFormat ParseException)))

;; --------------------------------------------------------------------------
;; CORE
;; --------------------------------------------------------------------------

(def date-format-str "MM/dd/yyyy")
(def both #(and %1 %2))

(def attr-order [:last-name :first-name :email :fav-color :dob])

(def delimiters {"csv" (re-pattern ",")
                 "psv" (re-pattern "\\|")
                 "ssv" (re-pattern " ")})

(defn- valid-string? [attr]
  (and (string? (last attr)) (not (str/blank? (last attr)))))

(defn- valid-date? [attr]
  (let [date-format (SimpleDateFormat. date-format-str)
        date-str (last attr)]
    (try
      (.parse date-format date-str)
      true
      (catch ParseException e false))))

(defmulti valid-attr? "Returns true if an attr is valid." #(first %))
(defmethod valid-attr? :default [attr] (valid-string? attr))
(defmethod valid-attr? :dob [attr] (reduce both ((juxt valid-string? valid-date?) attr)))

(defn- params->map [params]
  (->> params
       (map str/trim)
       (interleave attr-order)
       (partition 2)
       (map vec)
       (into {})))

(defn- create-parse-report [params]
  (if (not= (count attr-order) (count params))
    {:params params
     :is-valid? false
     :error  (str "Incorrect number of parameters, expecting 5 but got " (count params))}
    (let [date-format (SimpleDateFormat. date-format-str)
          record (params->map params)
          dob (:dob record)
          validity (->> record
                        seq
                        (map #(vector (first %) (valid-attr? %)))
                        (into {}))
          is-valid? (->> validity vals (reduce both))]
      {:params    params
       :is-valid? is-valid?
       :record    (if is-valid? (assoc record :dob (.parse date-format dob)) record)
       :validity  validity})))

(defn- parse-first-record [ext lines]
  (let [delim (get delimiters ext)
        line (first lines)]
    (if (nil? delim)
      {:is-valid? false
       :error (str "Invalid extension provided: " ext)}
      (create-parse-report (str/split line delim)))))

(defn- parse-lines [ext lines]
  ;; TODO
  ())

(comment
  (valid-string? [:key nil])
  (valid-string? [:key ""])
  (valid-string? [:key "string"])
  (valid-date? [:key "not a date"])
  (valid-date? [:key "01/15/2020"])
  (params->map ["last" "first" "me@ex.net" "blue" "date"])
  (create-parse-report ["last" "" "me@ex.net" "" "date"])
  (create-parse-report ["last" "first" "me@ex.net" "blue" "date"])
  (create-parse-report ["last" "first" "me@ex.net" "blue" "01/15/2020"]))

;; --------------------------------------------------------------------------
;; HTTP
;; --------------------------------------------------------------------------

(defroutes app-routes
           (GET "/" [] "Hello Worlds!")
           (POST "/records" [:as req] (str "hi " (ring/body-string req)))
           (route/not-found "Not Found"))

(def app
  (wrap-defaults
    app-routes
    (-> site-defaults
        (update-in [:security :anti-forgery] (constantly false)))))
