package com.packtpub.libgdx.bludbourne;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.isNull;

public class Entity {

    private static final String TAG = Entity.class.getSimpleName();
    private static final String defaultSpritePath = "sprites/characters/Warrior.png";

    public static Rectangle boundingBox;

    public final int FRAME_WIDTH = 16;
    public final int FRAME_HEIGHT = 16;

    public enum State {
        IDLE, WALKING
    }

    public enum Direction {
        UP,RIGHT,DOWN,LEFT;
    }

    private String entityId;

    private Vector2 velocity;

    private Direction currentDirection = Direction.LEFT;
    private Direction previousDirection = Direction.UP;

    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkDownAnimation;

    protected Vector2 nextPlayerPosition;
    protected Vector2 currentPlayerPosition;
    protected State state = State.IDLE;
    protected float frameTime = 0f;
    protected Sprite frameSprite;
    protected TextureRegion currentFrame;

    public Entity() {
        entityId = UUID.randomUUID().toString();
        nextPlayerPosition = new Vector2();
        currentPlayerPosition = new Vector2();
        boundingBox = new Rectangle();
        velocity = new Vector2(2f, 2f);

        Utility.loadTextureAsset(defaultSpritePath);
        loadDefaultSprite();
        loadAllAnimations();
    }

    public void init(float startX, float startY) {
        currentPlayerPosition.x = startX;
        currentPlayerPosition.y = startY;

        nextPlayerPosition.x = startX;
        nextPlayerPosition.y = startY;
    }

    public void update(float delta) {
        frameTime = (frameTime + delta) % 5; // Want to avoid overflow

        setBoundingBoxSize(0f, 0.5f);
    }

    public void setBoundingBoxSize(float percentageWidthReduced, float percentageHeightReduced) {
        // Update current bounding box
        float width;
        float height;

        float widthReductionAmount = 1.0f - percentageWidthReduced; // .8f for 20% (1 - .20)
        float heightReductionAmount = 1.0f - percentageHeightReduced;

        if( widthReductionAmount > 0 && widthReductionAmount < 1){
            width = FRAME_WIDTH * widthReductionAmount;
        }else{
            width = FRAME_WIDTH;
        }

        if( heightReductionAmount > 0 && heightReductionAmount < 1){
            height = FRAME_HEIGHT * heightReductionAmount;
        }else{
            height = FRAME_HEIGHT;
        }


        if( width == 0 || height == 0){
            Gdx.app.debug(TAG, "Width and Height are 0!! " + width + ":" + height);
        }

        //Need to account for the unitscale, since the map coordinates will be in pixels
        float minX;
        float minY;
        if(MapManager.UNIT_SCALE > 0) {
            minX = nextPlayerPosition.x / MapManager.UNIT_SCALE;
            minY = nextPlayerPosition.y / MapManager.UNIT_SCALE;
        }else{
            minX = nextPlayerPosition.x;
            minY = nextPlayerPosition.y;
        }

        boundingBox.set(minX, minY, width, height);
    }

    public void dispose() { Utility.unloadAsset(defaultSpritePath); }

    public void setState(State state) {
        this.state = state;
    }

    public Vector2 getCurrentPlayerPosition() {
        return currentPlayerPosition;
    }

    public Sprite getFrameSprite() {
        return frameSprite;
    }

    public TextureRegion getCurrentFrame() {
        return currentFrame;
    }

    public void setCurrentPosition(float currentPositionX, float currentPositionY){
        frameSprite.setX(currentPositionX);
        frameSprite.setY(currentPositionY);
        currentPlayerPosition.x = currentPositionX;
        currentPlayerPosition.y = currentPositionY;
    }

    public void setDirection(Direction direction){
        previousDirection = currentDirection;
        currentDirection = direction;

        //Look into the appropriate variable when changing position

        switch (currentDirection) {
            case DOWN :
                currentFrame = walkDownAnimation.getKeyFrame(frameTime);
                break;
            case LEFT :
                currentFrame = walkLeftAnimation.getKeyFrame(frameTime);
                break;
            case UP :
                currentFrame = walkUpAnimation.getKeyFrame(frameTime);
                break;
            case RIGHT :
                currentFrame = walkRightAnimation.getKeyFrame(frameTime);
                break;
            default:
                break;
        }
    }

    public void setNextPositionToCurrent(){
        setCurrentPosition(nextPlayerPosition.x, nextPlayerPosition.y);
    }


    public void calculateNextPosition(Direction currentDirection, float deltaTime){
        float testX = currentPlayerPosition.x;
        float testY = currentPlayerPosition.y;

        velocity.scl(deltaTime);

        switch (currentDirection) {
            case LEFT :
                testX -=  velocity.x;
                break;
            case RIGHT :
                testX += velocity.x;
                break;
            case UP :
                testY += velocity.y;
                break;
            case DOWN :
                testY -= velocity.y;
                break;
            default:
                break;
        }

        nextPlayerPosition.x = testX;
        nextPlayerPosition.y = testY;

        velocity.scl(1 / deltaTime);
    }

    private void loadDefaultSprite() {
        Texture texture = Utility.getTextureAsset(defaultSpritePath);
        TextureRegion[][] textureFrames = TextureRegion.split(Objects.requireNonNull(texture), FRAME_WIDTH, FRAME_HEIGHT);
        currentFrame = textureFrames[0][0];
        frameSprite = new Sprite(currentFrame.getTexture(), 0, 0, FRAME_WIDTH, FRAME_HEIGHT);
    }

    private void loadAllAnimations() {
        // Walking animation
        Texture texture = Utility.getTextureAsset(defaultSpritePath);
        TextureRegion[][] textureFrames = TextureRegion.split(Objects.requireNonNull(texture), FRAME_WIDTH, FRAME_HEIGHT);

        Array<TextureRegion> walkDownFrames = new Array<>(4);
        Array<TextureRegion> walkLeftFrames = new Array<>(4);
        Array<TextureRegion> walkRightFrames = new Array<>(4);
        Array<TextureRegion> walkUpFrames = new Array<>(4);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                TextureRegion region = textureFrames[i][j];
                if(isNull(region)) {
                    Gdx.app.debug(TAG, "Got null animation frame " + i + "," + j);
                }
                switch(i) {
                    case 0:
                        walkDownFrames.insert(j, region);
                        break;
                    case 1:
                        walkLeftFrames.insert(j, region);
                        break;
                    case 2:
                        walkRightFrames.insert(j, region);
                        break;
                    case 3:
                        walkUpFrames.insert(j, region);
                        break;
                }
            }
        }

        walkDownAnimation = new Animation(0.25f, walkDownFrames, Animation.PlayMode.LOOP);
        walkLeftAnimation = new Animation(0.25f, walkLeftFrames, Animation.PlayMode.LOOP);
        walkRightAnimation = new Animation(0.25f, walkRightFrames, Animation.PlayMode.LOOP);
        walkUpAnimation = new Animation(0.25f, walkUpFrames, Animation.PlayMode.LOOP);
    }
}
