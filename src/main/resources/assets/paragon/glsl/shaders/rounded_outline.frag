#version 120

uniform vec2 size;
uniform vec4 colour;
uniform float radius;
uniform float thickness;

float round(vec2 center, vec2 size, float radius) {
    return length(max(abs(center) - size + radius, 0.0)) - radius;
}

void main() {
    float roundedRect = round((gl_TexCoord[0].xy * size) - (size / 2.0), size / 2.0, radius);
    float smoothedRect = (1.0 - smoothstep(-1.0, 0.0, roundedRect)) * colour.a;

    float rounded = round((gl_TexCoord[0].xy * size) - (size / 2.0), size / 2.0, radius);
    float smoothed = ((smoothstep(-1.0, 0.0, rounded + thickness)) * colour.a) * smoothedRect;

    gl_FragColor = vec4(colour.rgb, smoothed);
}