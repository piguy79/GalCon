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

void main() {
	float imagehalfWidth = 512.0;
	float offsets[10];
	offsets[9] = 0.0;
	offsets[8] = 20.0/imagehalfWidth;
	offsets[7] = 41.0/imagehalfWidth;
	offsets[6] = 60.0/imagehalfWidth;
	offsets[5] = 80.0/imagehalfWidth;
	offsets[4] = 100.0/imagehalfWidth;
	offsets[3] = 122.0/imagehalfWidth;
	offsets[2] = 142.0/imagehalfWidth;
	offsets[1] = 160.0/imagehalfWidth;
	offsets[0] = 173.0/imagehalfWidth;
	
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
									vec2(offsets[shipCount] + xDistToNumberCenter * 0.18, yDistToNumberCenter * 0.2));
	}
	
	if(numberTexColor.a == 0.0) {
		numberTexColor = vec4(0,0,0,0);
	}
	
	float dist = distance(vec2(0.5, 0.5), vTexCoords);
	gl_FragColor = mix(uColor, vec4(0, 0, 0, 0), smoothstep(uRadius, uRadius + 0.05, dist)) + numberTexColor;
}