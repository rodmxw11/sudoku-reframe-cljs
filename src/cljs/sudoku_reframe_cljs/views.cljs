(ns sudoku-reframe-cljs.views
  (:require
   [re-frame.core :as re-frame]
   [sudoku-reframe-cljs.subs :as subs]
   [sudoku-reframe-cljs.sudoku-board :as sudoku]
   ))

(enable-console-print!)

(defn display-single-digit
  [cell-set]
  (apply str (disj cell-set sudoku/LOCKED-FLAG sudoku/INITIALIZED-FLAG))
  )

(defn display-cell-digits
  [lcl-style ri ci sorted-cell-set]
  [:div.cell-grid.cell lcl-style
   (for [i sudoku/digits-range]
    (if
      (sorted-cell-set i)
      [:div.cell-grid-empty {:key i}]
      [:div.cell-grid-digit {:key i} (str i)] ;; todo: make this clickable
      )
    )]
  )

(defn bg-color [ri ci]
  (let [
        box-row (quot ri 3)
        box-col (quot ci 3)
        even-box-row (rem box-row 2)
        even-box-col (rem box-col 2)
        color (if (= even-box-row even-box-col) "white" "LightGray")
        ]
    color
    )
  )

(defn display-cell
  [ri ci sorted-cell-set]
  (let [
        right-border (if (#{2 5} ci) "3px " "1px ")
        bottom-border (if (#{2 5} ri) "3px " "1px ")
        lcl-style {:key (str "cell-" ri ci) :style {
                    :border-width (str "1px " right-border bottom-border "1px")
                           :background-color (bg-color ri ci)
                           }}
        ]
  (cond
    (sorted-cell-set sudoku/ERROR-FLAG)
    [:div.error-cell.cell.single-digit  lcl-style "ERROR"]

    (sorted-cell-set sudoku/INITIALIZED-FLAG)
    [:div.init-cell.cell.single-digit lcl-style [display-single-digit sorted-cell-set]]

    (sorted-cell-set sudoku/LOCKED-FLAG)
    [:div.locked-cell.cell.single-digit lcl-style [display-single-digit sorted-cell-set]]

    :else
    ;; [:div.cell]
    [display-cell-digits lcl-style ri ci sorted-cell-set]
    ))
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
