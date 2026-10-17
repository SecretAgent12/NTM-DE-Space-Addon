#version 150

// backport: 1.21.1 program from hbmspace core/swarm.fsh

in vec4 vColor;

out vec4 fragColor;

void main() {
	fragColor = vColor;
}
