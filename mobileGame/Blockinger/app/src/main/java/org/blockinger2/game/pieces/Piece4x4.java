package org.blockinger2.game.pieces;

import android.content.Context;

import org.blockinger2.game.components.Board;
import org.blockinger2.game.engine.Square;

public abstract class Piece4x4 extends Piece
{
    Piece4x4(Context context)
    {
        super(context, 4);
    }

    @Override
    public void turnLeft(Board board)
    {
        int maxLeftOffset = -4;
        int maxRightOffset = -4;
        int maxBottomOffset = -4;
        int leftOffset;
        int rightOffset;
        int bottomOffset;
        Square backup[][];
        // [0][0] ... [0][3]
        //  ....       ....
        // [3][0] ... [3][3]
        rotated[0][0] = pattern[0][3];
        rotated[0][3] = pattern[3][3];
        rotated[3][3] = pattern[3][0];
        rotated[3][0] = pattern[0][0];

        rotated[0][1] = pattern[1][3];
        rotated[1][3] = pattern[3][2];
        rotated[3][2] = pattern[2][0];
        rotated[2][0] = pattern[0][1];

        rotated[0][2] = pattern[2][3];
        rotated[2][3] = pattern[3][1];
        rotated[3][1] = pattern[1][0];
        rotated[1][0] = pattern[0][2];

        rotated[1][1] = pattern[1][2];
        rotated[1][2] = pattern[2][2];
        rotated[2][2] = pattern[2][1];
        rotated[2][1] = pattern[1][1];

        // Check for border violations and collisions
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (rotated[i][j] != null) {
                    leftOffset = -(x + j);
                    rightOffset = (x + j) - (board.getWidth() - 1);
                    bottomOffset = (y + i) - (board.getHeight() - 1);
                    if (!rotated[i][j].isEmpty()) // Left border violation
                    {
                        if (maxLeftOffset < leftOffset) {
                            maxLeftOffset = leftOffset;
                        }
                    }

                    if (!rotated[i][j].isEmpty()) {
                        // Right border violation
                        if (maxRightOffset < rightOffset) {
                            maxRightOffset = rightOffset;
                        }
                    }

                    if (!rotated[i][j].isEmpty()) {
                        // Bottom border violation
                        if (maxBottomOffset < bottomOffset) {
                            maxBottomOffset = bottomOffset;
                        }
                    }

                    if (board.get(x + j, y + i) != null) {
                        if (!rotated[i][j].isEmpty() && !board.get(x + j, y + i).isEmpty()) // collision
                        {
                            return;
                        }
                    }
                }
            }
        }

        backup = pattern;
        pattern = rotated;
        rotated = backup;

        // Try to correct border violations
        if (maxBottomOffset < 1) {
            if (maxLeftOffset < 1) {
                if (maxRightOffset < 1) {
                    reDraw();
                } else {
                    if (setPosition(x - maxRightOffset, y, false, board)) {
                        reDraw();
                    } else {
                        rotated = pattern;
                        pattern = backup;
                    }
                }
            } else {
                if (setPosition(x + maxLeftOffset, y, false, board)) {
                    reDraw();
                } else {
                    rotated = pattern;
                    pattern = backup;
                }
            }
        } else {
            if (setPosition(x, y - maxBottomOffset, false, board)) {
                reDraw();
            } else {
                rotated = pattern;
                pattern = backup;
            }
        }
    }

    @Override
    public void turnRight(Board board)
    {
        int maxLeftOffset = -4;
        int maxRightOffset = -4;
        int maxBottomOffset = -4;
        int leftOffset;
        int rightOffset;
        int bottomOffset;
        Square backup[][];
        // [0][0] ... [0][3]
        //  ....       ....
        // [3][0] ... [3][3]
        rotated[0][3] = pattern[0][0];
        rotated[3][3] = pattern[0][3];
        rotated[3][0] = pattern[3][3];
        rotated[0][0] = pattern[3][0];

        rotated[1][3] = pattern[0][1];
        rotated[3][2] = pattern[1][3];
        rotated[2][0] = pattern[3][2];
        rotated[0][1] = pattern[2][0];

        rotated[2][3] = pattern[0][2];
        rotated[3][1] = pattern[2][3];
        rotated[1][0] = pattern[3][1];
        rotated[0][2] = pattern[1][0];

        rotated[1][2] = pattern[1][1];
        rotated[2][2] = pattern[1][2];
        rotated[2][1] = pattern[2][2];
        rotated[1][1] = pattern[2][1];

        // Check for border violations and collisions
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (rotated[i][j] != null) {
                    leftOffset = -(x + j);
                    rightOffset = (x + j) - (board.getWidth() - 1);
                    bottomOffset = (y + i) - (board.getHeight() - 1);

                    if (!rotated[i][j].isEmpty()) {
                        // Left border violation
                        if (maxLeftOffset < leftOffset) {
                            maxLeftOffset = leftOffset;
                        }
                    }

                    if (!rotated[i][j].isEmpty()) {
                        // Right border violation
                        if (maxRightOffset < rightOffset) {
                            maxRightOffset = rightOffset;
                        }
                    }

                    if (!rotated[i][j].isEmpty()) {
                        // Bottom border violation
                        if (maxBottomOffset < bottomOffset) {
                            maxBottomOffset = bottomOffset;
                        }
                    }

                    if (board.get(x + j, y + i) != null) {
                        if (!rotated[i][j].isEmpty() && !board.get(x + j, y + i).isEmpty()) // Collision
                        {
                            return;
                        }
                    }
                }
            }
        }

        backup = pattern;
        pattern = rotated;
        rotated = backup;

        // Try to correct border violations
        if (maxBottomOffset < 1) {
            if (maxLeftOffset < 1) {
                if (maxRightOffset < 1) {
                    reDraw();
                } else {
                    if (setPosition(x - maxRightOffset, y, false, board)) {
                        reDraw();
                    } else {
                        rotated = pattern;
                        pattern = backup;
                    }
                }
            } else {
                if (setPosition(x + maxLeftOffset, y, false, board)) {
                    reDraw();
                } else {
                    rotated = pattern;
                    pattern = backup;
                }
            }
        } else {
            if (setPosition(x, y - maxBottomOffset, false, board)) {
                reDraw();
            } else {
                rotated = pattern;
                pattern = backup;
            }
        }
    }
}
