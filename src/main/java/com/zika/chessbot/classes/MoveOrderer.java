package com.zika.chessbot.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Rank;
import com.github.bhlangonijr.chesslib.move.Move;

public class MoveOrderer {

    private static List<Move> orderMoveList(
        Board board,
        List<Move> unorderedMoves
    ){
        List<MoveStruct> moveStructs = new ArrayList<>();
        Side attacker = board.getSideToMove() == Side.WHITE ? Side.BLACK : Side.WHITE;

        for(Move move : unorderedMoves){
            int score = 0;
            Piece movingPiece = board.getPiece(move.getFrom());
            Piece capturedPiece = board.getPiece(move.getTo());
            Weights movingPieceWeigth = Weights.valueOf(board.getSideToMove()+"_"+movingPiece.getPieceType().toString());

            if(capturedPiece.getPieceType() != null){
                Weights capturedPieceWeights = Weights.valueOf(attacker+"_"+capturedPiece.getPieceType().toString());
                score += 10 * (capturedPieceWeights.getWeight() - movingPieceWeigth.getWeight());
            }

            if(
                movingPiece.getPieceType() == PieceType.PAWN &&
                (move.getTo().getRank() == Rank.RANK_1 ||
                 move.getTo().getRank() == Rank.RANK_8)
            ){
                score += Weights.WHITE_QUEEN.getWeight();
            }

            if(isSquareAttacked(move, board)) score -= movingPieceWeigth.getWeight();

            moveStructs.add(new MoveStruct(move, score));
        }


        return quickSort(moveStructs).stream().map(MoveStruct::getMove).toList();
    }

    public static List<Move> getOrderedMoves(Board board, Move bestMove){        
        List<Move> moves = new ArrayList<>();
        Predicate<Move> filter = move -> true;

        if(bestMove != null){
            moves.add(bestMove);
            filter = move -> !move.toString().equals(bestMove.toString());
        }

        moves.addAll(
            orderMoveList(board, board.legalMoves())
                .stream()
                .filter(filter)
                .toList()
        );

        return moves;
    }

    public static List<Move> getCaptures(Board board, Move bestMove){
        return orderMoveList(board, filterCaptures(board));
    }

    private static List<Move> filterCaptures(Board board){
        return board.legalMoves()
            .stream()
            .filter(move -> board.getPiece(move.getTo()) != Piece.NONE)
            .toList();
    }

    private static boolean isSquareAttacked(Move move, Board board){
        board.doMove(move);

        long attackCount = board.legalMoves()
            .stream()
            .filter(m -> m.getTo() == move.getTo())
            .count();

        board.undoMove();
        
        return attackCount > 0;
    }


    private static List<MoveStruct> quickSort(List<MoveStruct> moves){
        if(moves.size() <= 1) return moves;
        
        MoveStruct pivot = moves.get(0);
        List<MoveStruct> rigthList = new ArrayList<>();
        List<MoveStruct> leftList = new ArrayList<>();

        for(int i = 1; i < moves.size(); i++){
            if(moves.get(i).score > pivot.score) rigthList.add(moves.get(i));
            else leftList.add(moves.get(i));
        }
        
        List<MoveStruct> orderedList = new ArrayList<>(quickSort(rigthList));
        orderedList.add(pivot);
        orderedList.addAll(quickSort(leftList));

        return orderedList;
    }
    
    static class MoveStruct{
        Move move;
        int score;

        MoveStruct(Move move, int score){
            this.move = move;
            this.score = score;
        }

        Move getMove() {
            return move;
        }

        public String toString(){
            return String.format("Move {move: %s, score: %d}", this.move, this.score);
        }
    }
     
}
