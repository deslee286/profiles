package com.example.aetut2;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import org.andengine.AndEngine;
import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.ui.IGameInterface.OnCreateResourcesCallback;
import org.andengine.ui.IGameInterface.OnCreateSceneCallback;
import org.andengine.ui.IGameInterface.OnPopulateSceneCallback;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.IFont;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
//import org.andengine.opengl.texture.atlas.
import org.andengine.util.color.Color;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import java.io.IOException;
import java.lang.System.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class AutoTextureLoader {

	// ====================================================
	// CONSTANTS
	// ====================================================
	private static final int TEXT_TEXTURE_WIDTH = 512;
	private static final int TEXT_TEXTURE_HEIGHT = 512;

	// ====================================================
	// VARIABLES
	// ====================================================
	public final ArrayList<ManagedStandardTexture> loadedTextures = new ArrayList<ManagedStandardTexture>();
	public final Map<String,ManagedStandardTexture> mLoadedTextureMap = new HashMap<String,ManagedStandardTexture>();	// The hash for fast access via filename:
	public BaseGameActivity mBaseGameActivity;
	protected TextureOptions mTextureOptions;
	
	public final Map<String,Font> mLoadedFontMap = new HashMap<String,Font>();

	
	// CONSTRUCTORS:
	public AutoTextureLoader( BaseGameActivity iActivity ) {
		mBaseGameActivity = iActivity;
		mTextureOptions = TextureOptions.BILINEAR;
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("");
	}
	public AutoTextureLoader( BaseGameActivity iActivity, TextureOptions iTextureOptions ) {
		mBaseGameActivity = iActivity;
		mTextureOptions = iTextureOptions;
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("");
	}
	
	/** Unloads all managed textures and removes them from memory. */
	public void unloadAllTextures() {
		// Unload and remove every currently managed texture.
		for(ManagedStandardTexture curTex : loadedTextures) {
			curTex.removeFromMemory();
			curTex=null;
			loadedTextures.remove(curTex);
		}
		System.gc();
	}

	/** Retrieves a texture region by name if it is being managed.
	 * @param pName The name of the texture. If loaded by the Resource Manager, the texture name will be the short-filename without an extension.
	 * @return The first managed <b>TextureRegion</b> with the given Name.<br><b>null</b> if no managed texture has the given name.
	 */
	public ITextureRegion getLoadedTextureRegion(String pName) {
		// Check the currently managed texture regions and return the first one
		// that matches the name.
		if (mLoadedTextureMap.containsKey(pName)) {
			return mLoadedTextureMap.get(pName).textureRegion;
		}
		// Below will be slower, I guess depending on the Map implementation, so comment it out:
		//for(ManagedStandardTexture curTex : loadedTextures)
		//	if(curTex.name.equalsIgnoreCase(pName))
		//		return curTex.textureRegion;
		
		else {
			// Need to load the file, if it exists:
			loadAndManageTextureRegion( mTextureOptions, pName );
			return mLoadedTextureMap.get(pName).textureRegion;
		}
	}

//	/** Retrieves a texture region by Filename and starts managing it.
//	 * @param pTextureOptions The TextureOptions that you want to apply to the TextureRegion.
//	 * @param pFilename The path to the texture (inside of the 'assets' folder and with an extension) to be loaded and returned.
//	 * @param pSceneName The name of the scene to associate the TextureRegion with.
//	 * @return The <b>TextureRegion</b> that was loaded and managed by the ResourceManager.
//	 */
//	public ITextureRegion getTextureRegion(TextureOptions pTextureOptions, String pFilename) {
//		loadAndManageTextureRegion(pTextureOptions,pFilename);
//		return loadedTextures.get(loadedTextures.size()-1).textureRegion;
//	}

	/** Loads a texture region by Filename and starts managing it.
	 * @param pTextureOptions The TextureOptions that you want to apply to the TextureRegion.
	 * @param pFilename The path to the texture (inside of the 'assets' folder and with an extension) to be loaded and returned.
	 * @param pSceneName The name of the scene to associate the TextureRegion with.
	 * @return The <b>TextureRegion</b> that was loaded and managed by the ResourceManager.
	 */
	public void loadAndManageTextureRegion(TextureOptions pTextureOptions, String pFilename) {
		AssetBitmapTextureAtlasSource cSource = AssetBitmapTextureAtlasSource.create(mBaseGameActivity.getAssets(), "gfx/" + pFilename);
		System.out.println(pFilename);
		BitmapTextureAtlas TextureToLoad = new BitmapTextureAtlas(mBaseGameActivity.getEngine().getTextureManager(), cSource.getTextureWidth(), cSource.getTextureHeight(), pTextureOptions);
		System.out.println(cSource.toString());
		System.out.println(cSource.getTextureWidth() + "," + cSource.getTextureHeight());
		TextureRegion TextureRegionToLoad = BitmapTextureAtlasTextureRegionFactory.createFromAsset(TextureToLoad, mBaseGameActivity, "gfx/" + pFilename, 0, 0);     
		TextureToLoad.load();
		mLoadedTextureMap.put(pFilename, new ManagedStandardTexture(pFilename,TextureRegionToLoad) );
		loadedTextures.add( mLoadedTextureMap.get(pFilename));
	}

	/**
	 * Creates and returns a text object based on a font.  Each font requires an atlas for the
	 * system to render the bitmap into, not only that, but each font size also.  What we'll do
	 * is attempt to allocate an appropriately-sized bitmap by figuring about 64 characters used
	 * per alphabet, with the size of each letter in pixels of about 110% of the point size.
	 * So, for example: for 96 point, we'll make it size 8 * (96 * 1.1) by  8 * (96 * 1.1) = 844 x 844
	 * And we may as well round it to a factor of 2, so 844 goes to 1024 
	 * @author Des Lee
	 * @version 1.0
	 * @param iTextureOptions A TextureOptions object
	 * @param iFilename The TFF font filename, assumed to be in the assets/fonts directory
	 * @param iPointSize The point size of the font
	 * @param iColor The ANDROID color (int)
	 */
	public void loadFont(TextureOptions iTextureOptions, String iFilename, float iPointSize, int iColor) {
		String key = iFilename + "x" + iPointSize + "x" + iColor;
		int w = (int)(iPointSize * 1.1f)*8;	// Create a 8x8 grid making (roughly) a

		if (w < 256) {
			w = 256;
		}
		else if (w < 512) {
			w = 512;
		}
		else if (w < 1024) {
			w = 1024;
		}
		else {
			w = 1024; // Make the max 1024x1024, as per usage recommendation
		}
		System.out.println("Font " + iFilename + " with point size " + iPointSize + " is getting a " + w + "x" + w + " BitmapTextureAtlas.");
		// All our fonts reside here:
		FontFactory.setAssetBasePath("fonts/");
		BitmapTextureAtlas fontTextureAtlas = new BitmapTextureAtlas( mBaseGameActivity.getTextureManager(), w, w, iTextureOptions);

		Font font = FontFactory.createFromAsset( mBaseGameActivity.getFontManager(), fontTextureAtlas, mBaseGameActivity.getAssets(),
											iFilename,
											iPointSize,
											true,
											iColor );
		font.load();
		mLoadedFontMap.put(key, font);
	}
	
	/**
	 * Return the loaded
	 * @param iFilename
	 * @param iPointSize
	 * @param iColor
	 * @return
	 */
	public Font getLoadedFont( String iFilename, float iPointSize, int iColor ) {
		String key = iFilename + "x" + iPointSize + "x" + iColor;
		if (mLoadedFontMap.containsKey( key )) {
			return mLoadedFontMap.get(key);
		}
		else {
			loadFont( mTextureOptions, iFilename, iPointSize, iColor);
			if (mLoadedFontMap.containsKey(key)) {
				return mLoadedFontMap.get(key);
			}
			else {
				return null;
			}
		}
	}
	
	// A simple class that holds the information for every managed Texture and can remove itself from memory.
	public class ManagedStandardTexture extends Object {
		public ITextureRegion textureRegion;
		public String name;

		public ManagedStandardTexture(String pName, final ITextureRegion pTextureRegion) {
			name = pName;
			textureRegion = pTextureRegion;
		}

		public void removeFromMemory() {
			loadedTextures.remove(this);
			mLoadedTextureMap.remove(this);
			textureRegion.getTexture().unload();
			textureRegion = null;
			name = null;
		}
	}
	
}