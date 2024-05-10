#version 120

uniform sampler2D texture;
uniform float time;
uniform vec2 resolution;

uniform vec4 col;

// I didn't write these noise calcs
// PASTED?!?!??!/1/?!?!?!?!?1/?!1/ :O
// Yes, deal with it
mat3 rotX(float a) {
    return mat3(1, 0, 0, 0, cos(a), -sin(a), 0, sin(a), cos(a));
}

mat3 rotY(float a) {
    return mat3(cos(a), 0, -sin(a), 0, 1, 0, sin(a), 0, cos(a));
}

float random(vec2 pos) {
    return fract(1.0 * sin(pos.y + fract(100.0 * sin(pos.x))));// http://www.matteo-basei.it/noise
}

float noise(vec2 pos) {
    vec2 i = floor(pos);
    vec2 f = fract(pos);
    float a = random(i + vec2(0.0, 0.0));
    float b = random(i + vec2(1.0, 0.0));
    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(a, b, u.x) + (random(i + vec2(0.0, 1.0)) - a) * u.y * (1.0 - u.x) + (random(i + vec2(1.0, 1.0)) - b) * u.x * u.y;
}

float fbm(vec2 pos) {
    float a = 0.5;
    mat2 rot = mat2(cos(0.15), sin(0.15), -sin(0.25), cos(0.5));

    float v = 0.0;
    for (int i = 0; i < 12; i++) {
        v += a * noise(pos);
        pos = rot * pos * 2.0 + vec2(100.0);
        a *= 0.55;
    }

    return v;
}

// This is where i started writing shit
void main() {
    vec4 center = texture2D(texture, gl_TexCoord[0].xy);

    if (center.a > 0.0) {
        vec2 pos = gl_TexCoord[0].xy * 2.2 - resolution / min(resolution.x, resolution.y) * 1.25;

        float f = fbm(pos * 2.0 * vec2(fbm(pos - (time / 8.0)), fbm(pos / 2.0 - (time / 8.0))));

        vec3 colour = mix(
            vec3(col.r, col.g, col.b),
            vec3(col.r, col.g, col.b),
            vec3(col.r, col.g, col.b)
        );

        colour = mix(
            colour,
            vec3(col.r, col.g, col.b),
            vec3(col.r, col.g, col.b)
        );

        colour = mix(
            colour,
            vec3(col.r, col.g, col.b),
            vec3(col.r, col.g, col.b)
        );

        colour = (f * 1.5) * colour;

        gl_FragColor = vec4(colour, col.a);
    }
}