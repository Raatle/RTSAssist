#version 130

varying vec2 vTexCoOrd;

uniform sampler2D tex;

uniform vec3 uCircles[100];
uniform int uSize;
uniform vec2 uResolution;
uniform vec2 uPosition;
uniform float uBlend;
uniform float uAliasing;
uniform vec4 uColor;

float sdDiskSquare (vec2 pA, vec2 pB, float r) {
    return (dot(pA - pB, pA - pB) - (r*r));
}
float sdDisk (vec2 pA, vec2 pB, float r) {
    return (sqrt(dot(pA - pB, pA - pB)) - r);
}

// Google AI: sdf smooth minimum multiple shapes
// AKA Dreams optimised quadratic polynomial smooth minimum
float quadPoly(float a, float b, float k) {
    float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
    return (mix(b, a, h) - k * h * (1.0 - h));
}

// exponential
float smoothMinDistanceExp (vec3 circles[100], int arrSize, vec2 point, float blend) {
    float r = 0;
    float d;
    for (int i = 0; i < arrSize; i++) {
        d = sdDiskSquare(circles[i].xy, point, circles[i].z);
        r += exp2(-d/blend);
    }
    return (-blend * log2(r));
}

// quadratic polynomial
float smoothMinDistanceQuadPoly (vec3 circles[100], int arrSize, vec2 point, float blend) {
    float d;
    float min;
    min = sdDiskSquare(circles[0].xy, point, circles[0].z);
    for (int i = 1; i < arrSize; i++)
        min = quadPoly(min, sdDiskSquare(circles[i].xy, point, circles[i].z), blend);
    return (min);
}

void main() {
    vec2 coOrd = vec2(vTexCoOrd * uResolution) + uPosition;
    float alpha = smoothMinDistanceQuadPoly(uCircles, uSize, coOrd, uBlend);
    gl_FragColor = vec4(uColor.xyz, clamp(alpha * uAliasing, 0.0, 1.0));
//    gl_FragColor = vec4(vec3(0.5, 0.5, 0.5).xyz, clamp(alpha * uAliasing, 0.5, 1.0)); //debug culling method
}
