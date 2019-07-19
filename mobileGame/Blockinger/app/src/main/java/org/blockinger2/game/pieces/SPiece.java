package org.blockinger2.game.pieces;

import android.content.Context;

import org.blockinger2.game.engine.Square;

public class SPiece extends Piece3x3
{
    private Square sSquare;

    public SPiece(Context context)
    {
        super(context);
        sSquare = new Square(Piece.TYPE_S, context);
        pattern[1][1] = sSquare;
        pattern[1][2] = sSquare;
        pattern[2][0] = sSquare;
        pattern[2][1] = sSquare;
        reDraw();
    }

    @Override
    public void reset(Context context)
    {
        super.reset(context);

        pattern[1][1] = sSquare;
        pattern[1][2] = sSquare;
        pattern[2][0] = sSquare;
        pattern[2][1] = sSquare;

        reDraw();
    }
}
