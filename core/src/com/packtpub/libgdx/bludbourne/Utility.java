package com.packtpub.libgdx.bludbourne;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import static jdk.internal.joptsimple.internal.Strings.isNullOrEmpty;

public final class Utility {
    private static final String TAG = Utility.class.getSimpleName();

    private static final InternalFileHandleResolver filePathResolver = new InternalFileHandleResolver();

    public static final AssetManager assetManager = new AssetManager();

    public static void unloadAsset(String assetFileNamePath) {
        // once the asset manager is done loading
        if (assetManager.isLoaded(assetFileNamePath)) {
            assetManager.unload(assetFileNamePath);
        } else {
            Gdx.app.debug(TAG, "Asset is not loaded; Nothing to unload: " + assetFileNamePath);
        }
    }

    public static float loadCompleted() { return assetManager.getProgress(); }

    public static int numberAssetsQueued() { return assetManager.getQueuedAssets(); }

    public static boolean updateAssetLoading() { return assetManager.update(); }

    public static boolean isAssetLoaded(String fileName) {
        return assetManager.isLoaded(fileName);
    }

    public static void loadMapAsset(String mapFileNamePath) {
        if (isNullOrEmpty(mapFileNamePath)) {
            return;
        }

        // load asset
        if (filePathResolver.resolve(mapFileNamePath).exists()) {
            assetManager.setLoader(TiledMap.class, new TmxMapLoader(filePathResolver));
            assetManager.load(mapFileNamePath, TiledMap.class);
            // Until we add loading screen, just block until we load the map
            assetManager.finishLoadingAsset(mapFileNamePath);
            Gdx.app.debug(TAG, "Map loaded!: " + mapFileNamePath);
        } else {
            Gdx.app.debug(TAG, "Map doesn't exist!: " + mapFileNamePath );
        }
    }

    public static TiledMap getMapAsset(String mapFileNamePath) {
        if (assetManager.isLoaded(mapFileNamePath)) {
            return assetManager.get(mapFileNamePath,TiledMap.class);
        } else {
            Gdx.app.debug(TAG, "Map is not loaded: " + mapFileNamePath );
        }

        return null;
    }

    public static void loadTextureAsset(String textureFileNamePath) {
        if (isNullOrEmpty(textureFileNamePath)) {
            return;
        }

        if (filePathResolver.resolve(textureFileNamePath).exists()) {
            assetManager.setLoader(Texture.class, new TextureLoader(filePathResolver));
            assetManager.load(textureFileNamePath, Texture.class);
            assetManager.finishLoadingAsset(textureFileNamePath);
        } else {
            Gdx.app.debug(TAG, "Texture doesn't exist!: " + textureFileNamePath );
        }
    }

    public static Texture getTextureAsset(String textureFileNamePath) {
        if (assetManager.isLoaded(textureFileNamePath)) {
            return assetManager.get(textureFileNamePath,Texture.class);
        } else {
            Gdx.app.debug(TAG, "Map is not loaded: " + textureFileNamePath );
        }

        return null;
    }
}
