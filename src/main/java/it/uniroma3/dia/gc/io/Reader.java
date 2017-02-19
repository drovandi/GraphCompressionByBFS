
package it.uniroma3.dia.gc.io;

import java.io.PrintStream;

public interface Reader {

    void printWord(final PrintStream out);
    void close();
    long getPosition();
    void setPosition(final long pos);
    long getDimension();
    boolean readBit();
    int readBits(final int dim);
    long readLong();
    int readInt();

}

