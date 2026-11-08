#version 130

uniform vec2 u_resolution;
uniform float lineThickness;
uniform float opacity;
uniform float softness;
varying vec2 vTexCoOrd;

void main() {
    vec4 color = gl_Color;
    float xoRes = min(
        clamp(vTexCoOrd.x * u_resolution.x, 0.0, softness),
        clamp(u_resolution.x - (vTexCoOrd.x * u_resolution.x), 0.0, softness)
    );
    float yoRes = min(
        clamp(vTexCoOrd.y * u_resolution.y, 0.0, softness),
        clamp(u_resolution.y - (vTexCoOrd.y * u_resolution.y), 0.0, softness)
    );
    float alphaOut = min(xoRes, yoRes) / softness;

    float xiRes = max(
        softness - clamp((vTexCoOrd.x * u_resolution.x) - lineThickness, 0.0, softness),
        softness - clamp((u_resolution.x - (vTexCoOrd.x * u_resolution.x)) - lineThickness, 0.0, softness)
    );
    float yiRes = max(
        softness - clamp((vTexCoOrd.y * u_resolution.y) - lineThickness, 0.0, softness),
        softness - clamp((u_resolution.y - (vTexCoOrd.y * u_resolution.y)) - lineThickness, 0.0, softness)
    );
    float alphaIn = max(xiRes, yiRes) * (1.0 / softness);

    float alphaFinal = min(alphaOut, alphaIn);
    gl_FragColor = vec4(color.rgb, min(alphaFinal, opacity));
}
