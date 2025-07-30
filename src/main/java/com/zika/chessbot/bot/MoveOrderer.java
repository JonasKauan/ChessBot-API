package com.zika.chessbot.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;
import com.zika.chessbot.bot.utils.BoardUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MoveOrderer {
    private final BoardEvaluator boardEvaluator;

    private List<Move> orderMoveList(
        Board board,
        List<Move> unorderedMoves,
        KillerMoves killerMoves,
        Integer depth
    ){
        List<ScoredMove> scoredMoves = new ArrayList<>();

        for(Move move : unorderedMoves) {
            if(BoardUtils.captureMove(move, board)) {
                scoredMoves.add(new ScoredMove(move, boardEvaluator.evaluateCapture(move, board)));
                continue;
            }

            if(killerMoves != null && killerMoves.isKillerMove(move, depth)) {
                scoredMoves.add(new ScoredMove(move, 10_000));
                continue;
            }

            scoredMoves.add(new ScoredMove(move, boardEvaluator.evaluateQuietMove(move, board)));
        }

        return quickSort(scoredMoves).stream().map(ScoredMove::move).toList();
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
