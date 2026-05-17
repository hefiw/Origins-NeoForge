#version 150

uniform sampler2D DiffuseSampler;
uniform vec4 ColorModulate;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    // Усиливаем красный канал + лёгкий сепия-эффект
    float gray = dot(color.rgb, vec3(0.3, 0.59, 0.11));
    vec3 redTint = vec3(gray * 1.6, gray * 0.6, gray * 0.5);

    fragColor = vec4(redTint, color.a) * ColorModulate;
}