#version 130

varying vec2 vTexCoOrd;

void main() {
    vec2 uv = vTexCoOrd;
    vec2 d = uv - 0.5;
    float dist = (0.5 - sqrt(dot(d, d))) * 2.0;
    gl_FragColor = vec4(vec3(1.0), dist);
}
