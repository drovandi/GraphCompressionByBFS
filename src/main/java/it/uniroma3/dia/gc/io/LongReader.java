
package it.uniroma3.dia.gc.io;

import java.io.PrintStream;
import java.io.FileInputStream;

public final class LongReader implements Reader {

    private static final int WORD_SIZE=64;

    private int word,bit;
    private long[] buffer;
    private final long dimension;

    public final void printWord(final PrintStream out) {
	out.println(buffer[word]+":"+bit);
    }

    public LongReader(final String fileName) throws Exception {
	int i;
	final FileInputStream file=new FileInputStream(fileName);
    	this.word=0;
    	this.bit=WORD_SIZE;
	this.dimension=file.getChannel().size()/8;
	if (dimension<=Integer.MAX_VALUE) {
	    this.buffer=new long[(int)dimension];
	    final byte[] tmp=new byte[100000000];
	    int size,pos=0;
	    while ( (size=file.read(tmp))!=-1 ) {
		for (i=0;i<size;i+=4) {
		    buffer[pos]=(tmp[i]<<56)|(tmp[i+1]<<48)|(tmp[i+2]<<40)|(tmp[i+3]<<32)|
			(tmp[i+4]<<24)|(tmp[i+5]<<16)|(tmp[i+6]<<8)|(tmp[i+7]);
		    pos++;
		}
	    }
	} else {
	    throw new Exception("Huge compressed graph support is not implemented. I'm sorry!");
	}
	file.close();
    }

    public final void close() {
    }

    public final long getPosition() {
	return (((long)word)<<6)|bit;
    }
    public final void setPosition(final long pos) {
	word=(int)(pos>>>6);
	bit=(int)(pos&0x0000003F);
    }
//     public final int getPosition() {
// 	return (word<<5)|bit;
//     }
//     public final void setPosition(final int pos) {
// 	word=pos>>>5;
// 	bit=(pos&0x0000001F);
//     }

    public final long getDimension() {
	return this.dimension;
    }

    public final boolean readBit() {
        long r=buffer[word];
        if (--bit==0) {
            word++;
            bit=WORD_SIZE;
        } else {
            r>>>=bit;
        }
        return (r&1)!=0;
    }

//     public final int readBits(final int dim) {
// // 	int i,t=0;
// // 	for (i=0;i<dim;i++) {
// // 	    int r=buffer[word];
// // 	    if (--bit==0) {
// // 		word++;
// // 		bit=WORD_SIZE;
// // 	    } else {
// // 		r>>>=bit;
// // 	    }
// // 	    t+=t+(r&1);
// // 	}
// // 	return t;

// 	int i,r=0;
// 	for (i=0;i<dim;i++)
// 	    r+=r+(readBit()?1:0);
// 	return r;
//     }

    public final int readBits(final int dim) {
        int l;
	long r;
	if (dim==0) return 0;
	if ( (l=dim-bit)<0 ) {
	    r=((buffer[word]<0?((buffer[word]&0x7F)|0x00000080):buffer[word])>>>(bit-=dim))&(0xFF>>>(WORD_SIZE-dim));
	} else {
	    r=((buffer[word]<0?((buffer[word++]&0x7F)|0x00000080):buffer[word++])&(0xFF>>>(WORD_SIZE-bit)))<<l;
	    bit=WORD_SIZE;
	    while (l>=WORD_SIZE)
		r|=(buffer[word]<0?((buffer[word++]&0x7F)|0x00000080):buffer[word++])<<(l-=WORD_SIZE);
	    if (l>0)
		r|=(buffer[word]<0?((buffer[word]&0x7F)|0x00000080):buffer[word])>>>(bit-=l);
	}
	return (int)r;
    }

    public final long readLong() {
	final String s=Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4));
	return Long.parseLong(s,16);
    }

    public final int readInt() {
	final String s=Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4))+
	    Integer.toHexString(readBits(4));
	return Integer.parseInt(s,16);
    }

}

