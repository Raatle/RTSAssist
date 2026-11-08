#version 130

varying vec2 vTexCoOrd;

uniform sampler2D tex;
uniform vec2 uTexelSize;
uniform vec4 uOutlineColor;

void main() {
    float margin = 1.0;
    vec2 uv = vTexCoOrd;
    vec4 color = texture2D(tex, uv);

    float maxAlpha = 0.0;
    maxAlpha = max(texture2D(tex, uv + vec2(0.0, uTexelSize.y)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(0.0, -uTexelSize.y)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(uTexelSize.x, 0.0)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(-uTexelSize.x, 0.0)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(-uTexelSize.x, -uTexelSize.y)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(uTexelSize.x, uTexelSize.y)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(uTexelSize.x, -uTexelSize.y)).a, maxAlpha);
    maxAlpha = max(texture2D(tex, uv + vec2(-uTexelSize.x, uTexelSize.y)).a, maxAlpha);

    float alphaTotal = 0.0;
    alphaTotal += texture2D(tex, uv + vec2(0.0, uTexelSize.y)).a;
    alphaTotal += texture2D(tex, uv + vec2(0.0, -uTexelSize.y)).a;
    alphaTotal += texture2D(tex, uv + vec2(uTexelSize.x, 0.0)).a;
    alphaTotal += texture2D(tex, uv + vec2(-uTexelSize.x, 0.0)).a;
    alphaTotal += texture2D(tex, uv + vec2(-uTexelSize.x, -uTexelSize.y)).a;
    alphaTotal += texture2D(tex, uv + vec2(uTexelSize.x, uTexelSize.y)).a;
    alphaTotal += texture2D(tex, uv + vec2(uTexelSize.x, -uTexelSize.y)).a;
    alphaTotal += texture2D(tex, uv + vec2(-uTexelSize.x, uTexelSize.y)).a;
    float alphaAvg = alphaTotal / 2.0;

//    float combAlpha = (maxAlpha + alphaAvg) / 2.0;
    float combAlpha = (maxAlpha + alphaTotal) / 2.0;

    if (color.a < 0.9 && combAlpha > 0.0) {
        gl_FragColor = vec4(uOutlineColor.rgb, (combAlpha * uOutlineColor.a));
    } else {
        gl_FragColor = color;
    }
//    gl_FragColor = vec4(1.0);
}