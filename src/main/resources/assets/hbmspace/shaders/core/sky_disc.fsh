#version 150

// backport: 1.21.1 program (hbmspace core/sky_disc.fsh): 26.x apply_fog from 0 to the sky fog end
// (at most 192) is the 1.21.1 linear fog over the same range.
uniform vec4 ColorModulator;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;

out vec4 fragColor;

void main() {
    float fogEnd = min(FogEnd, 192.0);
    float value = fogEnd <= 0.0 ? 1.0 : smoothstep(0.0, fogEnd, vertexDistance);
    fragColor = vec4(mix(ColorModulator.rgb, FogColor.rgb, value * FogColor.a), ColorModulator.a);
}
