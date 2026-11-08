#version 130

varying vec2 vTexCoOrd;

uniform vec2 uCircleCenter;
uniform float uRadius;
uniform vec2 uResolution;
uniform vec2 uPosition;
uniform vec4 uColor;
uniform vec4 uColorFade;
uniform float uThickness;
uniform float uFadeThickness;
uniform float uFadeTransition;
uniform bool uFitVsEncircle;
uniform float uAliasing;


void fitRadius (vec2 texCoOrd) {
    float fadeTransition = max(uFadeThickness + uFadeTransition, uAliasing);
    float distance = distance(texCoOrd, uCircleCenter);

    float alphaA = clamp(uRadius + uFadeThickness - distance , 0.0, fadeTransition) / fadeTransition;
    float alphaB = clamp(distance - (uRadius - uThickness) , 0.0, uAliasing) / uAliasing;
    vec4 blend = mix(uColor, uColorFade, clamp(distance - uRadius, 0.0, uAliasing) / uAliasing);

    gl_FragColor = vec4(blend.rgb, clamp(alphaA + alphaB - 1.0, 0.0, 1.0) * blend.a);
}

void encircleRadius (vec2 texCoOrd) {
    float fadeTransition = max(uFadeThickness + uFadeTransition, uAliasing);
    float distance = distance(texCoOrd, uCircleCenter);

    float alphaA = clamp(uRadius + uThickness + uFadeThickness - distance, 0.0, fadeTransition) / fadeTransition;
    float alphaB = clamp(distance - uRadius, 0.0, uAliasing) / uAliasing;
    vec4 blend = mix(uColor, uColorFade, clamp(distance - (uRadius + uThickness), 0.0, uAliasing) / uAliasing);

    gl_FragColor = vec4(blend.rgb, clamp(alphaA + alphaB - 1.0, 0.0, 1.0) * blend.a);
}

void main() {
    vec2 rTexCoOrd = vec2(vTexCoOrd * uResolution) + uPosition;
    if (uFitVsEncircle)
        fitRadius(rTexCoOrd);
    else
        encircleRadius(rTexCoOrd);
}
