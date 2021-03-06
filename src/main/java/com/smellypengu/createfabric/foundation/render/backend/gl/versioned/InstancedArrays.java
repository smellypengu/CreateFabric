package com.smellypengu.createfabric.foundation.render.backend.gl.versioned;

import org.lwjgl.opengl.*;

public enum InstancedArrays implements GlVersioned {
    GL33_INSTANCED_ARRAYS {
        @Override
        public boolean supported(GLCapabilities caps) {
            return caps.OpenGL33;
        }

        @Override
        public void vertexAttribDivisor(int index, int divisor) {
            GL33.glVertexAttribDivisor(index, divisor);
        }
    },
    ARB_INSTANCED_ARRAYS {
        @Override
        public boolean supported(GLCapabilities caps) {
            return caps.GL_ARB_instanced_arrays;
        }

        @Override
        public void vertexAttribDivisor(int index, int divisor) {
            ARBInstancedArrays.glVertexAttribDivisorARB(index, divisor);
        }
    },
    UNSUPPORTED {
        @Override
        public boolean supported(GLCapabilities caps) {
            return true;
        }

        @Override
        public void vertexAttribDivisor(int index, int divisor) {
            throw new UnsupportedOperationException();
        }
    }

    ;

    public abstract void vertexAttribDivisor(int index, int divisor);
}
