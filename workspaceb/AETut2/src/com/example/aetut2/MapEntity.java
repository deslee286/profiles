package com.example.aetut2;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.Entity;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.render.RenderTexture;
import org.andengine.opengl.util.GLState;
import org.andengine.ui.activity.BaseActivity;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;

//import android.graphics.Color;

public class MapEntity extends Entity {
	
	private RenderTexture mMapTexture; // Used to render
	private BaseGameActivity mParentActivity; 
	// Return the texture of the map:
    public TextureRegion  getMapTexture(){
        return TextureRegionFactory.extractFromTexture(mMapTexture);
    }

    // Constructor
    public MapEntity(BaseGameActivity iParentActivity) {
    	mParentActivity = iParentActivity;
		mMapTexture = new RenderTexture(mParentActivity.getTextureManager(), 400, 400 );

    }
    
    @Override
    protected void onManagedDraw(GLState pGLState, Camera pCamera) {
            if (!mMapTexture.isInitialized()) {
                    mMapTexture.init(pGLState);
            }
            mMapTexture.begin(pGLState, false, true, Color.TRANSPARENT);
            {
                    super.onManagedDraw(pGLState, pCamera);
            }
            mMapTexture.end(pGLState);
    }
}