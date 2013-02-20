(ns scanner.test-description)


(test-case
 (rotation x {:degrees 10
              :duration 3.5
              :start-time 1.2
              :radius 19})
 (rotation y none)
 (rotation z none))
=> ;; Code
(process-test dataset {:start-time 1.2
                       :duration 3.5
                       :radius 19
                       :x-rotation 10
                       :y-rotation 0
                       :z-rotation 0})
=> ;; Output
{:x-translation {:pre-test {:RMS-error 2.6
                            :end-expected 0
                            :end-actual 4}
                 :test {:RMS-error 2.6
                        :end-expected 10
                        :end-actual 14}
                 :post-test {:RMS-error 2.6
                             :end-expected 0
                             :end-actual 2.7}}
 :y-translation {}
 :z-translation {}
 :x-rotation {}
 :y-rotation {}
 :z-rotation {}}
;;;;;;;;;;;;;;;;;
;; Second case
;;;;;;;;;;;;;;;;;
(test-case
 (translation x {:inches 10
                 :duration 2.34
                 :start-time 4.2})
 (translation y none)
 (translation z none)
 (rotation x none)
 (rotation y none)
 (rotation z none))

=> ;; Code


=> ;; Output
{:x-translation {:pre-test {:RMS-error 2.6
                            :end-expected 0
                            :end-actual 4}
                 :test {:RMS-error 2.6
                        :end-expected 10
                        :end-actual 14}
                 :post-test {:RMS-error 2.6
                             :end-expected 0
                             :end-actual 2.7}}
 :y-translation {:pre-test {:RMS-error 2.6
                            :end-expected 0
                            :end-actual 4}
                 :test {:RMS-error 2.6
                        :end-expected 10
                        :end-actual 14}
                 :post-test {:RMS-error 2.6
                             :end-expected 0
                             :end-actual 2.7}}
 :z-translation {}
 :x-rotation {}
 :y-rotation {}
 :z-rotation {}}