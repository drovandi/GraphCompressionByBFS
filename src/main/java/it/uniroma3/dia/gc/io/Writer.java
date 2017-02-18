
package it.uniroma3.dia.gc.io;

import java.io.*;

public class Writer {

    private static final int WORD_SIZE=8;
    private static final int BUFFER=10000000;

    private final DataOutputStream out;
    private final byte[] buffer;
    private int word,bit,tot;

    public void printWord(final PrintStream out) {
	out.println(buffer[word]+":"+bit);
    }

    public Writer(final String fileName) throws Exception {
	this.out=new DataOutputStream(new FileOutputStream(fileName));
	this.buffer=new byte[BUFFER];
	this.tot=this.word=0;
	this.bit=this.WORD_SIZE;
    }

    public final int close() throws Exception {
        flush(true);
        out.close();
        return this.tot;
    }

    public final int writeBit(final boolean b) throws Exception {
	writeBits(b?1:0,1);
	return 1;
    }

    public final int writeBits(final int v, final int dim) throws Exception {
	if (dim<=bit) {
            tot+=dim;
	    bit-=dim;
	    buffer[word]|=(v<<bit);
	    if (bit==0) {
                word++;
                bit=WORD_SIZE;
            }
        } else {
            int l1,l2;
            l1=bit;
            l2=dim-bit;
            writeBits(v>>>l2,l1);
            writeBits(v&(0xFFFFFFFF>>>(32-l2)),l2);
        }
        if (word>=BUFFER-2) flush(false);
	return dim;
    }

    public final int writeLong(final long v) throws Exception {
	writeBits((int)(v>>>32),32);
	writeBits((int)(v&0x00000000FFFFFFFF),32);
	return 64;
    }
    
    private final void flush(boolean complete) throws Exception {
        int i;
        if (complete) {
	    out.write(buffer,0,word+1);
//             for (i=0;i<=word && i<BUFFER;i++)
//                 out.writeInt(buffer[i]);
            for (i=0;i<BUFFER;i++)
                buffer[i]=0;
            word=0;
            bit=WORD_SIZE;
        } else {
	    out.write(buffer,0,word);
//             for (i=0;i<word;i++)
//                 out.writeInt(buffer[i]);
            for (i=0;word<BUFFER;word++)
                buffer[i++]=buffer[word];
            for (;i<BUFFER;i++)
                buffer[i]=0;
            word=0;
        }
    }

}
