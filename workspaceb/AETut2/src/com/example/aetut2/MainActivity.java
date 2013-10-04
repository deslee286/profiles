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
import org.andengine.entity.Entity;
import org.andengine.entity.modifier.FadeInModifier;
import org.andengine.entity.modifier.FadeOutModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.modifier.RotationAtModifier;
import org.andengine.entity.modifier.RotationByModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.render.RenderTexture;
import org.andengine.opengl.util.GLState;
//import org.andengine.opengl.vertex.RectangleVertexBuffer;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ModifierList;
import org.andengine.util.modifier.ParallelModifier;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import java.lang.*;
import java.lang.System.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.example.aetut2.XScene;
import com.example.aetut2.AutoTextureLoader;
import com.example.aetut2.MapEntity;
//import com.example.aetut2.XText;
//import com.example.aetut2.XRunnable;
//import com.example.aetut2.XUpdateHandler;


public class MainActivity extends BaseGameActivity implements IScrollDetectorListener,IOnSceneTouchListener {
	// Important stuffs:
	// There's only one camera - control the width and height here:
	protected static final int CAMERA_WIDTH = 1280;
	protected static final int CAMERA_HEIGHT = 800;

	// Main Scene:
	protected XScene 				scene;
	protected BitmapTextureAtlas 	playerTexture;
	protected ITextureRegion 		playerTexureRegion;
	protected PhysicsWorld 			physicsWorld;
	protected AutoTextureLoader 	mAutoTextureLoader;
	public boolean mIsDirty = true;
	
	// Splash Screen:
	
	private Sprite splash;
	private TextureRegion splashTextureRegion;
	private BitmapTextureAtlas splashTextureAtlas;
	private Scene splashScene;
	private SmoothCamera mCamera;
	private Engine TopEngine;
	
	// Menus:
	// We'll store all of our menu layers here:
	private final Map<String,Entity> mMenuLayerMap = new HashMap<String,Entity>();
	// Store a map of the menu items so that we can refer to it from within 'closures'
	protected final Map<String,Text> mMenuTextMap = new HashMap<String,Text>();	
	// This is used for formatting:
	private final List<Text> mMenuTextList = new ArrayList<Text>();
	// This holds the currently active menu:
	private String mActiveMenu = "0";	// 0 == no menu is active
		
	// Menu options:
	String mMenuOptions1[] = {"Continue","Load","New","Options","Quit"};
	//String mMenuOptionsNew[] = {''
	
	// Menu Screens:
	private Map <String, Text> activeMenuItemsMap = new HashMap <String, Text>(); 

	//
	// If GLES2 is not supported on this device, then Toast a message and then exit.
	// Otherwise, start the game normally.
	//
	@Override
	protected void onSetContentView() {
		Context context = getApplicationContext();
		CharSequence message = "This device does not support AndEngine GLES2, so this game will not work. Sorry.";
		int duration = Toast.LENGTH_SHORT;

		if(!AndEngine.isDeviceSupported()) {
			Thread thread = new Thread() {
	            @Override
	            public void run() {
	                try{
	                    Thread.sleep(3500);
	                    android.os.Process.killProcess(android.os.Process.myPid());
	                }
	                catch (InterruptedException e) {}
	            }
	        };
			Toast toast = Toast.makeText(context, message, duration);       
			toast.show();
	        finish();
	        thread.start();
		}
		else {
			super.onSetContentView();
		}
	}
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		//Camera mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		mCamera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 1000f, 1000f, 10f);
		
		//mCamera.setBounds(0, CAMERA_WIDTH * 2, 0, CAMERA_HEIGHT * 2);
		//mCamera.setBoundsEnabled(true);
	
