#version 150

// backport: 1.21.1 program (hbmspace core/weather_tinted.fsh): the tint replaces the texture's colour,
// the texture keeps its shape (alpha); 26.x apply_fog is the 1.21.1 linear fog.
uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = vec4(vertexColor.rgb, texture(Sampler0, texCoord0).a * vertexColor.a) * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    float value = vertexDistance <= FogStart ? 0.0 : smoothstep(FogStart, FogEnd, vertexDistance);
    fragColor = vec4(mix(color.rgb, FogColor.rgb, value * FogColor.a), color.a);
}
