
package it.uniroma3.dia.gc.code;

import it.uniroma3.dia.gc.io.Reader;

/**
 * This class implements a decoder for &pi;-Code from a bit-stream.
 *
 * @author Guido Drovandi
 * @version 0.2
 */
public final class PiDecoder {

    private final Reader in;

    /**
     * Creates a new instance.
     *
     * @param in the bit-stream where to read
     */
    public PiDecoder(final Reader in) {
	this.in=in;
    }

    /**
     * Decode a number in the interval [0; &#8734;).
     *
     * @param k the base of the &pi;-Code
     * @return 
     */
    public int decode0(final int k) {
	int l;
	if (in.readBit()) return 0;
	for (l=1;!in.readBit();l++);
	return ((l=(l<<k)-in.readBits(k)-1)==0)?1:(in.readBits(l)|(1<<l));
    }

    /**
     * Decode a number in the interval [0; &#8734;) using &pi;<small><sub>0</sub></small>-Code.
     * @return 
     */
    public int decode0K0() {
	int l;
	if (in.readBit()) return 0;
	for (l=0;!in.readBit();l++);
	return (l==0)?1:(in.readBits(l)|(1<<l));
    }

    /**
     * Decode a number in the interval [1; &#8734;) using &pi;<small><sub>0</sub></small>-Code.
     * @return 
     */
    public int decodeK0() {
	int l;
	for (l=0;!in.readBit();l++);
	return (l==0)?1:(in.readBits(l)|(1<<l));
    }

    /**
     * Decode a number in the interval [1; &#8734;).
     *
     * @param k the base of the &pi;-Code
     * @return 
     */
    public int decode(final int k) {
	int l;
	for (l=1;!in.readBit();l++);
	return ((l=(l<<k)-in.readBits(k)-1)==0)?1:(in.readBits(l)|(1<<l));
    }

}

