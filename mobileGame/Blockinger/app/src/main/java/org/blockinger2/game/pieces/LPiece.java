package org.blockinger2.game.pieces;

import android.content.Context;

import org.blockinger2.game.engine.Square;

public class LPiece extends Piece3x3
{
    private Square lSquare;

    public LPiece(Context canvas)
    {
        super(canvas);

        lSquare = new Square(Piece.TYPE_L, canvas);
        pattern[1][0] = lSquare;
        pattern[1][1] = lSquare;
        pattern[1][2] = lSquare;
        pattern[2][0] = lSquare;

        reDraw();
    }

    @Override
    public void reset(Context canvas)
    {
        super.reset(canvas);

        pattern[1][0] = lSquare;
        pattern[1][1] = lSquare;
        pattern[1][2] = lSquare;
        pattern[2][0] = lSquare;

        reDraw();
    }
}
