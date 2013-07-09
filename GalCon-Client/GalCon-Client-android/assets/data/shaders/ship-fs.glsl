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

void main() {
	if(uDimmer == 1.0 && uShowThisShip != 1.0){
		gl_FragColor = uColor + vec4(-0.6, -0.4, -0.4, 0);
	} else {
		gl_FragColor = uColor;
	}

	
}