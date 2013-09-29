precision mediump float;					// Set default precision to medium.

uniform sampler2D u_Texture;				// The input texture.

varying vec4 v_Color;						// Color from vertex shader interpolated across triangle per fragment.
varying vec2 v_TexCoordinate;				// Interpolated texture coordinate per fragment.

// The entry point for the fragment shader
void main() {
    // Multiply the color by the texture value to get final output color.
    vec4 tex = texture2D(u_Texture, v_TexCoordinate);
    gl_FragColor = v_Color * tex;
}