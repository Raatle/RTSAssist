/*
  **********************************************************************************************************
  * RTSAssist version 0.2.04exp
  * Copyright (C) 2025-2026, Raatle

  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.

  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.

  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
  **********************************************************************************************************
 */

package data.scripts.plugins.Render.RTS_drawManager.RTS_FBO;

import com.fs.starfarer.api.graphics.SpriteAPI;
import data.scripts.plugins.Render.RTS_drawManager.RTS_DrawManager;
import data.scripts.plugins.Render.RTS_drawManager.RTS_GenericDrawMeth;
import data.scripts.plugins.Render.RTS_drawManager.RTS_ShaderManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;

public class RTS_FBOManager {

    public static int boundFBO = -1;

    public static void prepareBufferWrapper () {
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glClearColor(0, 0, 0, 0); // Clear to total transparency
    }

    public static RTS_FBO buildFBO (Vector2f dimensions) {
        Vector2f flatDim = new Vector2f(
                Math.round(dimensions.getX()),
                Math.round(dimensions.getY())
        );
        int fboID = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
        int fboTextureID = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fboTextureID);
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA16,
                ((Float)flatDim.getX()).intValue(),
                ((Float)flatDim.getY()).intValue(),
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                (java.nio.ByteBuffer)null
        );
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL30.glFramebufferTexture2D(
                GL30.GL_FRAMEBUFFER,
                GL30.GL_COLOR_ATTACHMENT0,
                GL11.GL_TEXTURE_2D, fboTextureID,
                0
        );
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        return (new RTS_FBO(
                fboID,
                fboTextureID,
                flatDim
        ));
    }

    public static void destroyFBO (RTS_FBO FBO) {
        if (FBO.ID() != 0)
            GL30.glDeleteFramebuffers(FBO.ID());
        if (FBO.tex() != 0)
            GL11.glDeleteTextures(FBO.tex());
    }

    public static void bindFBO (RTS_FBO FBO) {
        RTS_FBOManager.bindFBO(FBO, null, false);
    }
    public static void bindFBO (RTS_FBO FBO, Vector2f offset) {
        RTS_FBOManager.bindFBO(FBO, offset, false);
    }
    public static void bindFBO (RTS_FBO FBO, Vector2f offset, boolean dontClear) {
        if (RTS_FBOManager.boundFBO == FBO.ID())
            return;
        RTS_FBOManager.boundFBO = FBO.ID();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, FBO.ID());
        if (!dontClear)
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        if (offset != null)
            RTS_DrawManager.adjustViewPort(
                    ((Float)offset.getX()).intValue() * -1,
                    ((Float)offset.getY()).intValue() * -1,
                    RTS_DrawManager.viewPort.width,
                    RTS_DrawManager.viewPort.height
            );
    }

    public static void unbindFBO () {
        if (RTS_FBOManager.boundFBO == 0)
            return;
        RTS_FBOManager.boundFBO = 0;
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        RTS_DrawManager.resetViewPort();
    }

    public static void paintFBO (RTS_PaintJob paintJob) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RTS_GenericDrawMeth.bindTexture(paintJob.FBO().tex());
        if (paintJob.filterProgram() != null) {
            if (paintJob.filterProgram() != 0)
                RTS_ShaderManager.useProgram(paintJob.filterProgram());
            else
                RTS_ShaderManager.clearProgram();
        }
        GL11.glBegin(GL11.GL_QUADS);
        if (paintJob.color() == null)
            GL11.glColor4f(1f, 1f, 1f, 1f);
        else
            GL11.glColor4f(
                    paintJob.color().getRed() / 255f,
                    paintJob.color().getGreen() / 255f,
                    paintJob.color().getBlue() / 255f,
                    paintJob.color().getAlpha() / 255f
            );
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2f(
                paintJob.bottomLeft().getX(),
                paintJob.bottomLeft().getY()
        );
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2f(
                paintJob.bottomLeft().getX() + paintJob.FBO().dimensions().getX(),
                paintJob.bottomLeft().getY()
        );
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2f(
                paintJob.bottomLeft().getX() + paintJob.FBO().dimensions().getX(),
                paintJob.bottomLeft().getY() + paintJob.FBO().dimensions().getY()
        );
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2f(
                paintJob.bottomLeft().getX(),
                paintJob.bottomLeft().getY() + paintJob.FBO().dimensions().getY()
        );
        GL11.glEnd();
    }

    public static RTS_BoundTexture buildTextureFBO (SpriteAPI sprite, float margin) {
        Vector2f fboDim = new Vector2f();
        float spriteDim = Math.max(sprite.getWidth(), sprite.getHeight());
        fboDim.set(spriteDim + margin, spriteDim + margin);
//        for (float i = margin; i <= 5000f ; i = i + margin) {
//            if (spriteDim < i) {
//                fboDim.set(i + margin, i + margin);
//                break;
//            }
//        }
        RTS_FBO FBO = RTS_FBOManager.buildFBO(fboDim);

        RTS_FBOManager.bindFBO(FBO);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        RTS_GenericDrawMeth.bindTexture(sprite.getTextureId());
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float textureWidth = sprite.getTextureWidth();
        float textureHeight = sprite.getTextureHeight();
        float w = sprite.getWidth();
        float h = sprite.getHeight();

        GL11.glTranslatef(fboDim.getX() / 2f, fboDim.getY() / 2f, 0);
        GL11.glTranslatef((-w / 2f), (-h / 2f), 0);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        RTS_ShaderManager.clearProgram();

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(0f, 0f);

        GL11.glTexCoord2f(textureWidth, 0f);
        GL11.glVertex2f(w, 0f);

        GL11.glTexCoord2f(textureWidth, textureHeight);
        GL11.glVertex2f(w, h);

        GL11.glTexCoord2f(0f, textureHeight);
        GL11.glVertex2f(0f, h);

        GL11.glEnd();

        GL11.glTranslatef((w / 2f), (h / 2f), 0);
        GL11.glTranslatef(-fboDim.getX() / 2f, -fboDim.getY() / 2f, 0);

        RTS_FBOManager.unbindFBO();
        return (new RTS_BoundTexture(
                FBO.ID(),
                sprite,
                RTS_GenericDrawMeth.crudeSpriteBuilder(FBO.tex(), FBO.dimensions())
        ));
    }

    public static void destroyTextureFBO (RTS_BoundTexture texture) {
        if (texture.FBOID() != 0)
            GL30.glDeleteFramebuffers(texture.FBOID());
        if (texture.newSprite().getTextureId() != 0)
            GL11.glDeleteTextures(texture.newSprite().getTextureId());
    }

    public static boolean isFBO () {
        return (RTS_FBOManager.boundFBO > 0);
    }
}
