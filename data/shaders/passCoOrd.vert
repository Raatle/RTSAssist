#version 130

varying vec2 vTexCoOrd;

void main() {
    gl_Position = ftransform();
    gl_TexCoord[0] = gl_MultiTexCoord0;
    vTexCoOrd = gl_MultiTexCoord0.xy;
    gl_FrontColor = gl_Color;
}
