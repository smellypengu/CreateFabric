package com.smellypengu.createfabric.foundation.render.backend.gl;

import com.smellypengu.createfabric.foundation.render.backend.Backend;
import com.smellypengu.createfabric.foundation.render.backend.gl.shader.GlProgram;
import com.smellypengu.createfabric.foundation.render.backend.gl.shader.ProgramFogMode;
import com.smellypengu.createfabric.foundation.utility.AnimationTickHolder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL20;

public class BasicProgram extends GlProgram {
    protected final int uTime;
    protected final int uViewProjection;
    protected final int uDebug;
    protected final int uCameraPos;

    protected final ProgramFogMode fogMode;

    protected int uBlockAtlas;
    protected int uLightMap;

    public BasicProgram(Identifier name, int handle, ProgramFogMode.Factory fogFactory) {
        super(name, handle);
        uTime = getUniformLocation("uTime");
        uViewProjection = getUniformLocation("uViewProjection");
        uDebug = getUniformLocation("uDebug");
        uCameraPos = getUniformLocation("uCameraPos");

        fogMode = fogFactory.create(this);

        bind();
        registerSamplers();
        unbind();
    }

    protected void registerSamplers() {
        uBlockAtlas = setSamplerBinding("uBlockAtlas", 0);
        uLightMap = setSamplerBinding("uLightMap", 2);
    }

    public void bind(Matrix4f viewProjection, double camX, double camY, double camZ, int debugMode) {
        super.bind();

        GL20.glUniform1i(uDebug, debugMode);
        GL20.glUniform1f(uTime, AnimationTickHolder.getRenderTick());

        uploadMatrixUniform(uViewProjection, viewProjection);
        GL20.glUniform3f(uCameraPos, (float) camX, (float) camY, (float) camZ);

        fogMode.bind();
    }

    protected static void uploadMatrixUniform(int uniform, Matrix4f mat) {
        Backend.MATRIX_BUFFER.position(0);
        mat.writeToBuffer(Backend.MATRIX_BUFFER);
        Backend.MATRIX_BUFFER.rewind();
        GL20.glUniformMatrix4fv(uniform, false, Backend.MATRIX_BUFFER);
    }
}