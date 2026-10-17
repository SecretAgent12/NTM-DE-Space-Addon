#version 150

// backport: 1.21.1 program from hbmspace core/planet.vsh (BODY_MASK)
#define BODY_MASK

uniform mat4 ModelViewMat;
uniform vec4 ColorModulator;
uniform mat4 ProjMat;

in vec3 Position;
in vec2 UV0;

out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord0 = UV0;
}