		EngineOptions options = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
		return options;
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {

		mAutoTextureLoader = new AutoTextureLoader(this);
		
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	private void createSplashScreen()
	{
		// We're just going to use this method to create the splash screen texture,
		// to display the splash screen quickly.
//		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		// The atlas concept is for when you have a bunch of sprites on one image.  Here we have the splash
		// screen on one image
//		splashTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),1024, 768, TextureOptions.DEFAULT);
//		splashTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTextureAtlas, this, "dark_forest.jpg", 0, 0 );
//		splashTextureAtlas.load();
		
		splashScene = new Scene();
	    splash = new Sprite(0, 0, 
	    		//splashTextureRegion,
	    		mAutoTextureLoader.getLoadedTextureRegion("dark_forest.jpg"),
	    		mEngine.getVertexBufferObjectManager())
	    {
	        @Override
	        protected void preDraw(GLState pGLState, Camera pCamera)	
	        {
	            super.preDraw(pGLState, pCamera);
	            pGLState.enableDither();
	        }
	    };

	    splash.setScale(CAMERA_WIDTH / 1024f);	// Increase/decrease scale of the sprite
	    // Position of a sprite is calculated from it's middle point, so position it at the middle of the screen:
	    splash.setPosition((CAMERA_WIDTH - splash.getWidth()) * 0.5f, (CAMERA_HEIGHT - splash.getHeight()) * 0.5f);
	    // Attaching the child will display the image:
	    splashScene.attachChild(splash);
	}
	
	

	
	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		//
		// Used to create the splash screen:
		//
		
		
		//this.scene = new Scene();
		//this.scene.setBackground(new Background(0, 125, 58));
		
		//physicsWorld = new PhysicsWorld(new Vector2(0,
		//		SensorManager.GRAVITY_EARTH), false);
		//this.scene.registerUpdateHandler(physicsWorld);
		//createWalls();
		
		createSplashScreen();
		this.getEngine().registerUpdateHandler(new FPSLogger());
		
		pOnCreateSceneCallback.onCreateSceneFinished(this.splashScene);
	}

	BitmapTextureAtlas fontTextureAtlas;
	Font font;

	private void displayText(String myText, Scene pScene, int pX, int pY)
	{
		FontFactory.setAssetBasePath("fonts/");
		fontTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);

//		this.mDroidFont = FontFactory.createFromAsset(
				//this.getFontManager(), droidFontTexture, this.getAssets(), "Droid.ttf", 45, true, Color.BLACK);
		//this.mDroidFont.load();

//		font = FontFactory.createFromAsset(
//				this.getFontManager(), fontTextureAtlas, this.getAssets(),
//					//"Achafexp.ttf",
//					"CabinSketch-Bold.ttf",
//					95f, true, android.graphics.Color.WHITE);
		font = mAutoTextureLoader.getLoadedFont("CabinSketch-Bold.ttf", 95f, android.graphics.Color.WHITE);
		Text newText;
		TopEngine = this.getEngine();
		
