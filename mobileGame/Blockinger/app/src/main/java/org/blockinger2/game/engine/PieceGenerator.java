package org.blockinger2.game.engine;

import java.util.Random;

public class PieceGenerator
{
    private int bag[];
    private int bagPointer;
    private Random random;

    public PieceGenerator()
    {
        bag = new int[7];

        // Initial permutation
        for (int i = 0; i < 7; i++) {
            bag[i] = i;
        }

        random = new Random(System.currentTimeMillis());

        // Fill initial bag
        randomizeBag();

        bagPointer = 0;
    }

    public int next()
    {
        if (bagPointer < 7) {
            bagPointer++;

            return bag[bagPointer - 1];
        }

        // Randomize Bag
        randomizeBag();

        bagPointer = 1;

        return bag[bagPointer - 1];
    }

    private void randomizeBag()
    {
        for (int i = 0; i < 6; i++) {
            int c = random.nextInt(7 - i);
            int t = bag[i];

            bag[i] = bag[i + c];
            bag[i + c] = t; // Swap
        }
    }
}
