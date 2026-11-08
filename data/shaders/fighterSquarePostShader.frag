#version 130

uniform sampler2D tex;

uniform float lineThickness;
uniform vec4 outlineColor;
uniform float opacity;

varying vec2 vTexCoOrd;

void main() {
    vec2 uv = vTexCoOrd;
    vec4 color = texture2D(tex, uv);
    vec2 texelSize = vec2(1.5) * lineThickness / 1000.0;

    float maxAlpha = 0.0;
    maxAlpha = max(texture2D(tex, uv + vec2(0.0, texelSize.y)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(0.0, -texelSize.y)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(texelSize.x, 0.0)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(-texelSize.x, 0.0)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(-texelSize.x, -texelSize.y)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(texelSize.x, texelSize.y)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(texelSize.x, -texelSize.y)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(-texelSize.x, texelSize.y)).a, maxAlpha);

    if (color.a < 0.8 && maxAlpha > 0.0) {
        gl_FragColor = vec4(outlineColor.rgb, maxAlpha * outlineColor.a * opacity);
    } else if (color == vec4(0.0)) {
        gl_FragColor = color;
    } else {
        gl_FragColor = vec4(color.rgb, opacity);
    }
}
