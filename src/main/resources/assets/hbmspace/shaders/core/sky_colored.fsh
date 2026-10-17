#version 150

// backport: 1.21.1 program from hbmspace core/sky.fsh (COLORED)
#define COLORED

uniform mat4 ModelViewMat;
uniform vec4 ColorModulator;

#ifdef TEXTURED
uniform sampler2D Sampler0;

in vec2 texCoord0;
#endif
#ifdef COLORED
in vec4 vertexColor;
#endif

out vec4 fragColor;

void main() {
    vec4 color = ColorModulator;
#ifdef TEXTURED
    color *= texture(Sampler0, texCoord0);
#endif
#ifdef COLORED
    color *= vertexColor;
#endif
    fragColor = color;
}
