#version 330

uniform sampler2D DiffuseSampler;
layout(std140) uniform OreData {
    mat4 InverseTransformMatrix;
    mat4 ModelViewMat;
    vec4 CameraData;
    vec4 TargetData;
    vec4 TimeData;
};
#define CameraPosition CameraData.xyz
#define BlockPosition TargetData.xyz
#define IsBlockHit TargetData.w
#define iTime TimeData.x
#define viewWidth TimeData.y
#define viewHeight TimeData.z

in vec2 texCoord;



out vec4 fragColor;

vec2 rotate(vec2 p, float r) {
    return mat2(cos(r), -sin(r), sin(r), cos(r)) * p;
}

void main() {
    float frameTimeCounter = max(iTime - 37., 0.);

    vec3 original = texture(DiffuseSampler, texCoord).rgb;
    vec2 one_pixel = vec2(1. / viewWidth, 1. / viewHeight);
    vec2 rotated_pixel = rotate(one_pixel, -frameTimeCounter);

    float scale = max((-pow((frameTimeCounter - 0.84) * 8., 2.) + 50.) * 25. / (distance(CameraPosition, BlockPosition) - 24. + 25.), 0.);
    float ca_red = texture(DiffuseSampler, texCoord + (rotated_pixel) * scale).r;
    rotated_pixel = rotate(rotated_pixel, 2.09439510239);
    float ca_green = texture(DiffuseSampler, texCoord + (rotated_pixel - one_pixel) * scale).g;
    rotated_pixel = rotate(rotated_pixel, 2.09439510239);
    float ca_blue = texture(DiffuseSampler, texCoord + (rotated_pixel - one_pixel) * scale).b;

    fragColor = vec4(mix(original, vec3(ca_red, ca_green, ca_blue), clamp(2. * length(texCoord - vec2(0.5)), 0., 1.) * TimeData.w), 1.);
}
