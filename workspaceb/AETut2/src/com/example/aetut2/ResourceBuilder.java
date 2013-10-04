package com.example.aetut2;

import org.andengine.AndEngine;
import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.TextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.entity.text.Text;
import org.andengine.entity.shape.RectangularShape;
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
import java.lang.System.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class ResourceBuilder {
	private Map<String,Sprite> mSpriteCache = new HashMap<String,Sprite>();
	private Map<String,Font> mFontCache = new HashMap<String,Font>();
	private Map<String,XScene> mXSceneCache = new HashMap<String,XScene>();

//	private TextureManager mTextureManager;
	private Context mContext;
	private BaseGameActivity mBaseGameActivity;
//	private boolean mAssetBasePathSet = false;
	
	public ResourceBuilder( /* TextureManager iTextureManager,*/ BaseGameActivity iBaseGameActivity ) {
		// Assume nothing will ever change it from this value
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
//		mTextureManager = iTextureManager;
		mBaseGameActivity = iBaseGameActivity;
	} 
	
	public Sprite GetSprite( String Filename ) {
		return this.GetSprite( Filename, 0, 0 );
	}
	
	public Sprite GetSprite( String Filename, int X, int Y ) {
		// This assumes the asset base path is set appropriately:
		
		// Variables:
		BitmapTextureAtlas myBitmapTextureAtlas; // Do we really need this?
		TextureRegion myTextureRegion;
		
		int Width	= 1024;
		int Height 	= 768;
		System.out.println("FILE: " + Filename );
		try {
			Width	= Integer.parseInt( Filename.substring(0, 4) );
			Height	= Integer.parseInt( Filename.substring(5 ,9));
			System.out.println("Width: " + Width + ", Height: " + Height );
		}
		catch( NumberFormatException e) {
			// We can continue, using the defaults for Width and/or Height set above
			System.out.println("Caught exception parsing dimensions for file " + Filename + 
				". Using default dimensions " + Width + "x" + Height );
		}
		finally {
			System.out.println("Caught exception parsing dimensions for file " + Filename + 
					". Using default dimensions " + Width + "x" + Height );
		
		}
		
		// The atlas concept is for when you have a bunch of sprites on one image.  Here we have the splash
		// screen on one image
		myBitmapTextureAtlas = new BitmapTextureAtlas(mBaseGameActivity.getTextureManager(), Width, Height, TextureOptions.DEFAULT);
		myTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset
				(myBitmapTextureAtlas, mContext, Filename, X, Y );
		myBitmapTextureAtlas.load();
		
		Sprite mySprite = new Sprite( X, Y, myTextureRegion, mBaseGameActivity.getEngine().getVertexBufferObjectManager());
		
		return( mySprite );
	}
	
}
