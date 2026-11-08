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

package data.scripts.plugins.Render;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import data.scripts.plugins.RTSAssist;
import data.scripts.plugins.Render.RTS_drawManager.RTS_animator.RTS_Animator;
import data.scripts.plugins.Render.RTS_drawManager.RTS_DrawManager;
import data.scripts.plugins.Render.RTS_drawManager.RTS_FBO.RTS_FBOManager;
import data.scripts.plugins.Render.RTS_drawManager.RTS_GenericDrawMeth;
import data.scripts.plugins.Render.JXDOM.RTS_ComponentManager;
import data.scripts.plugins.Utils.RTS_StatefulClasses;

public class RTS_RenderManager extends RTS_StatefulClasses {

    public static class classidentifiers {
        public String componentManager = RTS_StatefulClasses.getUniqueIdentifier();
        public String drawManager = RTS_StatefulClasses.getUniqueIdentifier();
        public String animator = RTS_StatefulClasses.getUniqueIdentifier();
        public String root = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static classidentifiers stNames = new classidentifiers();

    public RTS_RenderManager (Object state) {
        super(state);
        this.init();
    }

    public static class zIndexBaseValues {
        public int UIBase = 50;
        public int fogOfWar = 45;
    }
    public static zIndexBaseValues ziNames = new zIndexBaseValues();

    private void init () {
//        RTS_GenericDrawMeth.rebuildShaders();

        this.setState(RTS_RenderManager.stNames.componentManager, new RTS_ComponentManager());
        this.setState(RTS_RenderManager.stNames.drawManager, new RTS_DrawManager(this.returnState()));
        this.setState(RTS_RenderManager.stNames.animator, new RTS_Animator(this.returnState()));
        this.setState(RTS_RenderManager.stNames.root, new RTS_Root(this.returnState()));
    }

    public void advance () {
        if (
                ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getCombatUI().isShowingCommandUI()
                || ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).isUIShowingDialog()
        )
            return;
        ((RTS_DrawManager)this.getState(RTS_RenderManager.stNames.drawManager)).open();
        ((RTS_Root)this.getState(RTS_RenderManager.stNames.root)).render();
        ((RTS_ComponentManager)this.getState(RTS_RenderManager.stNames.componentManager)).advance();
        ((RTS_Animator)this.getState(RTS_RenderManager.stNames.animator)).advance();
        ((RTS_DrawManager)this.getState(RTS_RenderManager.stNames.drawManager)).draw();
        ((RTS_DrawManager)this.getState(RTS_RenderManager.stNames.drawManager)).close();
    }
}































//    int vertexShader;
//    vertexShader = glCreateShader(GL_VERTEX_SHADER);
//    String vertex;
//            try {
//    vertex = Global.getSettings().loadText("data/shaders/blank.vert");
//            }
//                    catch (Exception ignored) {
//    vertex = null;
//            }
//    glShaderSource(vertexShader, vertex);
//    glCompileShader(vertexShader);
//
//    int fragmentShader;
//    fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
//    String fragment;
//            try {
//    fragment = Global.getSettings().loadText("data/shaders/test.frag");
//            }
//                    catch (Exception ignored) {
//    fragment = null;
//            }
//    glShaderSource(fragmentShader, fragment);
//    glCompileShader(fragmentShader);
//
//            this.shaderProgram = glCreateProgram();
//    glAttachShader(this.shaderProgram, vertexShader);
//    glAttachShader(this.shaderProgram, fragmentShader);
//    glLinkProgram(this.shaderProgram);
//    //        glUseProgram(shaderProgram);
//
//    glDeleteShader(vertexShader);
//    glDeleteShader(fragmentShader);
//
//    float[] vertices = {
//            -0.5f, -0.5f, 0.0f,
//            0.5f, -0.5f, 0.0f,
//            0.0f,  0.5f, 0.0f
//    };
//
//            this.VAO = GL30.glGenVertexArrays();
//            GL30.glBindVertexArray(this.VAO);
//    int VBO = GL15.glGenBuffers();
//    glBindBuffer(GL_ARRAY_BUFFER, VBO);
//    FloatBuffer buff = BufferUtils.createFloatBuffer(vertices.length);
//            buff.put(vertices);
//            buff.flip();
//    glBufferData(GL_ARRAY_BUFFER, buff, GL_STATIC_DRAW);
//
//
//
//    glEnableVertexAttribArray(0);
//    glBindBuffer(GL_ARRAY_BUFFER, 0);

//    FloatBuffer buff = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
