#ifdef GL_ES
	#ifdef GL_FRAGMENT_PRECISION_HIGH 
		precision highp float;
	#else
		precision mediump float;
	#endif
#endif

uniform vec4 uColor;
uniform float uDimmer;
uniform float uShowThisShip;
uniform float uTimeSinceShipSelected;



void main() {

	if(uDimmer == 1.0 && uShowThisShip != 1.0 && uTimeSinceShipSelected < 1000.0){
		gl_FragColor = uColor;
	} else {
		gl_FragColor = uColor;
	}

	
}