		newText = new Text( pX, pY, font, myText, this.getVertexBufferObjectManager() ) {
			@Override
			public boolean onAreaTouched( final TouchEvent pTouchEvent,
					final float pX, final float pY ) {
				System.out.println("Text '" + this.mText + "' has been pressed.");
				mCamera.setZoomFactor(300f);
				TopEngine.registerUpdateHandler( new TimerHandler(0.5f, new ITimerCallback() {
					
					@Override
					public void onTimePassed(TimerHandler pTimerHandler) {
						// TODO Auto-generated method stub
						TopEngine.getScene().detachChildren();
						return;
					}
				} ) );
				return true;
				
			}
		};
		pScene.registerTouchArea(newText);	
		System.out.println( "Text height: " + newText.getHeight());
		System.out.println( "Text width:  " + newText.getWidth());
		pScene.attachChild( newText );
	}
	
	/**
	 * Setup all the menus that we plan to use so that they can be used...
	 */
	private void createMenus() {
		mMenuTextList.clear();
		Entity entity = new Entity();
		mMenuLayerMap.put("1", entity);
		
		Text text;
		// CONTINUE
		Font myFont = mAutoTextureLoader.getLoadedFont("CabinSketch-Bold.ttf", 96f, android.graphics.Color.WHITE);
		text = new Text( 0,0, myFont, "Continue",this.getVertexBufferObjectManager() ) {
			@Override
			public boolean onAreaTouched( final TouchEvent pTouchEvent, final float pX, final float pY ) {
				gameContinue();
				return true;
			}
		};
		mMenuTextMap.put("1.continue",text);
		mMenuTextList.add(text);
		
		// LOAD
		text = new Text( 0,0, myFont, "Load",this.getVertexBufferObjectManager() ) {
			@Override
			public boolean onAreaTouched( final TouchEvent pTouchEvent, final float pX, final float pY ) {
				gameContinue();
				return true;
			}
		};
		mMenuTextMap.put("1.load",text);
		mMenuTextList.add(text);

		// NEW
		text = new Text( 0,0, myFont, "New",this.getVertexBufferObjectManager() ) {
			@Override
			public boolean onAreaTouched( final TouchEvent pTouchEvent, final float pX, final float pY ) {
				gameNew();
				return true;
			}
		};
		mMenuTextMap.put("1.new",text);
		mMenuTextList.add(text);

		text = new Text( 0,0, myFont, "Exit",this.getVertexBufferObjectManager() ) {
			@Override
			public boolean onAreaTouched( final TouchEvent pTouchEvent, final float pX, final float pY ) {
				gameQuit();
				return true;
			}
		};
		mMenuTextMap.put("1.quit",text);
		mMenuTextList.add(text);
		
//		layoutMenu( entity, mMenuTextList );
		int placeY,y;
		
		placeY = CAMERA_HEIGHT / ( mMenuTextList.size() * 2 );
		y = placeY;
		for ( Text t : mMenuTextList ) {
			t.setY(y - t.getHeight()/2);
			t.setX(CAMERA_WIDTH / 20);
			y = y + placeY * 2;
//			splashScene.registerTouchArea(t);
			entity.attachChild( t );
//			t.set
		}
		
		
		//
		// Menu 2: Discard Progress?
		//
		mMenuTextList.clear();
		entity = new Entity();
		mMenuLayerMap.put("2",entity);
		text = new Text( 0,0, myFont, "Discard progress?", this.getVertexBufferObjectManager());
		mMenuTextMap.put("2.message",text);
		mMenuTextList.add(text);

		text = new Text( 0,0, myFont, "Exit", this.getVertexBufferObjectManager())
		{
			@Override
			public boolean onAreaTouched( final TouchEvent pTouchEvent, final float pX, final float pY ) {
				mIsDirty = false;
				gameQuit();
				return true;
			}
		};
		mMenuTextMap.put("2.exit",text);
		mMenuTextList.add(text);

		text = new Text( 0,0, myFont, "Back", this.getVertexBufferObjectManager())
		{
			@Override
			public boolean onAreaTouched( final TouchEvent pTouchEvent, final float pX, final float pY ) {
//				transitionOutMenu("2");
				transitionInMenu("1");
				return true;
			}
		};
		mMenuTextMap.put("2.back",text);
		mMenuTextList.add(text);

		
		placeY = CAMERA_HEIGHT / ( mMenuTextList.size() * 2 );
		y = placeY;
		for ( Text t : mMenuTextList ) {
			t.setY(y - t.getHeight()/2);
			t.setX(CAMERA_WIDTH / 20);
			y = y + placeY * 2;
//			splashScene.registerTouchArea(t);
			entity.attachChild( t );
//			t.set
		}

	}

	
	// IScrollDetectorListener:
	
	
	/**
	 * Quit the game
	 */

	private void gameQuit() {
		if (mIsDirty) {
			// Quit, yes/no:
			transitionInMenu("2");
		}
		else {
			transitionOutMenu(mActiveMenu);
			final TimerHandler spriteTimerHandler = new TimerHandler(0.5f, new ITimerCallback()
			{
				@Override
				public void onTimePassed(final TimerHandler pTimerHandler)
				{ 
					finish();                      
				}
			});
			this.getEngine().registerUpdateHandler(spriteTimerHandler);
//			mTextMenuItems1.
//			finish();
		}
	}
	
	/**
	 * Are you sure?
	 */
	private void transitionInMenu(String iMenuID) {
		if (mActiveMenu != "0") {
			transitionOutMenu( mActiveMenu );
		}
		System.out.println("Transitioning in menu " + iMenuID );
		if (! mMenuLayerMap.containsKey(iMenuID)) {
			System.out.println("mMenuLayerMap does not contain key " + iMenuID + ".");
			return;
		}
		mActiveMenu = iMenuID;
		// Menu is set up, need to enable the buttons and phase in:
		Entity entity =mMenuLayerMap.get(iMenuID); 
		for (int i = 0; i < entity.getChildCount(); i++) {
			Text text = (Text)entity.getChildByIndex(i);
			text.clearEntityModifiers();
			text.clearUpdateHandlers();
			text.registerEntityModifier(new ParallelEntityModifier(new FadeInModifier(0.35f), new RotationAtModifier( 0.5f, 90f, 0f, 25f, 25f)));
			//splashScene.registerTouchArea(text);
		}
		// If it's not already attached to the scene, attach it:
		if (! entity.hasParent()) {
			splashScene.attachChild(entity);
		}
		// Set the timer to allow button activation:
		TimerHandler activeateTouchAreasTimerHandler = new TimerHandler(0.5f, new ITimerCallback()
		{
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler)
			{ 
				registerActiveMenuTouchAreas();
			}
		});
		this.getEngine().registerUpdateHandler(activeateTouchAreasTimerHandler);

	}

	/**
	 * A simple toast:
	 * @param msg
	 */
	public void gameToast(final String msg) {
	    this.runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	           Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
	        }
	    });
	}	
	
	/**
	 * Activate touch areas for a given set of menu options.
	 */
	private void registerActiveMenuTouchAreas() {
		if (mActiveMenu == "0") {
			gameToast( "Error: mActiveMenu is 0" );
			return;
		}
		System.out.println("Registering touch areas for menu" + mActiveMenu );
		if (! mMenuLayerMap.containsKey(mActiveMenu)) {
			System.out.println("mMenuLayerMap does not contain key " + mActiveMenu + ".");
			return;
		}
		// Menu is set up, register touch areas:
		Entity entity =mMenuLayerMap.get(mActiveMenu); 
		for (int i = 0; i < entity.getChildCount(); i++) {
			Text text = (Text)entity.getChildByIndex(i);
			splashScene.registerTouchArea(text);
		}
	}

	/**
	 * Transition menu items out.
	 */
	private void transitionOutMenu(String iMenuID) {
		System.out.println("Transitioning out menu " + iMenuID );
		if (! mMenuLayerMap.containsKey(iMenuID)) {
			System.out.println("mMenuLayerMap does not contain key " + iMenuID + ".");
			return;
		}
		// Menu is set up, need to enable the buttons and phase in:
		Entity entity =mMenuLayerMap.get(iMenuID); 
		for (int i = 0; i < entity.getChildCount(); i++) {
			Text text = (Text)entity.getChildByIndex(i);
			text.clearEntityModifiers();
			text.clearUpdateHandlers();
			text.registerEntityModifier(new ParallelEntityModifier(new FadeOutModifier(0.35f), new RotationAtModifier( 0.5f, 0f, -90f, 25f, 25f)));
			System.out.println("Unregister touch area");
			splashScene.unregisterTouchArea(text);
		}
		// If it's not already attached to the scene, attach it:
		if (! entity.hasParent()) {
			splashScene.attachChild(entity);
		}

	}
	/**
	 * Continue last saved game
	 */

	MapEntity mMap;
	boolean mMapInitialized = false;
	private void gameContinue() {
		if (!mMapInitialized) {
			mMap = new MapEntity(this);
			for (int i = 0; i < 5000; i++ ) {
				mMap.attachChild( new Sprite((float)(Math.random() * 400),(float)(Math.random() * 400), 
			    		mAutoTextureLoader.getLoadedTextureRegion("wall_64x8.png"),
			    		mEngine.getVertexBufferObjectManager()) );
			}
//			mMap.setPosition(50,50);
		}
		Sprite mMapSprite = new Sprite( 50, 50, mMap.getMapTexture(),this.getVertexBufferObjectManager());
		
		splashScene.attachChild( mMapSprite );
	    //TextureManager mapTextureManager = new TextureManager();
	    //Sprite mapSprite = new Sprite(
	    //mapTextureManager.
	//    mapTexture.
	    //s.
	    
	}
	
	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		//
		// This is where the game work begins:
		//
		createMenus();
		transitionInMenu("1");
