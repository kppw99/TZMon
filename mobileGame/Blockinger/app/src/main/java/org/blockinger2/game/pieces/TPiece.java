package org.blockinger2.game.pieces;

import android.content.Context;

import org.blockinger2.game.engine.Square;

public class TPiece extends Piece3x3
{
    private Square tSquare;

    public TPiece(Context context)
    {
        super(context);

        tSquare = new Square(Piece.TYPE_T, context);
        pattern[1][0] = tSquare;
        pattern[1][1] = tSquare;
        pattern[1][2] = tSquare;
        pattern[2][1] = tSquare;

        reDraw();
    }

    @Override
    public void reset(Context context)
    {
        super.reset(context);

        pattern[1][0] = tSquare;
        pattern[1][1] = tSquare;
        pattern[1][2] = tSquare;
        pattern[2][1] = tSquare;

        reDraw();
    }
}
