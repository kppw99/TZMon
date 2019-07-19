package org.blockinger2.game.pieces;

import android.content.Context;

import org.blockinger2.game.engine.Square;

public class JPiece extends Piece3x3
{
    private Square jSquare;

    public JPiece(Context context)
    {
        super(context);

        jSquare = new Square(Piece.TYPE_J, context);
        pattern[1][0] = jSquare;
        pattern[1][1] = jSquare;
        pattern[1][2] = jSquare;
        pattern[2][2] = jSquare;

        reDraw();
    }

    @Override
    public void reset(Context context)
    {
        super.reset(context);

        pattern[1][0] = jSquare;
        pattern[1][1] = jSquare;
        pattern[1][2] = jSquare;
        pattern[2][2] = jSquare;

        reDraw();
    }
}
