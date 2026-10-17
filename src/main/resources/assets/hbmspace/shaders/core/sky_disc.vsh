#version 150

// backport: 1.21.1 program (26.x vanilla core/sky.vsh): position and the 1.21.1 fog distance.
in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out float vertexDistance;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vec3 pos = (ModelViewMat * vec4(Position, 1.0)).xyz;
    vertexDistance = FogShape == 0 ? length(pos) : max(length(pos.xz), abs(pos.y));
}
