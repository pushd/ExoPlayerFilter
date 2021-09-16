package com.daasuu.epf.filter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import com.daasuu.epf.EglUtil;

/**
 * Accepts a 512x512 LUT see https://github.com/BradLarson/GPUImage/blob/master/framework/Source/GPUImageLookupFilter.h
 */
public class GlLargeLookupTableFilter extends GlFilter {
    private final static String LARGE_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform mediump sampler2D lutTexture; \n" +
            "uniform lowp sampler2D sTexture; \n" +
            "varying highp vec2 vTextureCoord; \n" +
            " void main()\n" +
            " {\n" +
            "     highp vec4 textureColor = texture2D(sTexture, vTextureCoord);\n" +
            "     \n" +
            "     highp float blueColor = textureColor.b * 63.0;\n" +
            "     \n" +
            "     highp vec2 quad1;\n" +
            "     quad1.y = floor(floor(blueColor) / 8.0);\n" +
            "     quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +
            "     \n" +
            "     highp vec2 quad2;\n" +
            "     quad2.y = floor(ceil(blueColor) / 8.0);\n" +
            "     quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +
            "     \n" +
            "     highp vec2 texPos1;\n" +
            "     texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "     texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "     \n" +
            "     highp vec2 texPos2;\n" +
            "     texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "     texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "     \n" +
            "     lowp vec4 newColor1 = texture2D(lutTexture, texPos1);\n" +
            "     lowp vec4 newColor2 = texture2D(lutTexture, texPos2);\n" +
            "     \n" +
            "     gl_FragColor = mix(newColor1, newColor2, fract(blueColor));\n" +
            " }";

    public GlLargeLookupTableFilter(Bitmap lut) {
        super(DEFAULT_VERTEX_SHADER, LARGE_FRAGMENT_SHADER);
        lutTexture = lut;
        if (lutTexture.getWidth() != 512 || lutTexture.getHeight() != 512) {
            throw new IllegalArgumentException("expected 512x512 lut got " + lutTexture.getWidth() + "x" + lutTexture.getHeight());
        }
        hTex = EglUtil.NO_TEXTURE;
    }

    public GlLargeLookupTableFilter(Resources resources, int fxID) {
        super(DEFAULT_VERTEX_SHADER, LARGE_FRAGMENT_SHADER);
        this.lutTexture = BitmapFactory.decodeResource(resources, fxID);
        if (lutTexture.getWidth() != 512 || lutTexture.getHeight() != 512) {
            throw new IllegalArgumentException("expected 512x512 lut got " + lutTexture.getWidth() + "x" + lutTexture.getHeight());
        }
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
