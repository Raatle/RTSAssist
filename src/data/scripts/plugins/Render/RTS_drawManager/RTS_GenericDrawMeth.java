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
import data.scripts.plugins.Utils.RTS_StatefulClasses;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class RTS_GenericDrawMeth {

    public static class shNames {
        public static String outLineProgram = RTS_StatefulClasses.getUniqueIdentifier();
        public static String monoColorProgram = RTS_StatefulClasses.getUniqueIdentifier();
        public static String quadToCircleProgram = RTS_StatefulClasses.getUniqueIdentifier();
        public static String quadToFCircleProgram = RTS_StatefulClasses.getUniqueIdentifier();
        public static String quadToCirclelineProgram = RTS_StatefulClasses.getUniqueIdentifier();
        public static String quadToFCircleProgramBiC = RTS_StatefulClasses.getUniqueIdentifier();
        public static String outlineQuadProgram = RTS_StatefulClasses.getUniqueIdentifier();
        public static String quadToLineProgram = RTS_StatefulClasses.getUniqueIdentifier();
    }

    private static HashMap<String, RTS_ShaderManager.shaderInfo> rawShaderStore = new HashMap<>() {{
        put(RTS_GenericDrawMeth.shNames.outlineQuadProgram, new RTS_ShaderManager.shaderInfo(
                "data/shaders/passCoOrd.vert",
                "data/shaders/fighterSquarePostShader.frag"
        ));
        put(shNames.quadToLineProgram, new RTS_ShaderManager.shaderInfo(
                "data/shaders/passCoOrd.vert",
                "data/shaders/RTS_SH_GenericDrawMeth/quadToLine.frag"
        ));

    }};

    static {
        for (Map.Entry<String, RTS_ShaderManager.shaderInfo> entry : RTS_GenericDrawMeth.rawShaderStore.entrySet())
            RTS_ShaderManager.loadProgram_LEGACY(entry.getKey(), entry.getValue());
    }

    private static int boundTexture = -1;

    //------------------------------------------------------------------------------------------------------------------

    public static void rebuildShaders () {
        for (Map.Entry<String, RTS_ShaderManager.shaderInfo> entry : RTS_GenericDrawMeth.rawShaderStore.entrySet())
            RTS_ShaderManager.loadProgram_LEGACY(entry.getKey(), entry.getValue());
        RTS_GenericDrawMeth.quadToCircleLineShader.rebuildShaders();
        RTS_GenericDrawMeth.circularizeAndFadeQuad.rebuildShaders();
        RTS_GenericDrawMeth.quadToOutline.rebuildShaders();
        RTS_GenericDrawMeth.quadToCircleSDF.rebuildShaders();
        RTS_GenericDrawMeth.quadToCircleSDFNegative.rebuildShader();
        RTS_GenericDrawMeth.addOutLineToQuad.rebuildShader();
        RTS_GenericDrawMeth.quadToCircleArrayUnion.rebuildShaders();
    }

    public static class quadToCircleLineShader extends RTS_ShaderManager.shaderPlugin_quad {

        private static int program;

        public static void rebuildShaders () {
            program = RTS_ShaderManager.loadProgram(
                    "data/shaders/passCoOrd.vert",
                    "data/shaders/RTS_SH_GenericDrawMeth/quadToCircleLine.frag"
            );
        }

        static {
            quadToCircleLineShader.rebuildShaders();
        }

        private static Color baseFadeColor = new Color(0, 0, 0, 255);

        private Vector2f rawCentre = new Vector2f(0f, 0f);
        private boolean inheritCentre = true;
        private float radius = 1f;
        private boolean inheritRadius = true;
        private Color colorLine = null;
        private boolean inheritColor = true;
        private Color colorFade = baseFadeColor;
        private float thickness = 1f;
        private float fadeThickness = 1f;
        private float fadeTransitionMod = 0f;
        private boolean fitVsEncircle = false;
        private float aliasing = 1.2f;

        @Override
        public RTS_ShaderManager.shaderPlugin_quad cloneShader () {
            RTS_GenericDrawMeth.quadToCircleLineShader hold = new quadToCircleLineShader();
            hold.rawCentre.set(rawCentre);
            hold.inheritCentre = inheritCentre;
            hold.radius = radius;
            hold.inheritRadius = inheritRadius;
            hold.colorLine = colorLine;
            hold.inheritColor = inheritColor;
            hold.colorFade = colorFade;
            hold.thickness = thickness;
            hold.fadeThickness = fadeThickness;
            hold.fadeTransitionMod = fadeTransitionMod;
            hold.fitVsEncircle = fitVsEncircle;
            hold.aliasing = aliasing;
            return (hold);
        }

        @Override
        public void run(RTS_DrawQuad.quadCall quadCall) {
            RTS_ShaderManager.useProgram(quadToCircleLineShader.program);

            int circleCentreLoc = GL20.glGetUniformLocation(quadToCircleLineShader.program, "uCircleCenter");
            Vector2f centre = RTS_DrawManager.adjustForVP(this.inheritCentre
                    ? new Vector2f(
                            quadCall.getPos().getX() + (quadCall.getSize().getX() / 2f),
                            quadCall.getPos().getY() + (quadCall.getSize().getY() / 2f)
                    )
                    : this.rawCentre
            );
            GL20.glUniform2f(
                    circleCentreLoc,
                    centre.getX(),
                    centre.getY()
            );

            int dimLoc = GL20.glGetUniformLocation(quadToCircleLineShader.program, "uResolution");
            GL20.glUniform2f(
                    dimLoc,
                    quadCall.getSize().getX(),
                    quadCall.getSize().getY()
            );

            int posLoc = GL20.glGetUniformLocation(quadToCircleLineShader.program, "uPosition");
            Vector2f position = RTS_DrawManager.adjustForVP(quadCall.getPos());
            GL20.glUniform2f(
                    posLoc,
                    position.getX(),
                    position.getY()
            );

            int radiusLoc = GL20.glGetUniformLocation(quadToCircleLineShader.program, "uRadius");
            GL20.glUniform1f(
                    radiusLoc,
                    this.inheritRadius ? Math.min(quadCall.getSize().getX(), quadCall.getSize().getY()) / 4f : this.radius
            );

            int colorLoc = GL20.glGetUniformLocation(quadToCircleLineShader.program, "uColor");
            if (this.inheritColor)
                GL20.glUniform4f(
                        colorLoc,
                        quadCall.getColor().getRed()/255f,
                        quadCall.getColor().getGreen()/255f,
                        quadCall.getColor().getBlue()/255f,
                        quadCall.getAlpha() == -1f ? quadCall.getColor().getAlpha()/255f : quadCall.getAlpha()
                );
            else
                GL20.glUniform4f(
                        colorLoc,
                        this.colorLine.getRed()/255f,
                        this.colorLine.getGreen()/255f,
                        this.colorLine.getBlue()/255f,
                        this.colorLine.getAlpha()/255f
                );

            int colorFadeLoc = GL20.glGetUniformLocation(quadToCircleLineShader.program, "uColorFade");
            GL20.glUniform4f(
                    colorFadeLoc,
                    this.colorFade.getRed()/255f,
                    this.colorFade.getGreen()/255f,
                    this.colorFade.getBlue()/255f,
                    this.colorFade.getAlpha()/255f
            );

            int thicknessLoc = GL20.glGetUniformLocation(quadToCircleLineShader.program, "uThickness");
            GL20.glUniform1f(
                    thicknessLoc,
                    this.thickness
            );

            int fadeThicknessLoc = GL20.glGetUniformLocation(quadToCircleLineShader.program, "uFadeThickness");
            GL20.glUniform1f(
                    fadeThicknessLoc,
                    this.fadeThickness
            );

            int fadeTransitionsLoc = GL20.glGetUniformLocation(quadToCircleLineShader.program, "uFadeTransition");
            GL20.glUniform1f(
                    fadeTransitionsLoc,
                    this.fadeTransitionMod
            );

            int fVSeLoc = GL20.glGetUniformLocation(quadToCircleLineShader.program, "uFitVsEncircle");
            GL20.glUniform1i(
                    fVSeLoc,
                    this.fitVsEncircle ? 1 : 0
            );

            int aliasingLoc = GL20.glGetUniformLocation(quadToCircleLineShader.program, "uAliasing");
            GL20.glUniform1f(
                    aliasingLoc,
                    this.aliasing
            );
        }

        public quadToCircleLineShader centre (Vector2f centre) {
            this.centre(centre.getX(), centre.getY());
            this.inheritCentre = false;
            return (this);
        }
        public quadToCircleLineShader centre (float x, float y) {
            this.rawCentre.set(x, y);
            this.inheritCentre = false;
            return (this);
        }

        public quadToCircleLineShader radius (float radius) {
            this.radius = radius;
            this.inheritRadius = false;
            return (this);
        }

        public quadToCircleLineShader color (Color color) {
            this.colorLine = color;
            this.inheritColor = false;
            return (this);
        }

        public quadToCircleLineShader colorFade (Color colorFade) {
            this.colorFade = colorFade;
            return (this);
        }

        public quadToCircleLineShader thickness (float thickness) {
            this.thickness = thickness;
            return (this);
        }

        public quadToCircleLineShader fitVsEncircle (boolean fitVsEncircle) {
            this.fitVsEncircle = fitVsEncircle;
            return (this);
        }

        public quadToCircleLineShader aliasing (float aliasing) {
            this.aliasing = aliasing;
            return (this);
        }

        public quadToCircleLineShader fadeThickness (float fadeThickness) {
            this.fadeThickness = fadeThickness;
            return (this);
        }

        public quadToCircleLineShader fadeTransitionMod (float FTM) {
            this.fadeTransitionMod = FTM;
            return (this);
        }

        @Override
        public void reset () {
            this.rawCentre.set(0f, 0f);
            this.inheritCentre = true;
            this.radius = 1f;
            this.inheritRadius = true;
            this.colorLine = null;
            this.inheritColor = true;
            this.colorFade = baseFadeColor;
            this.thickness = 1f;
            this.fadeThickness = 1f;
            this.fadeTransitionMod = 0f;
            this.fitVsEncircle = false;
            this.aliasing = 1.2f;
        }
    }

    public static class circularizeAndFadeQuad extends RTS_ShaderManager.shaderPlugin_quad {

        private static int programAlpha;
        private static int programBiC;

        public static void rebuildShaders () {
            circularizeAndFadeQuad.programAlpha = RTS_ShaderManager.loadProgram(
                    "data/shaders/passCoOrd.vert",
                    "data/shaders/RTS_SH_GenericDrawMeth/quadToFadingCircleAlpha.frag"
            );
            circularizeAndFadeQuad.programBiC = RTS_ShaderManager.loadProgram(
                    "data/shaders/passCoOrd.vert",
                    "data/shaders/RTS_SH_GenericDrawMeth/quadToFadingCircleBiColor.frag"
            );
        }

        static {
            circularizeAndFadeQuad.rebuildShaders();
        }

        private Vector2f rawCentre = new Vector2f(0f, 0f);
        private boolean inheritCentre = true;
        private float radius = 1f;
        private boolean inheritRadius = true;
        private Color color = null;
        private boolean inheritColor = true;
        private Color color1 = null;
        private float fadeStr = 1f;
        private float innDom = 1f;
        private float aliasing = 1.2f;

        @Override
        public RTS_ShaderManager.shaderPlugin_quad cloneShader() {
            RTS_GenericDrawMeth.circularizeAndFadeQuad hold = new circularizeAndFadeQuad();
            hold.rawCentre = rawCentre;
            hold.inheritCentre = inheritCentre;
            hold.radius = radius;
            hold.inheritRadius = inheritRadius;
            hold.color = color;
            hold.inheritColor = inheritColor;
            hold.color1 = color1;
            hold.fadeStr = fadeStr;
            hold.innDom = innDom;
            hold.aliasing = aliasing;
            return (hold);
        }

        @Override
        public void run(RTS_DrawQuad.quadCall quadCall) {
            int programID;
            if (color1 == null)
                programID = programAlpha;
            else
                programID = programBiC;

            RTS_ShaderManager.useProgram(programID);
            int circleCentreLoc = GL20.glGetUniformLocation(programID, "uCircleCenter");
            Vector2f centre = RTS_DrawManager.adjustForVP(this.inheritCentre
                    ? new Vector2f(
                            quadCall.getPos().getX() + (quadCall.getSize().getX() / 2f),
                            quadCall.getPos().getY() + (quadCall.getSize().getY() / 2f)
                    )
                    : this.rawCentre
            );
            GL20.glUniform2f(
                    circleCentreLoc,
                    centre.getX(),
                    centre.getY()
            );

            int dimLoc = GL20.glGetUniformLocation(programID, "uResolution");
            GL20.glUniform2f(
                    dimLoc,
                    quadCall.getSize().getX(),
                    quadCall.getSize().getY()
            );

            int posLoc = GL20.glGetUniformLocation(programID, "uPosition");
            Vector2f position = RTS_DrawManager.adjustForVP(quadCall.getPos());
            GL20.glUniform2f(
                    posLoc,
                    position.getX(),
                    position.getY()
            );

            int radiusLoc = GL20.glGetUniformLocation(programID, "uRadius");
            GL20.glUniform1f(
                    radiusLoc,
                    this.inheritRadius ? Math.min(quadCall.getSize().getX(), quadCall.getSize().getY()) / 4f : this.radius
            );

            int colorLoc = GL20.glGetUniformLocation(programID, "uColor");
            if (this.inheritColor)
                GL20.glUniform4f(
                        colorLoc,
                        quadCall.getColor().getRed()/255f,
                        quadCall.getColor().getGreen()/255f,
                        quadCall.getColor().getBlue()/255f,
                        quadCall.getColor().getAlpha()/255f
                );
            else
                GL20.glUniform4f(
                        colorLoc,
                        this.color.getRed()/255f,
                        this.color.getGreen()/255f,
                        this.color.getBlue()/255f,
                        this.color.getAlpha()/255f
                );

            if (color1 != null) {
                int color1Loc = GL20.glGetUniformLocation(programID, "uColor2");
                GL20.glUniform4f(
                        color1Loc,
                        color1.getRed() / 255f,
                        color1.getGreen() / 255f,
                        color1.getBlue() / 255f,
                        color1.getAlpha() / 255f
                );
            }

            int fadeLoc = GL20.glGetUniformLocation(programID, "uFadeSpeed");
            GL20.glUniform1f(
                    fadeLoc,
                    fadeStr
            );

            int innerDomLoc = GL20.glGetUniformLocation(programID, "uInnerDominence");
            GL20.glUniform1f(
                    innerDomLoc,
                    innDom
            );

            int aliasingLoc = GL20.glGetUniformLocation(programID, "uAliasing");
            GL20.glUniform1f(
                    aliasingLoc,
                    aliasing
            );
        }

        public circularizeAndFadeQuad centre (Vector2f pos) {
            this.rawCentre.set(pos);
            this.inheritCentre = false;
            return (this);
        }

        public circularizeAndFadeQuad centre (float x, float y) {
            this.rawCentre.set(x, y);
            this.inheritCentre = false;
            return (this);
        }

        public circularizeAndFadeQuad radius (float radius) {
            this.radius = radius;
            this.inheritRadius = false;
            return (this);
        }

        public circularizeAndFadeQuad color (Color color) {
            this.color = color;
            this.inheritColor = false;
            return (this);
        }

        public circularizeAndFadeQuad color1 (Color color1) {
            this.color1 = color1;
            return (this);
        }

        public circularizeAndFadeQuad fadeStrength (float fadeStr) {
            this.fadeStr = fadeStr;
            return (this);
        }

        public circularizeAndFadeQuad innerStrength (float innDom) {
            this.innDom = innDom;
            return (this);
        }

        public circularizeAndFadeQuad aliasing (float aliasing) {
            this.aliasing = aliasing;
            return (this);
        }

        @Override
        public void reset () {
            this.rawCentre.set(0f, 0f);
            this.inheritCentre = true;
            this.radius = 1f;
            this.inheritRadius = true;
            this.color = null;
            this.inheritColor = true;
            this.color1 = null;
            this.fadeStr = 1f;
            this.innDom = 1f;
            this.aliasing = 1.2f;
        }
    }

    public static class quadToOutline extends RTS_ShaderManager.shaderPlugin_quad {

        private static int program;

        public static void rebuildShaders () {
            quadToOutline.program = RTS_ShaderManager.loadProgram(
                    "data/shaders/passCoOrd.vert",
                    "data/shaders/RTS_SH_GenericDrawMeth/quadToLine.frag"
            );
        }

        static {
            quadToOutline.rebuildShaders();
        }

        private float lineThickness = 1f;
        private Vector2f size = new Vector2f(1f,1f);
        private float opacity = 1f;
        private float softness = 1f;

        @Override
        public RTS_ShaderManager.shaderPlugin_quad cloneShader() {
            RTS_GenericDrawMeth.quadToOutline hold = new quadToOutline();
            hold.param(this.lineThickness, this.size, this.opacity, this.softness);
            return (hold);
        }

        @Override
        public void run(RTS_DrawQuad.quadCall quadCall) {
            RTS_ShaderManager.useProgram(program);
            int lineThicknessLoc = GL20.glGetUniformLocation(program, "lineThickness");
            GL20.glUniform1f(
                    lineThicknessLoc,
                    lineThickness
            );
            int resolutionLoc = GL20.glGetUniformLocation(program, "u_resolution");
            GL20.glUniform2f(
                    resolutionLoc,
                    size.getX() / 2f,
                    size.getY() / 2f
            );
            int opacityLoc = GL20.glGetUniformLocation(program, "opacity");
            GL20.glUniform1f(
                    opacityLoc,
                    opacity
            );
            int softnessLoc = GL20.glGetUniformLocation(program, "softness");
            GL20.glUniform1f(
                    softnessLoc,
                    softness
            );
        }

        public quadToOutline lineThickness (float lineThickness) {
            this.lineThickness = lineThickness;
            return(this);
        }

        public quadToOutline size (Vector2f size) {
            this.size.set(size);
            return(this);
        }
        public quadToOutline size (float x, float y) {
            this.size.set(x, y);
            return(this);
        }

        public quadToOutline opacity (float opacity) {
            this.opacity = opacity;
            return(this);
        }

        public quadToOutline softness (float softness) {
            this.softness = softness;
            return(this);
        }

        public quadToOutline param (
                float lineThickness,
                Vector2f size,
                float opacity,
                float softness
        ) {
            this.lineThickness = lineThickness;
            this.size.set(size);
            this.opacity = opacity;
            this.softness = softness;
            return(this);
        }

        @Override
        public void reset () {
            this.lineThickness = 1f;
            this.size.set(1f, 1f);
            this.opacity = 1f;
            this.softness = 1f;
        }
    }

    public static class quadToCircleSDF extends RTS_ShaderManager.shaderPlugin_quad {

        private static int program;

        public static void rebuildShaders () {
            quadToCircleSDF.program = RTS_ShaderManager.loadProgram(
                    "data/shaders/passCoOrd.vert",
                    "data/shaders/RTS_SH_GenericDrawMeth/quadToCircleSDF.frag"
            );
        }

        static {
            quadToCircleSDF.rebuildShaders();
        }

        @Override
        public RTS_ShaderManager.shaderPlugin_quad cloneShader() {
            return (new quadToCircleSDF());
        }

        @Override
        public void run(RTS_DrawQuad.quadCall quadCall) {
            RTS_ShaderManager.useProgram(program);
        }

        @Override
        public void reset () {}
    }

    public static class quadToCircleSDFNegative extends RTS_ShaderManager.shaderPlugin_quad {

        public static int program;

        public static void rebuildShader () {
            quadToCircleSDFNegative.program = RTS_ShaderManager.loadProgram(
                    "data/shaders/passCoOrd.vert",
                    "data/shaders/RTS_SH_GenericDrawMeth/circleSDFToNegative.frag"
            );
        }

        static {
            quadToCircleSDFNegative.rebuildShader();
        }

        @Override
        public RTS_ShaderManager.shaderPlugin_quad cloneShader() {
            return (new quadToCircleSDFNegative());
        }

        @Override
        public void run(RTS_DrawQuad.quadCall quadCall) {
            RTS_ShaderManager.useProgram(program);
        }

        @Override
        public void reset () {}
    }

    public static class addOutLineToQuad extends RTS_ShaderManager.shaderPlugin_quad {

        public static int program;

        public static void rebuildShader () {
            addOutLineToQuad.program = RTS_ShaderManager.loadProgram(
                    "data/shaders/passCoOrd.vert",
                    "data/shaders/RTS_SH_GenericDrawMeth/outline.frag"
            );
        }

        static {
            addOutLineToQuad.rebuildShader();
        }

        Color baseColor = new Color(255, 255, 255, 255);

        private float thickness = 1f;
        private Color color = baseColor;

        @Override
        public RTS_ShaderManager.shaderPlugin_quad cloneShader () {
            RTS_GenericDrawMeth.addOutLineToQuad hold = new addOutLineToQuad();
            hold.thickness= thickness;
            hold.color = color;
            return (hold);
        }

        @Override
        public void run (RTS_DrawQuad.quadCall quadCall) {
            RTS_ShaderManager.useProgram(program);
            int texelSizeLoc = GL20.glGetUniformLocation(program, "uTexelSize");
            GL20.glUniform2f(
                    texelSizeLoc,
                    thickness,
                    thickness
            );

            int colorLoc = GL20.glGetUniformLocation(program, "uOutlineColor");
            GL20.glUniform4f(
                    colorLoc,
                    color.getRed()/255f,
                    color.getGreen()/255f,
                    color.getBlue()/255f,
                    color.getAlpha()/255f
            );
        }

        public addOutLineToQuad thickness (float thickness) {
            this.thickness = thickness;
            return (this);
        }

        public addOutLineToQuad color (Color color) {
            this.color = color;
            return (this);
        }

        @Override
        public void reset () {
            this.thickness = 1f;
            this.color = baseColor;
        }
    }

    public static class quadToCircleArrayUnion extends RTS_ShaderManager.shaderPlugin_quad {

        private static int program;

        public static void rebuildShaders () {
            program = RTS_ShaderManager.loadProgram(
                    "data/shaders/passCoOrd.vert",
                    "data/shaders/RTS_SH_GenericDrawMeth/circleUnionArray.frag"
            );
        }

        static {
            quadToCircleArrayUnion.rebuildShaders();
        }

        private static Color baseColor = new Color(255, 255, 255, 255);

        private Vector2f pos = null;
        private float blend = 1f;
        private float aliasing = 1f;
        private Color color = this.baseColor;
        private Vector2f dim = new Vector2f(1f,1f);
        private float[] xBuff = new float[100];
        private float[] yBuff = new float[100];
        private float[] rBuff = new float[100];
        private int index = 0;

        @Override
        public RTS_ShaderManager.shaderPlugin_quad cloneShader () {
            RTS_GenericDrawMeth.quadToCircleArrayUnion hold = new RTS_GenericDrawMeth.quadToCircleArrayUnion();
            if (pos == null)
                hold.pos = null;
            else
                hold.pos = new Vector2f(pos);
            hold.blend = blend;
            hold.aliasing = aliasing;
            hold.color = color;
            hold.dim.set(dim);
            for (int i = 0; i < this.index; i++) {
                hold.addCircle(
                        xBuff[i],
                        yBuff[i],
                        rBuff[i]
                );
            }
            hold.index = index;
            return (hold);
        }

        @Override
        public void run (RTS_DrawQuad.quadCall quadCall) {
            RTS_ShaderManager.useProgram(quadToCircleArrayUnion.program);

            int circArrLoc = GL20.glGetUniformLocation(program, "uCircles");
            FloatBuffer buffer = BufferUtils.createFloatBuffer(300);
            Vector2f vecPoi = new Vector2f();
            for (int i = 0; i < index; i++) {
                vecPoi.set(xBuff[i], yBuff[i]);
                RTS_DrawManager.adjustForVPunSafe(vecPoi);
                buffer.put(vecPoi.getX());
                buffer.put(vecPoi.getY());
                buffer.put(rBuff[i]);
            }
            buffer.flip();
            GL20.glUniform3(
                    circArrLoc,
                    buffer
            );

            int indexArrLoc = GL20.glGetUniformLocation(program, "uSize");
            GL20.glUniform1i(indexArrLoc, index);

            int sizeLoc = GL20.glGetUniformLocation(program, "uResolution");
            GL20.glUniform2f(
                    sizeLoc,
                    dim.getX(),
                    dim.getY()
            );

            int posLoc = GL20.glGetUniformLocation(program, "uPosition");
            Vector2f position = RTS_DrawManager.adjustForVP(pos);
            GL20.glUniform2f(
                    posLoc,
                    position.getX(),
                    position.getY()
            );

            int blendLoc = GL20.glGetUniformLocation(program, "uBlend");
            GL20.glUniform1f(blendLoc, blend * 100f);

            int aliasLoc = GL20.glGetUniformLocation(program, "uAliasing");
            GL20.glUniform1f(aliasLoc, 1f / (aliasing * 100f));

            int colorLoc = GL20.glGetUniformLocation(program, "uColor");
            GL20.glUniform4f(
                    colorLoc,
                    color.getRed()/255f,
                    color.getGreen()/255f,
                    color.getBlue()/255f,
                    color.getAlpha()/255f
            );
        }

        public quadToCircleArrayUnion addCircle (Vector3f circle) {
            this.addCircle(circle.getX(), circle.getY(), circle.getZ());
            return (this);
        }
        public quadToCircleArrayUnion addCircle (Vector2f location, float radius) {
            this.addCircle(location.getX(), location.getY(), radius);
            return (this);
        }
        public quadToCircleArrayUnion addCircle (float x, float y, float r) {
            this.xBuff[this.index] = x;
            this.yBuff[this.index] = y;
            this.rBuff[this.index] = r;
            this.index++;
            return (this);
        }

        public quadToCircleArrayUnion clearCircles () {
            this.index = 0;
            return (this);
        }

        public quadToCircleArrayUnion pos (float x, float y) {
            this.pos = new Vector2f(x, y);
            return (this);
        }
        public quadToCircleArrayUnion pos (Vector2f pos) {
            this.pos = pos;
            return (this);
        }

        public quadToCircleArrayUnion blend (float blend) {
            this.blend = blend;
            return (this);
        }

        public quadToCircleArrayUnion aliasing (float aliasing) {
            this.aliasing = aliasing;
            return (this);
        }

        public quadToCircleArrayUnion color (Color color) {
            this.color = color;
            return (this);
        }

        public quadToCircleArrayUnion size (Vector2f dimensions) {
            this.dim.set(dimensions);
            return (this);
        }
        public quadToCircleArrayUnion size (float x, float y) {
            this.dim.set(x, y);
            return (this);
        }
        public quadToCircleArrayUnion size (float xy) {
            this.dim.set(xy, xy);
            return (this);
        }

        @Override
        public void reset () {
            this.pos = null;
            this.color = baseColor;
            this.blend = 1f;
            this.aliasing = 1f;
            this.dim.set(1f, 1f);
            this.index = 0;
        }
    }

    public static void quadToLine_LEGACY (float lineThickness, Vector2f dimensions, float opacity, float softness) {
        int programID = RTS_ShaderManager.getProgram(shNames.quadToLineProgram);
        RTS_ShaderManager.useProgram(programID);
        int lineThicknessLoc = GL20.glGetUniformLocation(programID, "lineThickness");
        GL20.glUniform1f(
                lineThicknessLoc,
                lineThickness
        );
        int resolutionLoc = GL20.glGetUniformLocation(programID, "u_resolution");
        GL20.glUniform2f(
                resolutionLoc,
                dimensions.getX() / 2f,
                dimensions.getY() / 2f
        );
        int opacityLoc = GL20.glGetUniformLocation(programID, "opacity");
        GL20.glUniform1f(
                opacityLoc,
                opacity
        );
        int softnessLoc = GL20.glGetUniformLocation(programID, "softness");
        GL20.glUniform1f(
                softnessLoc,
                softness
        );
    }

    //------------------------------------------------------------------------------------------------------------------

    public record quadRec (
            Vector2f pos,
            Vector2f dim,
            Color color
    ) {}

    public record quadConstraint (
            float xMin,
            float xMax,
            float yMin,
            float yMax
    ) {}

    public static void viewPortBoxOutline_LEGACY (quadRec entry, float lineThickness, float opacity, float softness, quadConstraint c) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        RTS_GenericDrawMeth.unbindTexture();

        float w2 = entry.dim.getX() / 2f;
        float h2 = entry.dim.getY() / 2f;
        if (c == null)
            RTS_GenericDrawMeth.quadToLine_LEGACY(lineThickness, entry.dim, opacity, softness);
        else {
            Vector2f hold = new Vector2f(
                    MathUtils.clamp(entry.pos().getX() + w2, c.xMin(), c.xMax())
                            - MathUtils.clamp(entry.pos().getX() - w2, c.xMin(), c.xMax()),
                    MathUtils.clamp(entry.pos().getY() + h2, c.yMin(), c.yMax())
                            - MathUtils.clamp(entry.pos().getY() - h2, c.yMin(), c.yMax())
            );
            if (hold.getX() <= 0 || hold.getY() <= 0)
                return;
            RTS_GenericDrawMeth.quadToLine_LEGACY(lineThickness, hold, opacity, softness);
        }
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(
                entry.color().getRed(),
                entry.color().getGreen(),
                entry.color().getBlue(),
                entry.color().getAlpha()
        );
        if (c == null) {
            GL11.glTexCoord2f(0.0f, 0.0f);
            GL11.glVertex2f(entry.pos().getX() - w2, entry.pos().getY() - h2);
            GL11.glTexCoord2f(1.0f, 0.0f);
            GL11.glVertex2f(entry.pos().getX() + w2, entry.pos().getY() - h2);
            GL11.glTexCoord2f(1.0f, 1.0f);
            GL11.glVertex2f(entry.pos().getX() + w2, entry.pos().getY() + h2);
            GL11.glTexCoord2f(0.0f, 1.0f);
            GL11.glVertex2f(entry.pos().getX() - w2, entry.pos().getY() + h2);
        }
        else {
            GL11.glTexCoord2f(0.0f, 0.0f);
            GL11.glVertex2f(
                    MathUtils.clamp(entry.pos().getX() - w2, c.xMin(), c.xMax()),
                    MathUtils.clamp(entry.pos().getY() - h2, c.yMin(), c.yMax())
            );
            GL11.glTexCoord2f(1.0f, 0.0f);
            GL11.glVertex2f(
                    MathUtils.clamp(entry.pos().getX() + w2, c.xMin(), c.xMax()),
                    MathUtils.clamp(entry.pos().getY() - h2, c.yMin(), c.yMax())
            );
            GL11.glTexCoord2f(1.0f, 1.0f);
            GL11.glVertex2f(
                    MathUtils.clamp(entry.pos().getX() + w2, c.xMin(), c.xMax()),
                    MathUtils.clamp(entry.pos().getY() + h2, c.yMin(), c.yMax())
            );
            GL11.glTexCoord2f(0.0f, 1.0f);
            GL11.glVertex2f(
                    MathUtils.clamp(entry.pos().getX() - w2, c.xMin(), c.xMax()),
                    MathUtils.clamp(entry.pos().getY() + h2, c.yMin(), c.yMax())
            );
        }
        GL11.glEnd();
    }

    //------------------------------------------------------------------------------------------------------------------

    public static void resetTextureDebouncer() {
        RTS_GenericDrawMeth.boundTexture = -1;
    }

    public static void bindTexture (int tex) {
        if (RTS_GenericDrawMeth.boundTexture == tex)
            return;
        RTS_GenericDrawMeth.boundTexture = tex;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
    }

    public static void unbindTexture () {
        if (RTS_GenericDrawMeth.boundTexture == 0)
            return;
        RTS_GenericDrawMeth.boundTexture = 0;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    //------------------------------------------------------------------------------------------------------------------
    
    public static SpriteAPI crudeSpriteBuilder (int textureID, Vector2f dimensions) {
        SpriteAPI hold = new SpriteAPI() {
            int tex = textureID;
            
            @Override
            public void setBlendFunc(int src, int dest) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public void setNormalBlend() {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public void setAdditiveBlend() {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public void setCenter(float x, float y) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public void setSize(float width, float height) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public float getAngle() {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public void setAngle(float angle) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public Color getColor() {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public void setColor(Color color) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public void setHeight(float height) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public void setWidth(float width) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public void renderAtCenter(float x, float y) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public void render(float x, float y) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public void renderRegionAtCenter(float x, float y, float tx, float ty, float tw, float th) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public void renderRegion(float x, float y, float tx, float ty, float tw, float th) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public float getAlphaMult() {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public void setAlphaMult(float alphaMult) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            @Override
            public void setCenterY(float cy) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public void setCenterX(float cx) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public Color getAverageColor() {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public void setTexX(float texX) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public void setTexY(float texY) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public void setTexWidth(float texWidth) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public void setTexHeight(float texHeight) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public void renderWithCorners(float blX, float blY, float tlX, float tlY, float trX, float trY, float brX, float brY) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public Color getAverageBrightColor() {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public void renderNoBind(float x, float y) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public void renderAtCenterNoBind(float x, float y) {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public int getBlendDest() {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public int getBlendSrc() {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public float getTexX() {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public float getTexY() {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public float getTexWidth() {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }

            @Override
            public float getTexHeight() {
                throw new RuntimeException("RTSAssist: you have called a method on a SpriteAPI instance that was built by crudeSpriteBuilder. This method is not supported.");
            }
            //------------------------------//
            @Override
            public float getHeight() {
                return (dimensions.getY());
            }

            @Override
            public float getWidth() {
                return (dimensions.getX());
            }

            @Override
            public void bindTexture() {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.tex);
            }

            @Override
            public int getTextureId() {
                return(this.tex);
            }

            @Override
            public float getCenterX() {
                return (dimensions.getX() / 2f);
            }

            @Override
            public float getCenterY() {
                return (dimensions.getY() / 2f);
            }

            @Override
            public float getTextureWidth() {
                return (1f);
            }

            @Override
            public float getTextureHeight() {
                return (1f);
            }
        };
        return (hold);
    }
}
