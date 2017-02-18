
package it.uniroma3.dia.gc.code;

import it.uniroma3.dia.gc.io.Writer;

/**
 * This class implements an encoder for &pi;-Code.
 *
 * @author Guido Drovandi
 * @version 0.2
 */
public final class PiEncoder {

    private static final double LOG2=Math.log(2);

    private final int[] codes;
    private final int indexMult;
    private final Writer out;

    /**
     * Creates an instance.
     *
     * @param out the bit-stream where to write
     */
    public PiEncoder(final Writer out) {
	this(out,0,0);
    }

    /**
     * Creates an instance.
     *
     * A buffer of size <code>codesNum</code>x<code>dim</code> is created to improve
     * the encoding phase performance.
     *
     * @param out the bit-stream where to write
     * @param dim the size of the buffer
     * @param codesNum the number of code-buffers
     */
    public PiEncoder(final Writer out, final int dim, final int codesNum) {
	this.out=out;
	this.indexMult=codesNum*2;
	this.codes=new int[dim*this.indexMult];
    }

    /**
     * Write the encode an integer in the interval [1, &#8734;) into the stream.
     *
     * @param n the integer to be encoded
     * @param k the base of the &pi;-Code
     * @return 
     * @throws java.lang.Exception
     */
    public final int encode(final int n, final int k) throws Exception {
	final int length;
	int i,s,r,nn,l;
	if (n<=0) throw new Exception("Encoder value less than or equal to 0!");
	if (k<0) throw new Exception("Encoder parameter less than 0!");
	s=(int)Math.floor(Math.log(n)/LOG2)+1;
	length=s-1;
	nn=n-(1<<(s-1));
	l=k;
	r=1<<k;
	for (i=0;i<k;i++) {
	    if (s%2==1)
		r|=1<<i;
	    s=(int)Math.ceil(s/2.);
	}
	l+=s;
	out.writeBits(r,l);
	out.writeBits(nn,length);
	return l+length;
    }

    /**
     * Write an integer in the interval [0, &#8734;) into the stream.
     *
     * @param n the integer to be encoded
     * @param k the base of the &pi;-Code
     * @return 
     * @throws java.lang.Exception
     */
    public final int encode0(final int n, final int k) throws Exception {
	if (n<0) throw new Exception("Encoder value less than 0!");
	if (k<0) throw new Exception("Encoder parameter less than 0!");
	if (n==0) {
	    out.writeBit(true);
	    return 1;
	}
	out.writeBit(false);
	return encode(n,k)+1;
    }

    /**
     * Encode an integer in the interval [1, &#8734;).
     *
     * @param n the integer to be encoded
     * @param k the base of the &pi;-Code
     * @return 
     * @throws java.lang.Exception
     */
    public final int[] code(final int n, final int k) throws Exception {
	if (n<=0) throw new Exception("Encoder value less than or equal to 0!");
	if (k<0) throw new Exception("Encoder parameter less than 0!");
	int i,s,r,nn,l;
	final int length;
	final int[] output=new int[2];
	final int index=indexMult*(n-1)+k*2;
	if (index+1<codes.length && codes[index+1]!=0) {
	    output[0]=codes[index];
	    output[1]=codes[index+1];
	} else {
	    s=(int)Math.floor(Math.log(n)/LOG2)+1;
	    length=s-1;
	    nn=n-(1<<(s-1));
	    l=k;
	    r=1<<k;
	    for (i=0;i<k;i++) {
		if (s%2==1)
		    r|=1<<i;
		s=(int)Math.ceil(s/2.);
	    }
	    l+=s;
	    output[0]=(r<<length)|nn;
	    output[1]=l+length;
	    if (index+1<codes.length) {
		codes[index]=output[0];
		codes[index+1]=output[1];
	    }
	}
	return output;
    }

    /**
     * Encode an integer in the interval [0, &#8734;).
     *
     * @param n the integer to be encoded
     * @param k the base of the &pi;-Code
     * @return 
     * @throws java.lang.Exception
     */
    public final int[] code0(final int n, final int k) throws Exception {
	if (n<0) throw new Exception("Encoder value less than 0!");
	if (k<0) throw new Exception("Encoder parameter less than 0!");
	if (n==0) return new int[] {1,1};
	final int[] output=code(n,k);
	output[1]++;
	return output;
    }

}

