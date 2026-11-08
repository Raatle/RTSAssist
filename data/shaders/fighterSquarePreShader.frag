#version 130

varying vec2 vTexCoOrd;

void main() {
    vec2 uv = vTexCoOrd;
    float xMin = min(uv.x, abs(1.0 - uv.x));
    float yMin = min(uv.y, abs(1.0 - uv.y));
    float alpha = clamp(min(xMin, yMin), 0.0, 1.0);
    gl_FragColor = vec4(gl_Color.rgb, pow(alpha * 10.0, 2));
}
