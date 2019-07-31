package com.breezesoftware.skyrim2d;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.breezesoftware.skyrim2d.entity.Arrow;
import com.breezesoftware.skyrim2d.entity.Enemy;
import com.breezesoftware.skyrim2d.entity.Player;
import com.breezesoftware.skyrim2d.level.LevelManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 21.08.2018.
 */
public class GameView extends SurfaceView {
    private static final int PLAYER_X_OFFSET = 50;

    public Player player;

    public List<Enemy> enemies;

    private boolean isGameOver = false;

    private ConstraintLayout gameOverOverlay;
    private TextView levelLabel;
    private TextView monstersLabel;
    private TextView goldLabel;

    private int killCount;

    private LevelManager levelManager;

    private int canvasWidth = 0;
    private int canvasHeight = 0;

    private MediaPlayer goldDroppedSound;

    public void updateUI() {
        if (this.levelLabel != null) {
            this.levelLabel.setText(String.format("Level %d", levelManager.getCurrentLevel()));
        }

        if (this.monstersLabel != null) {
            this.monstersLabel.setText(String.format("Monsters: %d", levelManager.getCurrentLevelMonsterCount()));
        }

        if (this.goldLabel != null) {
            this.goldLabel.setText(String.format("%d", player.getGold()));
        }
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setMonstersLabel(TextView monstersLabel) {
        this.monstersLabel = monstersLabel;
    }

    public void setLevelLabel(TextView levelLabel) {
        this.levelLabel = levelLabel;
    }

    public void setGameOverOverlay(ConstraintLayout gameOverOverlay) {
        this.gameOverOverlay = gameOverOverlay;
    }

    public void setGoldLabel(TextView goldLabel) {
        this.goldLabel = goldLabel;
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.initSounds();
    }

    public void initLevelManager() {
        levelManager = new LevelManager(getContext());
    }

    public void initSounds() {
        // BAD API HACK
        // TODO: Rework this with a factory
        Enemy enemy = new Enemy(getContext(), 0, 0, "Monster", R.drawable.monster, 0, 0, 1.0f);
        enemy.addDiedSound(R.raw.orc_dead);
        enemy.addHurtSound(R.raw.orc_damaged_1);
        enemy.addHurtSound(R.raw.orc_damaged_2);

        goldDroppedSound = MediaPlayer.create(getContext(), R.raw.coin_drop);
    }

    public void startGame() {
        this.levelManager.setCurrentLevel(0);
        this.spawnPlayer();
        resetGame();
    }

    public void spawnEnemies() {
        this.enemies = levelManager.getEnemies();
    }

    public void resetGame() {
        this.isGameOver = false;

        if (this.gameOverOverlay != null) {
            this.gameOverOverlay.setVisibility(INVISIBLE);
        }

        this.killCount = 0;

        this.spawnEnemies();
    }

    public void update() {
        //Log.d("GameView", "update");
        if (isGameOver) {
            return;
        }

        checkGameOver();

        this.player.update();
    }

    private void checkGameOver() {
        if (killCount == levelManager.getCurrentLevelMonsterCount()) {
            levelManager.nextLevel();

            if (levelManager.isLastLevel()) {
                this.isGameOver = true;
            } else {
                this.resetGame();
            }
        }
    }

    private void onEnemyDead(Enemy enemy) {
        killCount++;
        int gold = enemy.getGold();

        if (gold != 0) {
            goldDroppedSound.start();
        }

        player.addGold(enemy.getGold());
    }

    private void checkEnemyHit(Enemy enemy) {
        List<Arrow> toDelete = new ArrayList<>(player.getArrows().size());

        for (int i = 0; i < player.getArrows().size(); i++) {
            Arrow arrow = player.getArrows().get(i);

            if (enemy.intersectsWith(arrow)) {
                enemy.hurt(arrow.getDamage());
                toDelete.add(arrow);

                if (enemy.isDead()) {
                    onEnemyDead(enemy);
                }
            }
        }

        for (Arrow arrow : toDelete) {
            player.removeArrow(arrow);
        }
    }

    public void updateEnemies(Canvas canvas) {
        for (Enemy enemy : this.enemies) {

            checkEnemyHit(enemy);

            enemy.update();

            if (enemy.getX() < player.getX()) {
                this.gameOver();
            }

            enemy.draw(canvas);
        }
    }

    private void gameOver() {
        this.isGameOver = true;
        if (this.gameOverOverlay != null) {
            this.gameOverOverlay.setVisibility(View.VISIBLE);
        }
    }

    public void spawnPlayer() {
        Drawable playerDrawable = getResources().getDrawable(R.drawable.archer);

        // Player aligned vertically
        this.player = new Player(getContext(), new Point(PLAYER_X_OFFSET,
                MainActivity.SCREEN_SIZE.y / 2 - playerDrawable.getIntrinsicHeight() / 2));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Log.d("GameView", "onDraw");

        updateUI();

        if (isGameOver) {
            gameOver();
        }

        if (canvasWidth == 0) {
            canvasWidth = getWidth();
        }

        if (canvasHeight == 0) {
            canvasHeight = getHeight();
        }

        this.updateEnemies(canvas);

        this.player.draw(canvas);
    }

    public Player getPlayer() {
        return player;
    }

    public void fireArrow(Point destination) {

    }
}
