uniform mat4 u_MVPMatrix;						// A constant representing the combined model/view/projection matrix

attribute vec4 a_Position;						// Per-vertex Position information we will pass in.
attribute vec4 a_Color;							// Per-vertex Color information we will pass in.
attribute vec2 a_TexCoordinate;					// Per-vertex Texture Coordinate information we will pass in.

varying vec4 v_Color;							// This will be passed into the fragment shader.
varying vec2 v_TexCoordinate;					// This will be passed into the fragment shader.

// The entry point for the vertex shader
void main() {

    // Pass the color through to fragment shader
    v_Color = a_Color;
    
    // Pass the texture coordinate through to the fragment shader
    v_TexCoordinate = a_TexCoordinate;
    
    // gl_Position is a special variable that stores the final position.
    // Multiply the vertex by the MVPMatrix to get the final point in normalized screen coordinates
    gl_Position = u_MVPMatrix * a_Position;
}