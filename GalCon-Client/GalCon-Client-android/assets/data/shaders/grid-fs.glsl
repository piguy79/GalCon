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

uniform LOWP float uTilesWide;
uniform LOWP float uTilesTall;

varying MEDP vec2 vTexCoords;

void main() {
	vec4 color = vec4(0, 0, 0, 0);
	
	float gridY = 1.0 / uTilesTall;
	float coordY = mod(vTexCoords.t, gridY);
	if(coordY >= gridY-0.002 || coordY <= 0.002) {
		color = vec4(.2, .2, 0, .2);
	}
	
	float gridX = 1.0 / uTilesWide;
	float coordX = mod(vTexCoords.s, gridX);
	if(coordX >= gridX-0.002 || coordX <= 0.002) {
		color = vec4(.2, .2, 0, .2);
	}
		
	gl_FragColor = color;
}