
package it.uniroma3.dia.gc;

import java.io.Closeable;
import java.io.RandomAccessFile;
import java.util.Random;

public class OfflineArray implements Closeable {

    private static int counter=0;

    private long dim;
    private long first;
    private int[] buffer;
    private RandomAccessFile array;

    public static void main(String[] args) {
	int i,d,n,j;
	d=100;
	n=1000000;
	Random r=new Random();
	OfflineArray oa=new OfflineArray(n,d,0);
	int[] v=new int[n];
	for (i=0;i<n;i++) {
	    v[i]=r.nextInt();
	    oa.setElement(i,v[i]);
	}
	for (j=0;j<n;j++) {
	    i=r.nextInt(n);
	    if (oa.getElement(i)!=v[i]) {
		System.err.println("ERRORE "+oa.getElement(i)+" "+v[i]);
		System.exit(1);
	    }
	}
    }

    public OfflineArray(long dim, int bufferSize, int value) {
	try {
	    long i;
	    this.dim=dim;
	    this.array=new RandomAccessFile("int_array_"+this.counter+".tmp","rw");
	    this.counter++;
	    if (dim<bufferSize)
		this.buffer=new int[(int)dim];
	    else
		this.buffer=new int[bufferSize];
	    for (i=0;i<dim;i++) {
		if (i<this.buffer.length)
		    this.buffer[(int)i]=value;
		this.array.writeInt(value);
	    }
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    System.exit(1);
	}
    }

    public long getDimension() {
	return this.dim;
    }

    public void setElement(long pos, int value) {
	try {
	    if ( pos<first || pos>=first+buffer.length ) {
		flushBuffer();
		loadBuffer(pos);
	    }
	    buffer[(int)(pos-first)]=value;
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    System.exit(1);
	}
    }

    public int getElement(long pos) {
	try {
	    if ( pos<first || pos>=first+buffer.length ) {
		flushBuffer();
		loadBuffer(pos);
	    }
	    return buffer[(int)(pos-first)];
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    System.exit(1);
	    return 0;
	}
    }

    @Override
    public void close() {
	try {
	    this.buffer=null;
	    this.array.close();
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    System.exit(1);
	}
    }

    public void flushBuffer() {
	try {
	    int i;
	    array.seek(4*first);
	    for (i=0;i<buffer.length;i++)
		array.writeInt(buffer[i]);
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    System.exit(1);
	}
    }

    public void loadBuffer(long pos) {
	try {
	    int i;
	    this.first=(long)Math.min(pos-10,dim-buffer.length);
	    if (this.first<0) this.first=0;
	    array.seek(4*first);
	    for (i=0;i<buffer.length;i++)
		buffer[i]=array.readInt();
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    System.exit(1);
	}
    }

}

