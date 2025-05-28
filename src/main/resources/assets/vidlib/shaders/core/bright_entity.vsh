#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord0;

void main() {
	vec3 pos = Position;
	gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

	vertexColor = Color;
	texCoord0 = UV0;
}
