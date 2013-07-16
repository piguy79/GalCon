#ifdef GL_ES
	#ifdef GL_FRAGMENT_PRECISION_HIGH 
		precision highp float;
	#else
		precision mediump float;
	#endif
#endif

uniform float uTilesWide;
uniform float uTilesTall;

varying vec2 vTexCoords;
uniform sampler2D bgTex; 

void main() {
	vec4 color = vec4(0, 0, 0, 0);
	
	float gridY = 1.0 / uTilesTall;
	float coordY = mod(vTexCoords.t, gridY);
	if(coordY >= gridY-0.002) {
		color = vec4(.2, .2, .2, .2);
	}
	if(coordY <= 0.002) {
		color = vec4(.2, .2, .2, .2);
	}
	
	float gridX = 1.0 / uTilesWide;
	float coordX = mod(vTexCoords.s, gridX);
	if(coordX >= gridX-0.002) {
		color = vec4(.2, .2, .2, .2);
	}
	if(coordX <= 0.002) {
		color = vec4(.2, .2, .2, .2);
	}	
	
	vec4 bgColor = texture2D(bgTex, vTexCoords);
		
	gl_FragColor = color + bgColor;
}