#version 130

varying vec2 vTexCoOrd;

uniform vec2 uCircleCenter;
uniform float uRadius;
uniform float uInnerDominence;
uniform vec4 uColor;
uniform float uFadeSpeed;
uniform vec2 uPosition;
uniform vec2 uResolution;
uniform float uAliasing;


void main() {
    vec2 rTexCoOrd = vec2(vTexCoOrd * uResolution) + uPosition;

    float distance = distance(uCircleCenter, rTexCoOrd);
    float ratio = (uRadius - distance) / (distance * (1.0 / uInnerDominence));
    gl_FragColor = vec4(uColor.rgb, pow(clamp(ratio, 0.0, 1.0), uFadeSpeed));
}
