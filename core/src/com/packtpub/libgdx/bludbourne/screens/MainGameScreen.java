package com.packtpub.libgdx.bludbourne.screens;

public class MainGameScreen {

    private static final String TAG = MainGameScreen.class.getSimpleName();

    public static class VIEWPORT {
        public static float viewportWidth;
        public static float viewportHeight;
        public static float virtualWidth;
        public static float virtualHeight;
        public static float physicalWidth;
        public static float physicalHeight;
        public static float aspectRatio;
    }

    public enum GameState {
        SAVING,
        LOADING,
        RUNNING,
        PAUSED,
        GAME_OVER
    }

    private static GameState gameState;
}
