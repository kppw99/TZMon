package org.blockinger2.game.pieces;

import android.content.Context;

import org.blockinger2.game.engine.Square;

public class OPiece extends Piece4x4
{
    private Square oSquare;

    public OPiece(Context canvas)
    {
        super(canvas);

        oSquare = new Square(Piece.TYPE_O, canvas);
        pattern[1][1] = oSquare;
        pattern[1][2] = oSquare;
        pattern[2][1] = oSquare;
        pattern[2][2] = oSquare;

        reDraw();
    }

    @Override
    public void reset(Context canvas)
    {
        super.reset(canvas);

        pattern[1][1] = oSquare;
        pattern[1][2] = oSquare;
        pattern[2][1] = oSquare;
        pattern[2][2] = oSquare;

        reDraw();
    }
}
