#version 130

float sdfCircle(vec2 uv, float radius) {
    return length(uv - 0.5) - radius;
}

uniform vec2 u_resolution;

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution.xy;
    float dist = sdfCircle(uv, 0.2);
    float edge = fwidth(dist);
    float alpha = smoothstep(edge, 0.0, dist);
    gl_FragColor = vec4(vec3(1.0), alpha); // White circle, soft edge
//    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}