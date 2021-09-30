package com.daasuu.epf.filter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import com.daasuu.epf.EglUtil;

/**
 * This will posterize the resulting video a bit since it does not interpolate between 2 LUT values
 */
public class GlFastLookUpTableFilter extends GlFilter {

    private final static String FRAGMENT_SHADER =
            "precision lowp float;" +
                    "uniform sampler2D lutTexture; \n" +
                    "uniform sampler2D sTexture; \n" +
                    "varying vec2 vTextureCoord; \n" +
                    "void main() {\n" +
                    "   vec4 pixel = texture2D(sTexture, vTextureCoord);\n" +
                    "   float width = 16.;\n" +
                    "   float sliceSize = 1.0 / width;\n" +
                    "   float slicePixelSize = sliceSize / width;\n" +
                    "   float sliceInnerSize = slicePixelSize * (width - 1.0);\n" +
                    "   float zSlice0 = min(floor(pixel.b * width), width - 1.0);\n" +
                    "   float xOffset = slicePixelSize * 0.5 + pixel.r * sliceInnerSize;\n" +
                    "   float s0 = xOffset + (zSlice0 * sliceSize);\n" +
                    "   gl_FragColor = texture2D(lutTexture, vec2(s0, pixel.g));\n" +
                    "}";

    public GlFastLookUpTableFilter(Bitmap bitmap) {
        super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER);
        this.lutTexture = bitmap;
        hTex = EglUtil.NO_TEXTURE;
    }


    public GlFastLookUpTableFilter(Resources resources, int fxID) {
        super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER);
        this.lutTexture = BitmapFactory.decodeResource(resources, fxID);
        hTex = EglUtil.NO_TEXTURE;
    }

    private int hTex;

    private Bitmap lutTexture;

    @Override
    public void onDraw() {
        int offsetDepthMapTextureUniform = getHandle("lutTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, hTex);
        GLES20.glUniform1i(offsetDepthMapTextureUniform, 3);
    }

    @Override
    public void setup() {
        super.setup();
        loadTexture();
    }

    private void loadTexture() {
        if (hTex == EglUtil.NO_TEXTURE) {
            hTex = EglUtil.loadTexture(lutTexture, EglUtil.NO_TEXTURE, false);
        }
    }

    public void releaseLutBitmap() {
        if (lutTexture != null && !lutTexture.isRecycled()) {
            lutTexture.recycle();
            lutTexture = null;
        }
    }

    public void reset() {
        hTex = EglUtil.NO_TEXTURE;
        hTex = EglUtil.loadTexture(lutTexture, EglUtil.NO_TEXTURE, false);
    }
}
