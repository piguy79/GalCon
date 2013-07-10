#ifdef GL_ES
	#ifdef GL_FRAGMENT_PRECISION_HIGH 
		precision highp float;
	#else
		precision mediump float;
	#endif
#endif

uniform float uTilesWide;
uniform float uTilesTall;
uniform float uDimmer;
uniform float uSelectedPlanetsCoords[4];

uniform sampler2D bgTex; 

varying vec2 vTexCoords;


void main() {
	vec4 color = vec4(0, 0, 0, 0);
	
	float tileX = vTexCoords.s * uTilesWide;
	float tileY = vTexCoords.t * uTilesTall;
	bool found = false;
	
	
	if(uDimmer == 1.0){
		if(!(tileX < (uSelectedPlanetsCoords[0] + 1.0) && tileX > uSelectedPlanetsCoords[0]
		&& tileY < (uSelectedPlanetsCoords[1] + 1.0) && tileY > uSelectedPlanetsCoords[1]) && !(tileX < (uSelectedPlanetsCoords[2] + 1.0) && tileX > uSelectedPlanetsCoords[2]
		&& tileY < (uSelectedPlanetsCoords[3] + 1.0) && tileY > uSelectedPlanetsCoords[3])) {			
			color = vec4(-0.4, -0.4, -0.4, .6); 
		} 
		
		
	}
	
		
	gl_FragColor = color;
}