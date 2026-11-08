#version 130

varying vec2 vTexCoOrd;

uniform vec2 uCircleCenter;
uniform float uRadius;
uniform float uInnerDominence;
uniform vec4 uColor;
uniform vec4 uColor2;
uniform float uFadeSpeed;
uniform vec2 uPosition;
uniform vec2 uResolution;
uniform float uAliasing;


void main() {
    vec2 rTexCoOrd = vec2(vTexCoOrd * uResolution) + uPosition;

    float distance = distance(uCircleCenter, rTexCoOrd);
    float alpha = clamp(uRadius - distance, 0.0, uAliasing) / uAliasing;
    float ratio = (uRadius - distance) / (distance * (1.0 / uInnerDominence));
    vec4 blend = mix(uColor, uColor2, pow(clamp(ratio, 0.0, 1.0), uFadeSpeed));
    gl_FragColor = vec4(blend.rgb, alpha * blend.a);
}
