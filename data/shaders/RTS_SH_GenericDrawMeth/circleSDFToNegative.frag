#version 130

varying vec2 vTexCoOrd;
uniform sampler2D tex;

void main() {
    vec2 uv = vTexCoOrd;
    vec4 color = texture2D(tex, uv);
    float alpha = clamp((color.a * 20.0) - 1.0, 0.0, 1.0);
    gl_FragColor = vec4(color.rgb, 1 - alpha);
}
