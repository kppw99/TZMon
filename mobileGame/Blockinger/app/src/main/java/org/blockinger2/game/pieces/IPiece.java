package org.blockinger2.game.pieces;

import android.content.Context;

import org.blockinger2.game.engine.Square;

public class IPiece extends Piece4x4
{
    private Square iSquare;

    public IPiece(Context context)
    {
        super(context);

        iSquare = new Square(Piece.TYPE_I, context);
        pattern[2][0] = iSquare;
        pattern[2][1] = iSquare;
        pattern[2][2] = iSquare;
        pattern[2][3] = iSquare;

        reDraw();
    }

    @Override
    public void reset(Context context)
    {
        super.reset(context);

        pattern[2][0] = iSquare;
        pattern[2][1] = iSquare;
        pattern[2][2] = iSquare;
        pattern[2][3] = iSquare;

        reDraw();
    }
}
