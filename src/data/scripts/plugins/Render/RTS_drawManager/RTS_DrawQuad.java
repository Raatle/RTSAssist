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

package data.scripts.plugins.Render.RTS_drawManager;

import com.fs.starfarer.api.graphics.SpriteAPI;
import data.scripts.plugins.Render.RTS_drawManager.RTS_FBO.RTS_FBOManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RTS_DrawQuad {

    //------------------------------------------------------------------------------------------------------------------

    private static HashMap<Integer, List<Integer>> dsBuff = new HashMap<>();

    //------------------------------------------------------------------------------------------------------------------

    /**
     * QuadCall:
     * <pre>
     *  * A builder pattern class that simplifies the process of drawing a sprite or quad.
     *  * Operated by setting internal state and then calling render().
     *  * Natively supports batching. Push() adds the current state to a stack.
     *  * Render() draws the stack instead.
     *  * By default, state and stack are reset on render().</pre>
     * Examples:
     * <pre>
     *     {@code
     *          quadBuilder.size(20f).pos(100f, 100f).render();
     *
     *          for (ShipAPI ship : alliedShips) {
     *              quadBuilder
     *                  .sprite(ship.getSprite())
     *                  .size(1.5f)
     *                  .facing(ship.getFacing())
     *                  .filter(outlineShader)
     *                      .thickness(5f)
     *                      .color(red)
     *                  .set()
     *                  .pos(ship.getLocation())
     *                  .push()
     *          }
     *          quadBuilder.render();
     *     }
     * </pre>
     */
    public static class quadCall {

        public interface blendFunc {
            public void setBlend ();
        }

        protected List<quadCall> callStore = new ArrayList<>();
        private boolean active = true;

        private quadCall.blendFunc baseblendFunc = new quadCall.blendFunc() {
            @Override
            public void setBlend() {
                GL11.glEnable(GL11.GL_BLEND);
                if (RTS_FBOManager.isFBO()) {
//                    GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GL14.glBlendFuncSeparate(
                            GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                            GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA
                    );
                }
                else
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }
        };

        private Color baseColor = new Color(1f, 1f, 1f, 1f);

        private SpriteAPI sprite = null;
        private Vector2f position = new Vector2f(0f, 0f);
        private Vector2f size = new Vector2f(1f, 1f);
        private float facing = 0f;
        private float alpha = -1f;
        private Color color = null;
        private RTS_ShaderManager.shaderPlugin_quad filter = null;
        private quadCall.blendFunc blendFunc = this.baseblendFunc;
        private Float staggerGPU = null;

        //  *#######*
        //   Methods
        //  *#######*
        /**
         * Define sprite. If left empty draw a quad, else draw the sprite.
         * @param sprite A SpriteAPI interface belonging to the SS API.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall sprite (SpriteAPI sprite) {
            this.sprite = sprite;
            return (this);
        }

        public SpriteAPI getSprite () {
            return (this.sprite);
        }
        /**
         * Define position by x and y coordinates, from bottom left. Places quad centre at pos.
         * @param x x cordinate (float)
         * @param y y cordinate (float)
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall pos (float x, float y) {
            this.position.set(x, y);
            return (this);
        }
        /**
         * Define position by Vector2f, from bottom left. Places quad centre at pos.
         * @param pos x and y coordinates stored in Vector2f, not modified.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall pos (Vector2f pos) {
            this.position.set(pos);
            return (this);
        }

        public Vector2f getPos () {
            return (this.position);
        }
        /**
         * Define quad size. Take care: If sprite is defined this is a modifier,
         * else this defines quad size explicitely. Define both x and y using single value.
         * @param size float representing both length and height.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall size (float size) {
            this.size.set(size, size);
            return (this);
        }
        /**
         * Define quad size. Take care: If sprite is defined this is a modifier,
         * else this defines quad size explicitely. Define both x and y seperately using individual float values.
         * @param x float representing Length.
         * @param y float representing Height.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall size (float x, float y) {
            this.size.set(x, y);
            return (this);
        }
        /**
         * Define quad size. Take care: If sprite is defined this is a modifier,
         * else this defines quad size explicitely. Define both x and y seperately by setting Vector2f state.
         * @param size Vector2f representing both length and height.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall size (Vector2f size) {
            this.size.set(size);
            return (this);
        }

        public Vector2f getSize () {
            if (this.sprite != null) {
                return (new Vector2f(
                        this.sprite.getWidth() * this.size.getX(),
                        this.sprite.getHeight() * this.size.getY()
                ));
            }
            return (this.size);
        }
        /**
         * Define rotation about sprite centre.
         * @param facing float representing facing, defined counterclockwise.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall facing (float facing) {
            this.facing = facing;
            return (this);
        }

        public float getFacing () {
            return (this.facing);
        }
        /**
         * Define an Alpha override that is used regardless of color setting.
         * @param alpha float value between 0 and 1.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall alpha (float alpha) {
            this.alpha = alpha;
            return (this);
        }

        public float getAlpha () {
            return (this.alpha);
        }
        /**
         * Define java.awt.Color. If left blank default color of white pure is used.
         * @param color java.awt.Color.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall color (Color color) {
            this.color = color;
            return (this);
        }

        public Color getColor () {
            if (this.color == null) {
                return (this.baseColor);
            }
            return (this.color);
        }
        /**
         * Define bounded generic extending RTS_ShaderManager.shaderPlugin. Used to add shader
         * functionality to drawCall. RTS_DrawQuad.drawQuads will call RTS_ShaderManager.shaderPlugin.run()
         * prior to drawing quad. If unset will clear any active shaders.
         * @param filter bounded generic RTS_ShaderManager.shaderPlugin
         * @return returns the RTS_ShaderManager.shaderPlugin instance, setting "build space" to inside the shader,
         * which also supports builder pattern functionality.
         * Follow with RTS_ShaderManager.shaderPlugin.set() to return "build space" to this quadCall instance.
         * See class javadoc for example.
         */
        public <shader extends RTS_ShaderManager.shaderPlugin_quad> shader filter (shader filter) {
            filter.bindCaller(this);
            this.filter = filter;
            return (filter);
        }
        /**
         * Set the filter back to null
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall unSetFilter () {
            this.filter = null;
            return (this);
        }

        public RTS_ShaderManager.shaderPlugin_quad getFilter () {
            return (this.filter);
        }
        /**
         * Define function that, if defined, will override the default blend function.
         * @param blendFunc eg., () -> GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall blendFunc (quadCall.blendFunc blendFunc) {
            this.blendFunc = blendFunc;
            return (this);
        }

        public quadCall.blendFunc getBlendFunc () {
            return (this.blendFunc);
        }
        /**
         * Define Float  that, if defined, will hang the CPU after each call.
         * @param milliSec How long to hang the CPU;
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall setStagger(Float milliSec) {
            this.staggerGPU = milliSec;
            return (this);
        }

        public Float getStagger () {
            return (this.staggerGPU);
        }
        /**
         * Reset this quadCall instance to default state. RTS_DrawQuad.drawQuads calls this by default.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall reset () {
            this.sprite = null;
            this.position.set(0f, 0f);
            this.size.set(1f, 1f);
            this.facing = 0f;
            this.alpha = -1f;
            this.color = null;
            this.filter = null;
            this.blendFunc = this.baseblendFunc;
            this.staggerGPU = null;
            return (this);
        }

        //  *#########*
        //   Utilities
        //  *#########*

        private quadCall spriteCallBuilder (quadCall base, quadCall buff) {
            quadCall hold = buff;
            if (hold != null)
                hold.reset();
            else
                hold = new quadCall();
            hold
                    .sprite(base.sprite)
                    .pos(base.position)
                    .size(base.size)
                    .facing(base.facing)
                    .alpha(base.alpha)
                    .color(base.color)
                    .blendFunc(base.blendFunc)
                    .setStagger(base.staggerGPU);
            if (base.getFilter() != null)
                hold.filter(base.getFilter().cloneShader());
            hold.active = true;
            return (hold);
        }
        /**
         * Push the current State onto the queue. RTS_DrawQuad.drawQuads will now render the queue instead
         * of quadCalls current state.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall push () {
            if (this.callStore.isEmpty()) {
                this.callStore.add(this.spriteCallBuilder(this, null));
                return (this);
            }
            for (int targetNode = 0; true; targetNode++){
                if (!this.callStore.get(targetNode).active) {
                    this.spriteCallBuilder(this, this.callStore.get(targetNode));
                    return (this);
                }
                if ((targetNode + 1) == this.callStore.size()) {
                    this.callStore.add(this.spriteCallBuilder(this, null));
                    return (this);
                }
            }
        }
        /**
         * Pop the queue. If the queue becomes empty RTS_DrawQuad.drawQuads will read this instance
         * instead of the queue.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall pop () {
            if (this.callStore.isEmpty() || !this.callStore.get(0).active)
                return (this);
            int targetNode;
            for (targetNode = 0; true; targetNode++)
                if ((targetNode + 1) == this.callStore.size() || !this.callStore.get(targetNode).active)
                    break;
            targetNode--;
            this.callStore.get(targetNode).active = false;
            return (this);
        }
        /**
         * Clear the queue. If the queue becomes empty RTS_DrawQuad.drawQuads will read this instance
         * instead of the queue.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall clearStore () {
            for (quadCall call : this.callStore)
                call.active = false;
            return (this);
        }
        /**
         * Call RTS_DrawQuad.drawQuads passing this quadCall as its parameter.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall render () {
            RTS_DrawQuad.drawQuads(this);
            return (this);
        }
        /**
         * Call RTS_DrawQuad.drawQuads passing this quadCall as its primary parameter.
         * @param dontClear by default, RTS_DrawQuad.drawQuads clears the quadCall.
         *                  If true, the quadCall state is preserved.
         * @param dontBatch by default, RTS_DrawQuad.drawQuads attempts to batch drawcalls by texture id.
         *                  If true, drawcalls are rendered as they were added.
         * @return returns this quadCall instance, supporting builder pattern functionality.
         */
        public quadCall render (boolean dontClear, boolean dontBatch) {
            RTS_DrawQuad.drawQuads(this, dontClear, dontBatch);
            return (this);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    public static void drawQuads (quadCall call) {
        RTS_DrawQuad.drawQuads(call, false, false);
    }
    public static void drawQuads (quadCall call, boolean dontClear, boolean dontBatch) {
        // See *.
        boolean emptyStore = true;
        // If dontBatch is set to true, push all queue into a single array
        if (dontBatch) {
            if (!RTS_DrawQuad.dsBuff.containsKey(-1))
                RTS_DrawQuad.dsBuff.put(-1, new ArrayList<>());
            for (int i = 0; i < call.callStore.size(); i++) {
                if (!call.callStore.get(i).active)
                    continue;
                emptyStore = false;
                RTS_DrawQuad.dsBuff.get(-1).add(i);
            }
        }
        // By default, batch all calls in queue by their texture id as to minimise expensive bindTexture() calls.
        int texID;
        for (int i = 0; i < call.callStore.size(); i++) {
            if (!call.callStore.get(i).active)
                continue;
            emptyStore = false;
            texID = call.callStore.get(i).getSprite() != null
                    ? call.callStore.get(i).getSprite().getTextureId()
                    : -1;
            if (RTS_DrawQuad.dsBuff.containsKey(texID)) {
                RTS_DrawQuad.dsBuff.get(texID).add(i);
            }
            else {
                RTS_DrawQuad.dsBuff.put(texID, new ArrayList<>());
                RTS_DrawQuad.dsBuff.get(texID).add(i);
            }
        }
        // * If quadCall has no queue we look directly at the quadcall attributes.
        if (emptyStore)
            RTS_DrawQuad.renderQuad(call);
        // loop through the sorted quadCall queue.
        else
            for (Map.Entry<Integer, List<Integer>> entry : RTS_DrawQuad.dsBuff.entrySet()) {
                for (Integer index : entry.getValue()) {
                    RTS_DrawQuad.renderQuad(call.callStore.get(index));
                }
            }
        // cleanup for next call
        for (Map.Entry<Integer, List<Integer>> entry : RTS_DrawQuad.dsBuff.entrySet())
            entry.getValue().clear();
        // by default quadcall is reset upon draw. if true maintain quadcall state.
        if (!dontClear) {
            call.reset();
            call.clearStore();
        }
    }

    private static void renderQuad (quadCall callPointer) {
        Vector2f size = callPointer.getSize();
        float w = size.getX();
        float h = size.getY();
        float textureWidth;
        float textureHeight;
        // drawing a sprite.
        if (callPointer.getSprite() != null) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            RTS_GenericDrawMeth.bindTexture(callPointer.getSprite().getTextureId());
            textureWidth = callPointer.getSprite().getTextureWidth();
            textureHeight = callPointer.getSprite().getTextureHeight();
        }
        // drawing a quad.
        else {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            RTS_GenericDrawMeth.unbindTexture();
            textureWidth = 1f;
            textureHeight = 1f;
        }
        // if a shader is set run it here, else remove any active shaders.
        if (callPointer.getFilter() != null)
            callPointer.getFilter().run(callPointer);
        else
            RTS_ShaderManager.clearProgram();
        // run the blend fucntion.
        callPointer.getBlendFunc().setBlend();
        // define color. If a custom alpha is set override the colors set alpha.
        GL11.glColor4f(
                ((Integer)(callPointer.getColor().getRed())).floatValue() / 255f,
                ((Integer)(callPointer.getColor().getGreen())).floatValue() / 255f,
                ((Integer)(callPointer.getColor().getBlue())).floatValue() / 255f,
                callPointer.getAlpha() == -1
                        ? ((Integer)(callPointer.getColor().getAlpha())).floatValue() / 255f
                        : callPointer.getAlpha()
        );
        // translate and rotate the drawcall.
        GL11.glTranslatef(callPointer.getPos().getX(), callPointer.getPos().getY(), 0);
        GL11.glRotatef(callPointer.getFacing(), 0, 0, 1);
        GL11.glTranslatef(-w / 2, -h / 2, 0);
        // draw.
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
        // reset the translate/rotate operation.
        GL11.glTranslatef(w / 2, h / 2, 0);
        GL11.glRotatef(-callPointer.getFacing(), 0, 0, 1);
        GL11.glTranslatef(-callPointer.getPos().getX(), -callPointer.getPos().getY(), 0);
        if (callPointer.getStagger() != null) {
            GL11.glFinish();
            try {
                Thread.sleep(callPointer.getStagger().longValue());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------
}
