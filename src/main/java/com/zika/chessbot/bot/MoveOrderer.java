package com.zika.chessbot.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.zika.chessbot.bot.utils.BoardUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MoveOrderer {
    private static final int MATE_SCORE = 100_000_000;
    private static final int KILLER_MOVE_SCORE = 10_000;
    private final BoardEvaluator boardEvaluator;

    private List<Move> orderMoveList(
        Board board,
        List<Move> unorderedMoves,
        KillerMoves killerMoves,
        Integer depth
    ){
        List<ScoredMove> scoredMoves = new ArrayList<>();

        boolean printar = false;

        for(Move move : unorderedMoves) {
            if(killerMoves != null && killerMoves.isKillerMove(move, depth)) {
                scoredMoves.add(new ScoredMove(move, KILLER_MOVE_SCORE));
                continue;
            }

            if(BoardUtils.isMateMove(board, move)) {
                if(
                    move.getTo() == Square.D1
                    && move.getFrom() == Square.D6
                    && board.getPiece(move.getFrom()) == Piece.BLACK_QUEEN
                ) {
                    printar = true;
                    //System.out.println("não fui esquecido haha " + board.getSideToMove());
                }

                scoredMoves.add(new ScoredMove(move, MATE_SCORE));
                continue;
            }

            int score = BoardUtils.isCaptureMove(move, board)
                    ? boardEvaluator.evaluateCapture(move, board)
                    : boardEvaluator.evaluateQuietMove(move, board);

            scoredMoves.add(new ScoredMove(move, score));
        }

        List<Move> orderedMoves = quickSort(scoredMoves).stream().map(ScoredMove::move).toList();

        if(printar) {
            //System.out.println(orderedMoves);
        }

        return orderedMoves;
    }

    public List<Move> getOrderedMoves(Board board, Move bestMove, KillerMoves killerMoves, Integer depth) {
        List<Move> moves = new ArrayList<>();
        Predicate<Move> filter = move -> true;

        if(bestMove != null){
            moves.add(bestMove);
            filter = move -> !move.toString().equals(bestMove.toString());
        }

        moves.addAll(
            orderMoveList(board, board.legalMoves(), killerMoves, depth)
                .stream()
                .filter(filter)
                .toList()
        );

        return moves;
    }

    public List<Move> getCaptures(Board board, Move bestMove) {
        return orderMoveList(board, filterCaptures(board), null, null);
    }

    private List<Move> filterCaptures(Board board){
        return board.legalMoves()
            .stream()
            .filter(move -> board.getPiece(move.getTo()) != Piece.NONE)
            .toList();
    }

    private List<ScoredMove> quickSort(List<ScoredMove> moves){
        if(moves.size() <= 1) return moves;
        
        ScoredMove pivot = moves.get(0);
        List<ScoredMove> rigthList = new ArrayList<>();
        List<ScoredMove> leftList = new ArrayList<>();

        for(int i = 1; i < moves.size(); i++){
            if(moves.get(i).score() > pivot.score()) rigthList.add(moves.get(i));
            else leftList.add(moves.get(i));
        }
        
        List<ScoredMove> orderedList = new ArrayList<>(quickSort(rigthList));
        orderedList.add(pivot);
        orderedList.addAll(quickSort(leftList));

        return orderedList;
    }
}
