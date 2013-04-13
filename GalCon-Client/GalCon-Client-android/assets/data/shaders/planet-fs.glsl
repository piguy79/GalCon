#ifdef GL_ES
#define LOWP lowp
#define MEDP mediump
#define HIGP highp
precision lowp float;
#else
#define LOWP
#define MEDP
#define HIGP
#endif

uniform int shipCount;
uniform vec4 uColor;
uniform float uRadius;
uniform sampler2D numbersTex;

varying vec2 vTexCoords;

const float imagehalfWidth = 512.0; 

float numberOffset(int number);

void main() {
	float xDistToNumberCenter = 0.5 - vTexCoords.x;
	float yDistToNumberCenter = 0.5 - vTexCoords.y;
	float halfWidth = 0.08;
	float halfHeight = 0.1;
	vec4 numberTexColor = vec4(0,0,0,0);
	if(xDistToNumberCenter < halfWidth && xDistToNumberCenter > -halfWidth 
		&& yDistToNumberCenter < halfHeight && yDistToNumberCenter > -halfHeight) {
		xDistToNumberCenter = (xDistToNumberCenter + halfWidth) * (1.0 / (1.0-(halfWidth + halfWidth)));
		yDistToNumberCenter = (yDistToNumberCenter + halfHeight) * (1.0 / (1.0-(halfHeight + halfHeight)));
				
		numberTexColor = texture2D(numbersTex, 
									vec2(numberOffset(shipCount) + xDistToNumberCenter * 0.18, yDistToNumberCenter * 0.2));
	}
	
	if(numberTexColor.a == 0.0) {
		numberTexColor = vec4(0,0,0,0);
	}
	
	float dist = distance(vec2(0.5, 0.5), vTexCoords);
	gl_FragColor = mix(uColor, vec4(0, 0, 0, 0), smoothstep(uRadius, uRadius + 0.05, dist)) + numberTexColor;
}

float numberOffset(int number) {
	if(number == 0) {
		return 173.0/imagehalfWidth;
	}
	if(number == 1) {
		return 160.0/imagehalfWidth;
	}
	if(number == 2) {
		return 142.0/imagehalfWidth;
	}
	if(number == 3) {
		return 122.0/imagehalfWidth;
	}
	if(number == 4) {
		return 100.0/imagehalfWidth;
	}
	if(number == 5) {
		return 80.0/imagehalfWidth;
	}
	if(number == 6) {
		return 60.0/imagehalfWidth;
	}
	if(number == 7) {
		return 41.0/imagehalfWidth;
	}
	if(number == 8) {
		return 20.0/imagehalfWidth;
	}
	
	return 0.0;
}