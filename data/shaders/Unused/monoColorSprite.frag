#version 130

float sdfCircle(vec2 uv, float radius) {
    return length(uv - 0.5) - radius;
}

uniform sampler2D tex;
uniform vec4 monoColor;
uniform vec2 u_resolution;

void main() {
//    vec2 uvRef = gl_FragCoord.xy / u_resolution.xy;
//    float dist = sdfCircle(uvRef, 1.0);
//    float edge = fwidth(dist);
//    float alphaNew = smoothstep(edge, 0.0, dist);

    vec2 uv = gl_TexCoord[0].xy;
    vec4 color = texture2D(tex, uv);
    float alpha = color.a;
    if (color.a > 0.0) {
        gl_FragColor = vec4(monoColor.r, monoColor.g, monoColor.b, monoColor.a);
    }
//    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}


