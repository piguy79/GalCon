#ifdef GL_ES
	#ifdef GL_FRAGMENT_PRECISION_HIGH 
		precision highp float;
	#else
		precision mediump float;
	#endif
#endif

uniform int shipCount;
uniform sampler2D numbersTex;

varying vec2 vTexCoords;

const float IMAGE_WIDTH = 512.0; 

float numberOffset(int number);

void main() {
	float xDistToCenter = 0.5 - vTexCoords.x;
	float yDistToCenter = vTexCoords.y - 0.5;
	float halfWidth = 0.1;
	float halfHeight = 0.12;
	
	float textAreaHalfWidth = halfWidth;
	if(shipCount > 9) {
		textAreaHalfWidth *= 2.0;
	}
	
	vec4 numberTexColor = vec4(0,0,0,0);
	if(xDistToCenter < textAreaHalfWidth && xDistToCenter > -textAreaHalfWidth 
		&& yDistToCenter < halfHeight && yDistToCenter > -halfHeight) {
		
		int numberToShow = shipCount;
		
		float xNumberCenter = 0.5;
		if(shipCount > 9) {
			if(xDistToCenter > 0.0) {
				xNumberCenter = 0.5 - halfWidth;
				numberToShow = numberToShow / 10;
			} else {
				xNumberCenter = 0.5 + halfWidth;
				numberToShow = numberToShow - (numberToShow / 10) * 10;
			}
		}
			
		float xDistToNumberCenter = vTexCoords.x - xNumberCenter;
		float yDistToNumberCenter = yDistToCenter;
		xDistToNumberCenter = (xDistToNumberCenter + halfWidth) * (1.0 / (1.0-(halfWidth + halfWidth)));
		yDistToNumberCenter = (yDistToNumberCenter + halfHeight) * (1.0 / (1.0-(halfHeight + halfHeight)));
				
		float xMult = 0.42;
		if(numberToShow == 1) {
			xMult = 0.36;
		}
		numberTexColor = texture2D(numbersTex, 
									vec2(numberOffset(numberToShow) + xDistToNumberCenter * xMult, yDistToNumberCenter * 0.4));
	}
	
	if(numberTexColor.a == 0.0) {
		numberTexColor = vec4(0,0,0,0);
	}
	
	gl_FragColor = numberTexColor;
}

float numberOffset(int number) {
	if(number == 0) {
		return 444.0/IMAGE_WIDTH;
	}
	if(number == 1) {
		return 402.0/IMAGE_WIDTH;
	}
	if(number == 2) {
		return 351.0/IMAGE_WIDTH;
	}
	if(number == 3) {
		return 302.0/IMAGE_WIDTH;
	}
	if(number == 4) {
		return 248.0/IMAGE_WIDTH;
	}
	if(number == 5) {
		return 200.0/IMAGE_WIDTH;
	}
	if(number == 6) {
		return 150.0/IMAGE_WIDTH;
	}
	if(number == 7) {
		return 100.0/IMAGE_WIDTH;
	}
	if(number == 8) {
		return 50.0/IMAGE_WIDTH;
	}
	
	return 0.0;
}