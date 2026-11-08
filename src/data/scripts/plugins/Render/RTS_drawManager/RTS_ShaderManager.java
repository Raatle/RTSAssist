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

import com.fs.starfarer.api.Global;
import data.scripts.plugins.Utils.RTS_StatefulClasses;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.util.HashMap;

public class RTS_ShaderManager extends RTS_StatefulClasses {

    public RTS_ShaderManager() {}

    private static int activeProgram = 0;
    private static HashMap<String, Integer> loadedShaders = new HashMap<>();
    private static boolean hardfail = false;

    //------------------------------------------------------------------------------------------------------------------

    public record shaderInfo(
            String vertexDir,
            String fragDir
    ) {}

    public static abstract class shaderPlugin_quad {

        RTS_DrawQuad.quadCall parent;

        protected void bindCaller(RTS_DrawQuad.quadCall call) {
            this.parent = call;
        }

        public RTS_DrawQuad.quadCall set () {
            return (this.parent);
        }

        public abstract shaderPlugin_quad cloneShader ();

        public abstract void run (RTS_DrawQuad.quadCall quadCall);

        public abstract void reset ();
    }

    public static int loadProgram (String vertString, String fragString) {
        String vertexText;
        String fragmentText;
        try {
            vertexText = Global.getSettings().loadText(vertString);
            fragmentText = Global.getSettings().loadText(fragString);
        } catch (IOException e) {
            throw new RuntimeException("RTSAssist: Failed to load shaders from disk.");
        }
        int program = GL20.glCreateProgram();
        if (program == 0) {
            if (RTS_ShaderManager.hardfail)
                throw new RuntimeException("RTSAssist: Failed to build shader program.");
            else
                return (0);
        }
        int fragShader;
        int vertShader;
        fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        vertShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        if (vertShader == 0 || fragShader == 0) {
            if (RTS_ShaderManager.hardfail)
                throw new RuntimeException("RTSAssist: Failed to create shader.");
            else
                return (0);
        }
        GL20.glShaderSource(fragShader, fragmentText);
        GL20.glCompileShader(fragShader);
        GL20.glShaderSource(vertShader, vertexText);
        GL20.glCompileShader(vertShader);
        if (GL20.glGetShaderi(vertShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE
                || GL20.glGetShaderi(fragShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            if (RTS_ShaderManager.hardfail)
                throw new RuntimeException("RTSAssist: Shaders failed to compile.");
            else
                return (0);
        }
        GL20.glAttachShader(program, vertShader);
        GL20.glAttachShader(program, fragShader);
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            if (RTS_ShaderManager.hardfail)
                throw new RuntimeException("RTSAssist: Failed to link program");
            else
                return (0);
        }
        return (program);
    }

    public static void loadProgram_LEGACY (String identifier, shaderInfo info) {
        String vertexText;
        String fragmentText;
        try {
            vertexText = Global.getSettings().loadText(info.vertexDir());
            fragmentText = Global.getSettings().loadText(info.fragDir());
        } catch (IOException e) {
            throw new RuntimeException("RTSAssist: Failed to load shaders from disk.");
        }
        RTS_ShaderManager.loadedShaders.put(identifier, 0);
        int program = GL20.glCreateProgram();
        if (program == 0) {
            if (RTS_ShaderManager.hardfail)
                throw new RuntimeException("RTSAssist: Failed to build shader program.");
            else
                return;
        }
        int fragShader;
        int vertShader;
        fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        vertShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        if (vertShader == 0 || fragShader == 0) {
            if (RTS_ShaderManager.hardfail)
                throw new RuntimeException("RTSAssist: Failed to create shader.");
            else
                return;
        }
        GL20.glShaderSource(fragShader, fragmentText);
        GL20.glCompileShader(fragShader);
        GL20.glShaderSource(vertShader, vertexText);
        GL20.glCompileShader(vertShader);
        if (GL20.glGetShaderi(vertShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE
                || GL20.glGetShaderi(fragShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            if (RTS_ShaderManager.hardfail)
                throw new RuntimeException("RTSAssist: Shaders failed to compile.");
            else
                return;
        }
        GL20.glAttachShader(program, vertShader);
        GL20.glAttachShader(program, fragShader);
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            if (RTS_ShaderManager.hardfail)
                throw new RuntimeException("RTSAssist: Failed to link program");
            else
                return;
        }
        RTS_ShaderManager.loadedShaders.put(identifier, program);
    }

    public static int getProgram (String shaderID) {
        try {
            return (RTS_ShaderManager.loadedShaders.get(shaderID));
        } catch (RuntimeException e) {
            throw(new RuntimeException("RTSAssist: getShader param refers to a shader that either does not exist or is not loaded"));
        }
    }

    public static void useProgram(String shaderID) {
        if (RTS_ShaderManager.activeProgram != getProgram(shaderID)) {
            RTS_ShaderManager.activeProgram = getProgram(shaderID);
            GL20.glUseProgram(getProgram(shaderID));
        }
    }
    public static void useProgram(int program) {
        if (RTS_ShaderManager.activeProgram != program) {
            RTS_ShaderManager.activeProgram = program;
            GL20.glUseProgram(program);
        }
    }

    public static void clearProgram() {
        if (RTS_ShaderManager.activeProgram != 0) {
            RTS_ShaderManager.activeProgram = 0;
            GL20.glUseProgram(0);
        }
    }
}

