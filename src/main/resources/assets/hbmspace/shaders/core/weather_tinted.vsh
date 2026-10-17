#version 150

// backport: 1.21.1 program (hbmspace core/weather_tinted): 1.21.1 draws weather with the particle
// vertex format, so the vertex colour is lit from the light map here and the fog distance is 1.21.1's.
in vec3 Position;
in vec2 UV0;
in vec4 Color;
in ivec2 UV2;

uniform sampler2D Sampler2;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out float vertexDistance;
out vec2 texCoord0;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexDistance = FogShape == 0 ? length(Position) : max(length(Position.xz), abs(Position.y));
    texCoord0 = UV0;
    vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0);
}
