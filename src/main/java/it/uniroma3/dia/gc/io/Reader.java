
package it.uniroma3.dia.gc.io;

import java.io.PrintStream;

public interface Reader {

    public void printWord(final PrintStream out);
    public void close();
    public long getPosition();
    public void setPosition(final long pos);
    public long getDimension();
    public boolean readBit();
    public int readBits(final int dim);
    public long readLong();
    public int readInt();

}

