#version 130

uniform vec2 circleCenter;
uniform float radius;
uniform vec4 color;

void main() {
    float distance = distance(circleCenter, gl_FragCoord.xy);
    gl_FragColor = vec4(vec3(color.rgb), clamp(radius - distance, 0.0, 1.0));
}
