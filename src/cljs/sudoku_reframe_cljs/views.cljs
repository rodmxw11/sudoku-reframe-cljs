(ns sudoku-reframe-cljs.views
  (:require
   [re-frame.core :as re-frame]
   [sudoku-reframe-cljs.subs :as subs]
   [sudoku-reframe-cljs.sudoku-board :as sudoku]
   ))


(defn display-single-digit
  [cell-set]
  (apply str (disj cell-set sudoku/LOCKED-FLAG sudoku/INITIALIZED-FLAG))
  )

(defn display-cell-digits
  [ri ci sorted-cell-set]
  (vec
  (for [i sudoku/digits-range]
    (if
      (sorted-cell-set i)
      [:div.cell-grid-empty]
      [:div.cell-grid-digit (str i)]
      )) ) ;; todo: make this clickable
  )

(defn display-cell
  [ri ci sorted-cell-set]
  (cond
    (sorted-cell-set sudoku/ERROR-FLAG)
    [:div.error-cell.cell  "ERROR"]

    (sorted-cell-set sudoku/INITIALIZED-FLAG)
    [:div.init-cell.cell [display-single-digit sorted-cell-set]]

    (sorted-cell-set sudoku/LOCKED-FLAG)
    [:div.locked-cell.cell [display-single-digit sorted-cell-set]]

    :else
    ;; [:div.cell]
    [:div.cell-grid.cell [display-cell-digits ri ci sorted-cell-set]]
    )
  )

(defn display-board
  [board]
  [:div.board-grid
   (for
     [ri sudoku/DIM-RANGE ci sudoku/DIM-RANGE]
     [display-cell  ri ci (get-in board [ri ci])] ;; todo: key>
     )
   ]
  )

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1 "Hello from " @name]
     [display-board sudoku/sudoku-2]
     ]))
