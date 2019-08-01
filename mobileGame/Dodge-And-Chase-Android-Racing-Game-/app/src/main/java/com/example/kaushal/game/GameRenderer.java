package com.example.kaushal.game;

/**
 * Created by Kaushal on 1/23/2017.
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;



import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.os.SystemClock;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

import static com.example.kaushal.game.Global.context;

public class GameRenderer implements GLSurfaceView.Renderer {
String mm;int thit=0;int score=0;
    private TexRoad road = new TexRoad();
    private TexCar car = new TexCar();
    private TexCar traffic=new TexCar();
    private TexCar evil=new TexCar();
    private TexController accelerator = new TexController();
    private TexController breaks = new TexController();
int kk=1;int pop=0,push=0;
    float posy,posx;
    float p=11.0f;
    private float carSpeed2= 0.05f;
    private long loopRunTime = 0;
    private long loopStart = 0;
double xx;float xxp;
    // Car horizontal position limit in scale
    private static float carLLimit = 1.8f;
    private static float carRLimit = 3.8f;
    private static float carCenterPos = 2.8f;
    private float carCurrentPos = 2.8f;
    private float roadYOffset = 0.0f;
    private float carSpeed = 0.0f;
    double ass=Math.random();
int depo=0;


//Traffic



    double r;int flag=0,flags=1;
    private float tCurrentPos =3.8f;
    @Override
    public void onDrawFrame(GL10 gl) {
        loopStart = System.currentTimeMillis();
        try {
            if (loopRunTime < Global.GAME_THREAD_FPS_SLEEP) {
                Thread.sleep(Global.GAME_THREAD_FPS_SLEEP - loopRunTime);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);


        ScrollRoad();
        DrawRoad(gl);
        MoveCar();

        DrawCar(gl);
        mm=Integer.toString(Global.score);



if(thit==5)
{

    Intent intent = new Intent(context,Main3Activity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);

}




        if(push>38000*ass)
        {
            push=38001;
            if (flag == 0) {
               DrawEvil(gl);
            }
        }
        else
        {

            ass = Math.random();
            push++;
        }
if(pop>(3000*ass)) {
pop=3000;
    if ((ass <= 0.33d))
        DrawCar(gl, r);

    else if (ass <= 0.66d)


        DrawCar(r, gl);
    else
        DrawCar(gl, kk);
}
        else {
    p=11.0f;
    ass = Math.random();
    pop++;
}

        DrawAccel(gl);
        DrawBreaks(gl);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    }


    private void ScrollRoad(){
        switch(Global.PLAYER_ACTION){
            case Global.ACCELERATOR_PRESSED:

                if(roadYOffset < 1.0f){
                   // reset road texture position
                    roadYOffset += carSpeed;
                    if(carSpeed < 0.09f){
                        p=p-0.05f;
                        carSpeed += 0.0002f;
                    }
                }else{
                    p=p-0.15f;
                    roadYOffset -= 1.0f;
                }


                break;
            case Global.BREAKS_PRESSED:

                if(carSpeed > 0.0f){
                    roadYOffset += carSpeed;
                    carSpeed -= 0.001;
                }else{
                    carSpeed = 0.0f;
                }
                break;
            case Global.CONTROL_RELEASED:
                if(carSpeed > 0.0f){
                    roadYOffset += carSpeed;
                    carSpeed -= 0.0002;
                }else{
                    carSpeed = 0.0f;
                }
                break;
        }
    }

    public void DrawRoad(GL10 gl){
        score=Global.score;
       if((score>10)&&(flags==1))
       {
           road.loadTexture(gl, Global.ROAD3, context);
           car.loadTexture(gl, Global.CAR3, context);
           flags=0;
       }
        if((score<=10)&&(flags==0))
        {
            road.loadTexture(gl, Global.ROAD2, context);
            car.loadTexture(gl, Global.CAR2, context);
            flags=1;
        }



        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glPushMatrix();
        gl.glScalef(1f, 1f, 1f);
        gl.glTranslatef(0f, 0f, 0f);

        gl.glMatrixMode(GL10.GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, roadYOffset, 0.0f);

        road.draw(gl);
        gl.glPopMatrix();

        gl.glLoadIdentity();
    }

    private void MoveCar(){

        if(Global.PLAYER_ACTION != Global.ACCELERATOR_PRESSED) return;

        if(Global.SENSORE_ACCELEROMETER_X > 0.3 ){
            if(carCurrentPos > carLLimit){
                carCurrentPos = carCurrentPos - (float)Global.SENSORE_ACCELEROMETER_X/25;
            }else{
                carCurrentPos = carLLimit;
            }
        }else if(Global.SENSORE_ACCELEROMETER_X < -0.7 ){
            if(carCurrentPos < carRLimit){
                carCurrentPos = carCurrentPos - (float)Global.SENSORE_ACCELEROMETER_X/25;
            }else{
                carCurrentPos = carRLimit;
            }
        }else{

        }
    }


    public void DrawCar(GL10 gl){


        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glPushMatrix();
        gl.glScalef(.15f, Global.getProportionateHeight(0.15f), .15f);
        gl.glTranslatef(carCurrentPos, 1f, 0f);

        gl.glMatrixMode(GL10.GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, 0.0f);

        car.draw(gl);
        gl.glPopMatrix();

        gl.glLoadIdentity();
    }
    public void DrawEvil(GL10 gl){

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glPushMatrix();
        gl.glScalef(.15f, Global.getProportionateHeight(0.15f), .15f);
        if(depo==0) {

            depo = 1;
            xx =  Math.random();
            xxp = (float) xx*10.5f;
            if (xxp < 5f)
                posy = 6f;
            else
                posy = xxp;
            xx = Math.random();
            xxp = (float) xx*3.8f;
            if ((xxp >= 1.8f))
                posx = xxp;
            else {
                posx = xxp + 1.8f;

            }
        }
        gl.glTranslatef(posx,posy, 0f);
        if(carSpeed2<0.075f)
        carSpeed2+=0.00017f;
        if(posy<12f)
        posy+=carSpeed2-carSpeed;
        else
        {
            carSpeed2=0.05f;
            depo=0;
            push=0;
        }
       if(posy<-3f)
       {
           carSpeed2=0.05f;
           depo=0;
           push=0;
       }
        if(posx>=carCurrentPos) {
            if (((posx - carCurrentPos <= 0.9f) && (posy <= 1.5f)&&(posy>=0.2f))||((posx - carCurrentPos <= 0.9f) && (posy<1.72f)&&(posy>1f))) {
                SystemClock.sleep(800);
                carSpeed2=0.05f;
                push = 0;Global.score++;
                depo = 0;

            }
        }
        else{
                if (((carCurrentPos - posx <= 0.68f) && (posy <= 1.5f)&&(posy>=0.2f))||((carCurrentPos - posx <= 0.68f) && (posy<1.72f)&&(posy>1f))) {
                    SystemClock.sleep(800);
                    carSpeed2=0.05f;
                    depo = 0;
                    push = 0;Global.score++;

                }
            }
        gl.glMatrixMode(GL10.GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, 0.0f);

        evil.draw(gl);
        gl.glPopMatrix();

        gl.glLoadIdentity();
    }
    public void DrawCar(GL10 gl,double r1){

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glPushMatrix();
        gl.glScalef(.15f, Global.getProportionateHeight(0.15f), .15f);


        gl.glTranslatef(tCurrentPos, p, 0f);
        traffic.draw(gl);

        if(p<=0.0f) {
            pop=0;
            p = 11.0f;
            xx=Math.random();
            xxp=(float)xx*3.8f;

if((xxp>=1.8f))
            tCurrentPos=xxp;
         else {
    tCurrentPos=xxp+1.8f;

}
            ass=Math.random();
            if(ass<=0.33d)
                traffic.loadTexture(gl, Global.TRAFFIC, context);
            else if(ass<=0.66d)

                traffic.loadTexture(gl, Global.TRAFIC, context);
            else
                traffic.loadTexture(gl, Global.TRAFICC, context);
        }
        else
        p=p-0.1f;
        if(tCurrentPos>=carCurrentPos) {
            if (((tCurrentPos - carCurrentPos <= 0.9f) && (p <= 1.5f))) {
                SystemClock.sleep(1000);
pop=0;push=0;depo=0;carSpeed2=0.05f;
                if(Global.score>0)
                    Global.score--;thit++;
                xx=Math.random();
                xxp=(float)xx*3.8f;

                if((xxp>=1.8f))
                    tCurrentPos=xxp;
                else {
                    tCurrentPos=xxp+1.8f;

                }
                p = 11.0f;
                ass=Math.random();
                if(ass<=0.33d)
                    traffic.loadTexture(gl, Global.TRAFFIC, context);
                else if(ass<=0.66d)

                    traffic.loadTexture(gl, Global.TRAFIC, context);
                else
                    traffic.loadTexture(gl, Global.TRAFICC, context);

            }
        }
        else
        {
            if (((carCurrentPos - tCurrentPos <= 0.7f) && (p <= 1.5f))) {
                SystemClock.sleep(1000);
                depo=0;carSpeed2=0.05f;
                if(Global.score>0)
                    Global.score--;thit++;
                xx=Math.random();
                xxp=(float)xx*3.8f;

                if((xxp>=1.8f))
                    tCurrentPos=xxp;
                else {
                    tCurrentPos=xxp+1.8f;

                }
                pop=0;push=0;
                p = 11.0f;
                ass=Math.random();
                if(ass<=0.33d)
                    traffic.loadTexture(gl, Global.TRAFFIC, context);
                else if(ass<=0.66d)

                    traffic.loadTexture(gl, Global.TRAFIC, context);
                else
                    traffic.loadTexture(gl, Global.TRAFICC, context);

            }
        }
        gl.glMatrixMode(GL10.GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, 0.0f);


        gl.glPopMatrix();

        gl.glLoadIdentity();
    }
    public void DrawCar(GL10 gl,int r1){

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glPushMatrix();
        gl.glScalef(.15f, Global.getProportionateHeight(0.15f), .15f);


        gl.glTranslatef(tCurrentPos, p, 0f);

        traffic.draw(gl);
        if(p<=0.0f) {
            pop=0;
            p = 11.0f;
            xx=Math.random();
            xxp=(float)xx*3.8f;

            if((xxp>=1.8f))
                tCurrentPos=xxp;
            else {
                tCurrentPos=xxp+1.8f;

            }
            ass=Math.random();
            if(ass<=0.33d)
                traffic.loadTexture(gl, Global.TRAFFIC, context);
            else if(ass<=0.66d)

                traffic.loadTexture(gl, Global.TRAFIC, context);
            else
                traffic.loadTexture(gl, Global.TRAFICC, context);
        }
        else
            p=p-0.1f;
        if(tCurrentPos>=carCurrentPos) {
            if (((tCurrentPos - carCurrentPos <= 0.9f) && (p <= 1.5f))) {
                SystemClock.sleep(1000);
                if(Global.score>0)
                    Global.score--;thit++;
pop=0;push=0;depo=0;carSpeed2=0.05f;
                xx=Math.random();
                xxp=(float)xx*3.8f;

                if((xxp>=1.8f))
                    tCurrentPos=xxp;
                else {
                    tCurrentPos=xxp+1.8f;

                }
                p = 11.0f;
                ass=Math.random();
                if(ass<=0.33d)
                    traffic.loadTexture(gl, Global.TRAFFIC, context);
                else if(ass<=0.66d)

                    traffic.loadTexture(gl, Global.TRAFIC, context);
                else
                    traffic.loadTexture(gl, Global.TRAFICC, context);
            }
        }
        else
        {
            if (((carCurrentPos - tCurrentPos <= 0.7f) && (p <= 1.5f))) {

                SystemClock.sleep(1000);
pop=0;push=0;depo=0;carSpeed2=0.05f;thit++;
                if(Global.score>0)
                    Global.score--;
                xx=Math.random();
                xxp=(float)xx*3.8f;

                if((xxp>=1.8f))
                    tCurrentPos=xxp;
                else {
                    tCurrentPos=xxp+1.8f;

                }
                p = 11.0f;
                ass=Math.random();
                if(ass<=0.33d)
                    traffic.loadTexture(gl, Global.TRAFFIC, context);
                else if(ass<=0.66d)

                    traffic.loadTexture(gl, Global.TRAFIC, context);
                else
                    traffic.loadTexture(gl, Global.TRAFICC, context);

            }
        }
        gl.glMatrixMode(GL10.GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, 0.0f);


        gl.glPopMatrix();

        gl.glLoadIdentity();
    }

    public void DrawCar(double r,GL10 gl){

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glPushMatrix();
        gl.glScalef(.15f, Global.getProportionateHeight(0.15f), .15f);


        gl.glTranslatef(tCurrentPos, p, 0f);

        traffic.draw(gl);
        if(p<=0.0f) {
            pop=0;
            p = 11.0f;
            xx=Math.random();
            xxp=(float)xx*3.8f;

            if((xxp>=1.8f))
                tCurrentPos=xxp;
            else {
                tCurrentPos=xxp+1.8f;

            }
            ass=Math.random();
            if(ass<=0.33d)
                traffic.loadTexture(gl, Global.TRAFFIC, context);
            else if(ass<=0.66d)

                traffic.loadTexture(gl, Global.TRAFIC, context);
            else
                traffic.loadTexture(gl, Global.TRAFICC, context);
        }
        else
            p=p-0.1f;
        if(tCurrentPos>=carCurrentPos) {
            if (((tCurrentPos - carCurrentPos <= 0.53f) && (p <= 1.5f))) {
                SystemClock.sleep(1000);
pop=0;push=0;depo=0;carSpeed2=0.05f;thit++;
                if(Global.score>0)
                    Global.score--;
                xx=Math.random();
                xxp=(float)xx*3.8f;

                if((xxp>=1.8f))
                    tCurrentPos=xxp;
                else {
                    tCurrentPos=xxp+1.8f;

                }
                p = 11.0f;
                ass=Math.random();
                if(ass<=0.33d)
                    traffic.loadTexture(gl, Global.TRAFFIC, context);
                else if(ass<=0.66d)

                    traffic.loadTexture(gl, Global.TRAFIC, context);
                else
                    traffic.loadTexture(gl, Global.TRAFICC, context);
            }
        }
        else
        {
            if (((carCurrentPos - tCurrentPos <= 0.43f) && (p <= 1.5f))) {
                SystemClock.sleep(1000);
pop=0;push=0;depo=0;carSpeed2=0.05f;thit++;
                if(Global.score>0)
                    Global.score--;
                xx=Math.random();
                xxp=(float)xx*3.8f;

                if((xxp>=1.8f))
                    tCurrentPos=xxp;
                else {
                    tCurrentPos=xxp+1.8f;

                }
                p = 11.0f;

                ass=Math.random();
                if(ass<=0.33d)
                    traffic.loadTexture(gl, Global.TRAFFIC, context);
                else if(ass<=0.66d)

                    traffic.loadTexture(gl, Global.TRAFIC, context);
                else
                    traffic.loadTexture(gl, Global.TRAFICC, context);
            }
        }
        gl.glMatrixMode(GL10.GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, 0.0f);


        gl.glPopMatrix();

        gl.glLoadIdentity();
    }



    public void DrawAccel(GL10 gl){
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glPushMatrix();
        gl.glScalef(.25f, Global.getProportionateHeight(0.25f), .25f);
        gl.glTranslatef(3f, 0f, 0f);

        gl.glMatrixMode(GL10.GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, 0.0f);

        accelerator.draw(gl);
        gl.glPopMatrix();

        gl.glLoadIdentity();
    }


    public void DrawBreaks(GL10 gl){
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glPushMatrix();
        gl.glScalef(.25f, Global.getProportionateHeight(0.25f), .25f);
        gl.glTranslatef(0f, 0f, 0f);

        gl.glMatrixMode(GL10.GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, 0.0f);

        breaks.draw(gl);
        gl.glPopMatrix();

        gl.glLoadIdentity();
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Enable 2D maping capability
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glClearDepthf(1.0f);

        // Text depthe of all objects on surface
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);

        // Enable blend to create transperency
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        // Load textures
        car.loadTexture(gl, Global.CAR, context);

            road.loadTexture(gl, Global.ROAD2, context);



        if(ass<=0.33d)
        traffic.loadTexture(gl, Global.TRAFFIC, context);
        else if(ass<=0.66d)

        traffic.loadTexture(gl, Global.TRAFIC, context);
        else
        traffic.loadTexture(gl, Global.TRAFICC, context);
        accelerator.loadTexture(gl, Global.ACCELERATOR, context);
        breaks.loadTexture(gl, Global.BREAKS, context);
        evil.loadTexture(gl,Global.EVIL, context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        // Enable game screen width and height to access other functions and classes
        Global.GAME_SCREEN_WIDTH = width;
        Global.GAME_SCREEN_HEIGHT = height;

        // set position and size of viewport
        gl.glViewport(0, 0, width, height);

        // Put OpenGL to projectiong matrix to access glOrthof()
        gl.glMatrixMode(GL10.GL_PROJECTION);

        // Load current identity of OpenGL state
        gl.glLoadIdentity();

        // set orthogonal two dimensional rendering of scene
        gl.glOrthof(0f, 1f, 0f, 1f, -1f, 1f);
    }

}

