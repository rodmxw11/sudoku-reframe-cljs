;; sun19apr2020 -- playing around with sudoku data structure
(ns sudoku-reframe-cljs.sudoku-board)

(def ^:const
  "Sudoku board dimension is 9x9"
  DIM 9)

(def all-digits-set
  "Initial content of a Sudoku cell is a Set of digits 1-9"
  (into #{} (range 1 (inc DIM))))

(def empty-row
  "A sudoku row is 9 cells"
  (vec (repeat DIM all-digits-set)))

(def empty-board
  "A sudoku board is 9 rows"
  (vec (repeat DIM empty-row)))

(defn cell-seq
  "Flatten a board into a seq of cell sets"
  [board]
  (for [row board cell row] cell))

(defn board-solved?
  "true if board is solved; A sudoku board is solved if every cell set contains the :locked keyword"
  [board]
  (every? #(% :locked) (cell-seq board)))

(defn board-error?
  "true if any board cell contains :error keyword"
  [board]
  (not-any? #(% :error) (cell-seq board)))

(defn lock-single-digit
  "set a :locked digit in a single cell"
  ([board ri ci digit] (lock-single-digit board ri ci false)) ;; not initializing a cell
  ([board ri ci digit init?]
  (assoc-in
    board                              ;;  update board cell
    [ri ci]                            ;; at these coords
    (if-not
      ((get-in board [ri ci]) digit)   ;; if digit is NOT in cell set
      #{:error}                        ;; THEN signal cell error
      (if
        init?                             ;; ELSE if initializing board
        #{:init :locked digit}            ;;      then lock that digit in cell and mark initialized
        #{:locked digit}                  ;;      else just lock that digit in cell
        )
      ))))


(def ^:const block-indices
  "Row or column indices of cells within a 3x3 block"
  [
    #{ 0 1 2} ;; block 0
    #{ 3 4 5} ;; block 1
    #{ 6 7 8} ;; block 2
    ])


(defn lock-cell
  "lock a digit in a cell and then remove that digit from other cells in the same row, column, and block"
  ([ri ci digit board] (lock-cell false ri ci digit board))
  ([init? ri ci digit board]
  (let [
         row-block-set (block-indices (quot ri 3))  ;; row indices of block containing ri
         col-block-set (block-indices (quot ci 3))  ;; col indices of block containing ci
         maybe-disj (fn [cell-set digit]     ;; maybe remove a digit from a cell set
                      (if
                        (cell-set :locked)  ;; if cell-set is locked
                        cell-set            ;; don't change it
                        (let [new-set (disj cell-set digit)]  ;; remove digit from cell-set
                          (if (empty? new-set) #{:error} new-set) ;; check for empty cell-set
                        )))
         ]
    (vec (map-indexed
           (fn [row-j row]   ;; for each row in new board ...
             (cond
               (= ri row-j) ;; if row-j==ri
               (mapv #(maybe-disj % digit)  row) ;; then remove digit from every cell in this row

               (row-block-set row-j)  ;; if ri is a row inside of target block
               (vec (map-indexed (fn [col-j cell] ;; for every cell in this row
                                  (if-not
                                    (col-block-set col-j)  ;; if the cell is not in the target block
                                    cell                   ;; then don't change the cell
                                    (maybe-disj col digit))) ;; else remove digit from cell-set
                                 row))

               :else  ;; default: this is a row that is not in the target block
               (update-in row [ci] maybe-disj digit)  ;; then just remove digit from the cell in the specified column
               ))
           (lock-single-digit board ri ci digit init?) ;; new board with digit locked into cell at ri,ci
           )))))

(def ^:const char-to-digit
  "map of a character to a cell digit"
  {
    \. 0
    \0 0
    \1 1
    \2 2
    \3 3
    \4 4
    \5 5
    \6 6
    \7 7
    \8 8
    \9 9
    }
  )

(defn find-init-cells [init-str]
  "Converts an 81 char sudoku string, with possible white space into a seq of [ri ci digit] tuples"
  (
    ->>
    init-str               ;; 81 char sudoku string with optional whitespace
    (map char-to-digit)    ;; convert to digits or nil
    (remove nil?)          ;; remove whitespace
    (map-indexed           ;; for each digit ...
        (fn [i digit]
          (if (zero? digit)  ;; map zero digit to nill
            nil
            [(quot i DIM) (rem i DIM) digit] ;; map non-zero digit to [row col digit] tuple
            )))
    (remove nil?)          ;; remove non-digit entries
    ))


(defn init-board
  "initialize a sudoku board from a 81 char sudoku string"
  [init-str]
  (reduce
     (fn [board [ri ci digit]]
         (lock-cell true ri ci digit board))  ;; initialize a cell from the sudoku string
     empty-board                ;; start with an empty sudoku board
     (find-init-cells init-str) ;; seq of [ri ci digit] tuples from the 81 char sudoku string
  ))


(def sudoku-0 "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4......")


(def sudoku-1 "
400000805
030000000
000700000
020000060
000080400
000010000
000603070
500200000
104000000" )

(def sudoku-2 (init-board sudoku-1))

(defn flatten-board [board]
  (for [ri (range DIM) ci (range DIM)] [ri ci (get-in board [ri ci])]))

;(remove (fn [[ri ci cell-set]] (or (cell-set :locked) (> (count cell-set) 3)))
;        (flatten-board sudoku-2)
;        )


;(->>
;  sudoku-1
;  init-board
;  (lock-cell 6 1 8) ;; 8 9
;  (lock-cell 6 0 2) ;; 2 9
;  (lock-cell 6 2 9)
;  (lock-cell 6 4 4) ;; 4 5
;  (lock-cell 6 8 1)
;  (lock-cell 6 6 5)
;  flatten-board
;  (remove (fn [[ri ci cell-set]] (or (cell-set :locked) #_(> (count cell-set) 3))))
;  (sort #(compare (count (%1 2)) (count (%2 2))))
;  )


;;(tree-seq
;;  branch?
;;  children
;;  root
;;  )

;; 1. naked single
;; 2. hidden single -- Intersect Union row|col|block is singleton
;; 3. naked pair -- two cell-sets equal in same row|col|block -- eliminate digit pair from other cells in same row|col|block