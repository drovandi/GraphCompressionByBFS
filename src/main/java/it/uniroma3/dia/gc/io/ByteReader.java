
package it.uniroma3.dia.gc.io;

import java.io.PrintStream;
import java.io.FileInputStream;

public final class ByteReader implements Reader {

    private static final int WORD_SIZE=8;

    private int word,bit;
    private byte[] buffer;
    private final long dimension;

    @Override
    public void printWord(final PrintStream out) {
	out.println(buffer[word]+":"+bit);
    }

    public ByteReader(final String fileName) throws Exception {
	try (FileInputStream file=new FileInputStream(fileName)) {
            this.word=0;
            this.bit=WORD_SIZE;
            this.dimension=file.getChannel().size();
            if (dimension<=100000000) {
                this.buffer=new byte[(int)dimension];
                file.read(this.buffer);
            } else if (dimension<=Integer.MAX_VALUE) {
                this.buffer=new byte[(int)dimension];
                final byte[] tmp=new byte[100000000];
                int size,pos=0;
                while ( (size=file.read(tmp))!=-1 ) {
                    System.arraycopy(tmp,0,buffer,pos,size);
                    pos+=size;
                }
            } else {
                throw new Exception("Huge compressed graph support is not implemented. I'm sorry!");
    // 	    int pages=(int)(dimension/Integer.MAX_VALUE+1);
    // 	    this.buffer=new byte[pages][];
    // 	    for (i=0;i<pages-1;i++) this.buffer[i]=new byte[Integer.MAX_VALUE];
    // 	    this.buffer[pages-1]=new byte[(int)(dimension-(page-1)*Integer.MAX_VALUE)];
    // 	    final byte[] tmp=new byte[100000000];
    // 	    int size,pos=0;
    // 	    while ( (size=file.read(tmp))!=-1 ) {
    // 		System.arraycopy(tmp,0,buffer,pos,size);
    // 		pos+=size;
    // 	    }
            }
        }
    }

    @Override
    public void close() {
    }

    @Override
    public long getPosition() {
	return (((long)word)<<6)|bit;
    }
    
    @Override
    public void setPosition(final long pos) {
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

    @Override
    public long getDimension() {
	return this.dimension;
    }

    @Override
    public boolean readBit() {
        int r=buffer[word];
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

    @Override
    public int readBits(final int dim) {
        int l,r;
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
	return r;
    }

    @Override
    public long readLong() {
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

    @Override
    public int readInt() {
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

