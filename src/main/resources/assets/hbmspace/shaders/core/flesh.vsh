#version 150

// backport: 1.21.1 program from hbmspace core/flesh.vsh

uniform mat4 ModelViewMat;
uniform vec4 ColorModulator;
uniform mat4 ProjMat;

in vec3 Position;
in vec2 UV0;

out vec3 vPosition;

void main() {
    vPosition = Position;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
