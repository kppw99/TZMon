package com.example.kaushal.game;

/**
 * Created by Kaushal on 1/23/2017.
 */
public class TexController extends Texture {

    private static float texture[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
    };

    public TexController(){
        super(texture);
    }

}