//		physicsWorld = new PhysicsWorld(new Vector2(0,
//				SensorManager.GRAVITY_EARTH), false);
//		this.splashScene.registerUpdateHandler(physicsWorld);
//		createWalls();
// 	   System.out.println( "Walls created\n");
// 	   Log.d("Basic",  "Something happening" );

//		Sprite sPlayer = new Sprite(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2,
//				playerTexureRegion, this.mEngine.getVertexBufferObjectManager()) {
//			@Override
//			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
//					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
//				//this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
//					final TouchEvent event = pSceneTouchEvent;
//					
//		                int eventaction = event.getAction(); 
//		                
//		                float X = event.getX();
//		                float Y = event.getY();
//
//		                switch (eventaction) {
//		                   case TouchEvent.ACTION_DOWN:
//		                	    displayText( "A1", splashScene, 250,250);
//			                	   System.out.println( "DOWN " + X + "," + Y);
//		                	    break;
//		                   case TouchEvent.ACTION_MOVE: {
//		            	        this.setPosition(X - this.getWidth() / 2,
//		            	        				 Y - this.getHeight() / 2
//		            	        				);
//			                	   System.out.println( "MOVE " + X + "," + Y);
//			                	//this.setPosition
//		            	        break;}
//		                   case TouchEvent.ACTION_UP:
//		                	   System.out.println( "UP " + X + "," + Y);
//		                        break;
//		                }
//				return true;
//			}
//		};
//		sPlayer.setRotation(45.0f);
////		final FixtureDef PLAYER_FIX = PhysicsFactory.createFixtureDef(10.0f,
////				1.0f, 0.0f);
////		Body body = PhysicsFactory.createCircleBody(physicsWorld, sPlayer,
////				BodyType.DynamicBody, PLAYER_FIX);
//		splashScene.registerTouchArea(sPlayer);
//		this.splashScene.attachChild(sPlayer);
////		physicsWorld.registerPhysicsConnector(new PhysicsConnector(sPlayer,
////				body, true, false));
//
		
//		this.splashScene.
		
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	//
	// Below is for screen touching/scrolling:
	//

	/**
	 * Use this to allow touch events from your BaseGameActivity class. Your main activity must implement IOnSceneTouchListener 
	 * implements IOnSceneTouchListener
	 * We also need to set the scene listener: gameScene.setOnSceneTouchListener(this);
	 */
	private SurfaceScrollDetector mScrollDetector;
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		//gameToast( "You touched the screen at " + pSceneTouchEvent.getX() + "," + pSceneTouchEvent.getY() + ".");
		System.out.println( "You touched the screen at " + pSceneTouchEvent.getX() + "," + pSceneTouchEvent.getY() + ".");
		this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
//	    this.mCamera.setCenter(pSceneTouchEvent.getX(), pSceneTouchEvent.getY() );

		return true;
	}
	
	@Override
	public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScroll(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		System.out.println( "onScroll called: " +  pDistanceX + "," + pDistanceY );
	//    float zoomFactor = this.mCamera.getZoomFactor();
	//    this.mCamera.setCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	    this.mCamera.offsetCenter(-pDistanceX, -pDistanceY);
	}

	@Override
	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * New game
	 */
	private Scene gameScene;
	private void gameNew() {
		// Test1: Shared vertex buffer:
		//final RectangeVertexBuffer sharedVertexBuffer = new RectangleVertexBuffer();
		
		gameScene = new Scene();
		gameScene.setBackground(new Background(0, 125, 58));
		this.getEngine().setScene(gameScene);
		Sprite s;
		
		for (int i = 0; i < 256; i++) {
		    s = new Sprite((int)(Math.random()*CAMERA_WIDTH*10), (int)(Math.random()*CAMERA_HEIGHT*10), 
		    		mAutoTextureLoader.getLoadedTextureRegion("wall_64x8.png"),
		    		mEngine.getVertexBufferObjectManager());
		    //s.setRotation((float)Math.random() * 180);
		    //s.setPosition( 100, 100 );
		    gameScene.attachChild(s);
		    s.setIgnoreUpdate(true);
		}
		if ( true ) {
			for (int i = 0; i < 1000; i++) {
			    s = new Sprite((int)(Math.random()*CAMERA_WIDTH*10), (int)(Math.random()*CAMERA_HEIGHT*10), 
			    		mAutoTextureLoader.getLoadedTextureRegion("wall_256x8.png"),
			    		mEngine.getVertexBufferObjectManager());
			    //s.setRotation((float)Math.random() * 180);
			    //s.setPosition( 100, 100 );
			    gameScene.attachChild(s);
			    s.setIgnoreUpdate(true);
			    s.setVisible(false);
			}
		}
//		for (int i = 0; i < 2500; i++) {
//		    s = new Sprite((int)(Math.random()*CAMERA_WIDTH*10), (int)(Math.random()*CAMERA_HEIGHT*10), 
//		    		mAutoTextureLoader.getLoadedTextureRegion("wall_64x8.png"),
//		    		mEngine.getVertexBufferObjectManager());
//		    //s.setRotation((float)Math.random() * 180);
//		    //s.setPosition( 100, 100 );
//		    gameScene.attachChild(s);
//		    s.setIgnoreUpdate(true);
//		    s.setVisible(false);
//		}

		// Listen to scroll requests: 
		gameScene.setOnSceneTouchListener(this);
//		gameScene.setTouchAreaBindingOnActionDownEnabled(true); // Necessary?
//		gameScene.setTouchAreaBindingOnActionMoveEnabled(true); // Necessary?
		mScrollDetector = new SurfaceScrollDetector(this);
		mScrollDetector.setEnabled(true);
//		mCamera.setCenter(800f, 800f);
//		mCamera.setCenter(pCenterX, pCenterY)
		gameToast("Hello");
	}

}