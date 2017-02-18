
package it.uniroma3.dia.gc;

import it.uniroma3.dia.gc.io.Writer;
import it.uniroma3.dia.gc.code.PiEncoder;
import java.io.BufferedReader;
import java.io.PrintStream;

class Chunk {

    private static final int LINES=0;
    private static final int LENGTH=1;
    private static final int COUNTER=2;
    private static final int OUT_DEGREE=3;
    private static final int FIRST_ELEM=4;

    private static final int THRESHOLD=2;
    private static final int THRESHOLD0=6;
    private static final int SIGMA=3;

    public static int[] statistics=new int[30];
    public static long[] stat=new long[4];

    private int LEVEL;
    private int dim,firstN;
    private int[] traverseList;
    private int[][] chunk;
    private int[][] blocks;
    private int[][] values;
    private PiEncoder encoder;

    private int piK,alphaK;

    protected Chunk(final int dim) {
	this(dim,null);
    }

    protected Chunk(final int dim, final PiEncoder encoder) {
	this.LEVEL=dim;
	this.dim=dim;
	this.traverseList=new int[dim];
	this.chunk=new int[dim][];
	this.blocks=new int[dim][];
	this.values=new int[dim][];
	this.encoder=encoder;
    }

    protected int getLevel() {
	return this.LEVEL;
    }

    protected void setEncoder(final PiEncoder encoder) {
	this.encoder=encoder;
    }

    protected final void setOutDegree(final int line, final int outdegree, final int counter) {
	chunk[line]=new int[FIRST_ELEM+2*outdegree];
	blocks[line]=new int[FIRST_ELEM+2*outdegree];
	values[line]=new int[outdegree];
	chunk[line][OUT_DEGREE]=outdegree;
	chunk[line][LENGTH]=FIRST_ELEM+2*outdegree;
	chunk[line][COUNTER]=counter;
    }

    protected final void setValues(final int line, final Integer[] vals, final int length) {
	int pos;
	final int counter=chunk[line][COUNTER];
	for (pos=0;pos<length;pos++) {
	    final int value=vals[pos];
	    values[line][pos]=value;
	    if (line==0) {
		int r;
		int p=FIRST_ELEM+2*pos;
		if (pos==0) {
		    r=counter-value;
		    if (r>=0) r=2*r;
		    else r=2*(-r)-1;
		} else {
		    r=value-values[line][pos-1]-1;
		}
		chunk[line][p]='x';
		chunk[line][p+1]=r;
	    } else { // line>0
		if (pos==0) {
		    int r=counter-value;
		    if (r>=0) r=2*r;
		    else r=2*(-r)-1;
		    final int last=findPrevRow(line-1,pos);

		    if (last==-1) {
			chunk[line][FIRST_ELEM]='x';
			chunk[line][FIRST_ELEM+1]=r;
		    } else {
			final int p=FIRST_ELEM+2*pos;
			if (value>=values[last][pos] && value-values[last][pos]<r) {
			    chunk[line][p]='c';
			    chunk[line][p+1]=value-values[last][pos];
			} else if (value<values[last][pos] && values[last][pos]-value-1<r) {
			    chunk[line][p]='b';
			    chunk[line][p+1]=values[last][pos]-value-1;
			} else {
			    chunk[line][p]='a';
			    chunk[line][p+1]=r;
			}

			// final int p=FIRST_ELEM+2*pos;
			// int r2=values[last][pos]-value;
			// if (r2>=0) r2=2*r2;
			// else r2=2*(-r2)-1;
			// if (r2<r) {
			//     chunk[line][p]='c';
			//     chunk[line][p+1]=r2;
			// } else {
			//     chunk[line][p]='a';
			//     chunk[line][p+1]=r;
			// }

		    }
		} else { // pos>0
		    final int p=FIRST_ELEM+2*pos;
		    final int last=findPrevRow(line-1,pos);
		    int r=value-values[line][pos-1]-1;
		    if (last==-1) {
			chunk[line][p]='x';
			chunk[line][p+1]=r;
		    } else if (values[last][pos]-1<=values[line][pos-1]) {
			chunk[line][p]='x';
			chunk[line][p+1]=r;
		    } else if (value>=values[last][pos]) {
			chunk[line][p]='c';
			chunk[line][p+1]=value-values[last][pos];
		    } else if (values[last][pos]-value-1<r) {
			chunk[line][p]='b';
			chunk[line][p+1]=values[last][pos]-value-1;
		    } else {
			chunk[line][p]='a';
			chunk[line][p+1]=r;
		    }

		    // final int p=FIRST_ELEM+2*pos;
		    // final int last=findPrevRow(line-1,pos);
		    // int r=value-values[line][pos-1]-1;
		    // int r2=0;
		    // if (last!=-1) {
		    // 	r2=values[last][pos]-value;
		    // 	if (r2>=0) r2=2*r2;
		    // 	else r2=2*(-r2)-1;
		    // }
		    // if (last==-1) {
		    // 	chunk[line][p]='x';
		    // 	chunk[line][p+1]=r;
		    // } else if (values[last][pos]-1<=values[line][pos-1]) {
		    // 	chunk[line][p]='x';
		    // 	chunk[line][p+1]=r;
		    // } else if (r2<r) {
		    // 	chunk[line][p]='c';
		    // 	chunk[line][p+1]=r2;
		    // } else {
		    // 	chunk[line][p]='a';
		    // 	chunk[line][p+1]=r;
		    // }

		}
	    }
	}

    }

    private int findPrevRow(final int line, final int pos) {
	int i;
	for (i=line;i>=0 && chunk[i][OUT_DEGREE]<=pos;i--);
	return i;
    }

    protected final void setTraverseList(final int[] traverse, final int firstNode, final boolean zeros, final int nodes) {
	this.firstN=zeros?0:firstNode;
	this.traverseList=traverse;
	this.dim=nodes;
    }

    protected final void compressLines() {
	int i,j;
	for (i=1;i<dim;i++) {
	    if (chunk[i-1].length==chunk[i].length) {
		for (j=FIRST_ELEM;j<chunk[i].length;j++)
		    if (chunk[i-1][j]!=chunk[i][j]) break;
		if (j==chunk[i].length) {
		    chunk[i-1][LINES]++;
		    for (j=i;j<dim-1;j++) {
			chunk[j]=chunk[j+1];
			blocks[j]=blocks[j+1];
		    }
		    dim--;
		    i--;
		}
	    }
	}
    }

    protected final void printChunk(PrintStream out) {
	int i,j;
	if (out==null) return;
	out.print(firstN);
	for (i=0;i<traverseList.length;i++)
	    out.print(" "+traverseList[i]);
	out.println();
	for (i=0;i<dim;i++) {
	    if (chunk[i]==null) break;
	    out.print(chunk[i][OUT_DEGREE]);
	    for (j=FIRST_ELEM;j<chunk[i].length;j+=2) {
		out.print(" "+(char)chunk[i][j]);
		out.print(" "+chunk[i][j+1]);
	    }
	    out.println();
	}
    }

    public final int parse(BufferedReader fin) throws Exception {
	int i,j,counter,lines;
	String line,prev="";

	dim=LEVEL;

	line=fin.readLine();
	if (line==null) {
	    dim=0;
	    return 0;
	}

	String[] st=line.split(" ");
	// il primo vicino nella traverse list
	// se uguale a 0 significa che tutti i valori sono 0
	firstN=Integer.parseInt(st[0]);
	if (firstN>0) {
	    for (i=0;i<dim && i<st.length-1;i++)
		traverseList[i]=Integer.parseInt(st[i+1]);
	    for (;i<dim;i++)
		traverseList[i]=0;
	}

	lines=counter=0;
	for (i=0;i<dim;i++) {
	    counter++;
	    line=fin.readLine();
	    if (line==null) break;
	    if (line.equals(prev)) {
		chunk[lines-1][LINES]++;
		continue;
	    }
	    prev=line;
	    int length,stCount;
	    stCount=0;
	    st=line.split(" ");
	    length=st.length+FIRST_ELEM-1;
	    if (chunk[lines]==null || chunk[lines].length<length) {
		chunk[lines]=new int[length];
		blocks[lines]=new int[length];
	    }
	    chunk[lines][LINES]=0;
	    chunk[lines][LENGTH]=length;
	    chunk[lines][OUT_DEGREE]=Integer.parseInt(st[stCount++]);
	    for (j=FIRST_ELEM;j<length;j+=2) {
		chunk[lines][j]=st[stCount++].charAt(0);
		chunk[lines][j+1]=Integer.parseInt(st[stCount++]);
		blocks[lines][j]=blocks[lines][j+1]=0;
	    }
	    lines++;
	}
	dim=lines;

	return counter;
    }

    private int commons(final int[] lineA, final int[] lineB, final int pos) {
	int i,elem=0,min;
	min=Math.min(lineA[LENGTH],lineB[LENGTH]);
	for (i=pos;i<min;i+=2) {
	    if (lineA[i]<0 || lineB[i]<0) break;
	    if (lineA[i+1]!=lineB[i+1]) break;
	    if (lineA[i]==lineB[i] || ((lineA[i]=='a' && lineB[i]=='x') || (lineA[i]=='x' && lineB[i]=='a')) ) {
		elem++;
	    } else {
		break;
	    }
	}
	return elem;
    }

    private int blockSearch(final int line, final int pos, final int area, final boolean fast) {
	int i,j,elem,tmp,rows;

	if ( (dim-line)*chunk[line][OUT_DEGREE]<area ) return chunk[line][LENGTH];

	if (line>=dim-1) return 0;
	if ((elem=commons(chunk[line],chunk[line+1],pos))<2) return fast?1:elem;
	if ( (dim-line)*elem<area ) return elem;

	if (elem>1048000) elem=1048000;

	rows=2;

	for (i=line+2;i<dim;i++) {
	    tmp=commons(chunk[line],chunk[i],pos);
	    if (tmp<elem) {
		if (tmp<2 || elem*rows>=area) break;
		elem=tmp;
	    }
	    rows++;
	    if (rows==1000) break;
	}

	if (rows*elem<area) return fast?elem:0;

	for (j=pos;j<pos+2*elem;j+=2) {
	    chunk[line][j]=-chunk[line][j]-2;
	    chunk[line][j+1]=-chunk[line][j+1]-2;
	}
	for (i=line+1;i<line+rows;i++) {
	    for (j=pos;j<pos+2*elem;j+=2) {
		chunk[i][j]=-1;
		chunk[i][j+1]=-1;
	    }
	}
	blocks[line][pos]=rows;
	blocks[line][pos+1]=elem;

	return elem;
    }

    public final void blockSearch(final int area, final boolean fast) {
	int i,j;
	for (i=0;i<dim-1;i++) {
	    for (j=FIRST_ELEM;j<chunk[i][LENGTH];j+=2) {
		if (chunk[i][j]<0) continue;
		j+=2*blockSearch(i,j,area,fast);
	    }
	}
    }

    private void columnSearch(final int line, final int pos, final int height) {
	int i,rows;

	if (line>=dim-1) return;

	rows=1;

	for (i=line+1;i<dim;i++) {
	    if (chunk[i][LENGTH]<=pos) break;
	    if ( (chunk[line][pos]==chunk[i][pos] ||
		  (chunk[line][pos]=='x' && chunk[i][pos]=='a') ||
		  (chunk[line][pos]=='a' && chunk[i][pos]=='x') ) &&
		 chunk[line][pos+1]==chunk[i][pos+1])
		rows++;
	    else break;
	    if (rows==1000) break;
	}

	if (rows<height) return;

	chunk[line][pos]=-chunk[line][pos]-2;
	chunk[line][pos+1]=-chunk[line][pos+1]-2;
	for (i=line+1;i<line+rows;i++) {
	    chunk[i][pos]=-1;
	    chunk[i][pos+1]=-1;
	}
	blocks[line][pos]=rows;
	blocks[line][pos+1]=1;
    }

    public final void columnSearch(final int height) {
	int i,j;
	if (height>dim || height<=0) return;
	for (i=0;i<=dim-height;i++) {
	    for (j=FIRST_ELEM;j<chunk[i][LENGTH];j+=2) {
		if (chunk[i][j]<0) continue;
		columnSearch(i,j,height);
	    }
	}
    }

    public final void print(PrintStream out) {
	if (dim==0) return;
	int i,j;
	if (firstN>0) {
	    for (i=0;i<traverseList.length;i++)
		out.print(traverseList[i]+" ");
	}
	out.println();
	for (i=0;i<dim;i++) {
	    if (chunk[i]!=null) {
		out.print(chunk[i][LINES]+" ");
		out.print(chunk[i][OUT_DEGREE]+" ");
		for (j=FIRST_ELEM;j<chunk[i][LENGTH];j+=2) {
		    if (chunk[i][j]==-1) out.print("@ @ ");
		    else if (blocks[i][j]>0) {
			out.print(blocks[i][j]+"x"+blocks[i][j+1]+" ");
		    }
		    else if (chunk[i][j]<0) {
			out.print((char)(-chunk[i][j]-2)+" "+(-chunk[i][j+1]-2)+" ");
		    }
		    else out.print((char)chunk[i][j]+" "+chunk[i][j+1]+" ");
		}
		out.println();
	    }
	}
    }

    public final long setCompressionParams() throws Exception {
	if (dim==0) return 0;
	int i,j,k;

	long bits=0;
	int lastDegOut=-1;
	long[] bits2=new long[4];
	long[] alpha=new long[30];

	for (i=0;i<dim;i++) {
	    for (j=FIRST_ELEM;j<chunk[i][LENGTH];j+=2) {
		if (chunk[i][j]==-1) continue;
		if (chunk[i][j]<0) {
		    chunk[i][j]=-chunk[i][j]-2;
		    chunk[i][j+1]=-chunk[i][j+1]-2;
		}
	    }
	}

	for (k=0;k<bits2.length;k++) {
	    for (i=0;i<dim;i++) {
		if (k==0) {
		    bits+=encoder.code0(chunk[i][LINES],0)[1];
		    if (lastDegOut==-1) {
			bits+=encoder.code0(chunk[i][OUT_DEGREE],0)[1];
		    } else {
			int r=chunk[i][OUT_DEGREE]-lastDegOut;
			if (r<0) r=2*(-r)-1; else r=2*r;
			bits+=encoder.code0(r,0)[1];
		    }
		    if (chunk[i][OUT_DEGREE]>0) {
			lastDegOut=chunk[i][OUT_DEGREE];
		    }
		}
		for (j=FIRST_ELEM;j<chunk[i][LENGTH];j+=2) {
		    if (chunk[i][j]==-1) continue;
		    if (chunk[i][j+1]==-1) throw new Exception();
		    int l,count=0;
		    for (l=j+2;l<chunk[i][LENGTH];l+=2) {
			if ( ( chunk[i][j]==chunk[i][l] ||
			       (chunk[i][j]=='a' && chunk[i][l]=='x') ||
			       (chunk[i][j]=='x' && chunk[i][l]=='a') )
			     && chunk[i][j+1]==chunk[i][l+1]
			     && blocks[i][l]==0 ) {
			    count++;
			    continue;
			}
			break;
		    }

		    boolean red=blocks[i][j]>0;
		    if (chunk[i][j+1]==0 && count>THRESHOLD0)
			red=true;
		    else if (chunk[i][j+1]!=0 && count>THRESHOLD)
			red=true;
		    else
			count=0;

		    if (k==0) {
			alpha[chunk[i][j]-'a']++;
			statistics[chunk[i][j]-'a']++;
		    }

		    if (!red) {
			if (chunk[i][j+1]>=SIGMA) bits2[k]+=encoder.code0(chunk[i][j+1]+1,k)[1];
			else bits2[k]+=encoder.code0(chunk[i][j+1],k)[1];
		    } else {
			bits2[k]+=encoder.code0(SIGMA,k)[1];
			bits2[k]+=encoder.code0(chunk[i][j+1],k)[1];
			if (k==0) {
			    if (blocks[i][j]>0 && count!=0) {bits+=2; statistics['m'-'a']++;}
			    else if (blocks[i][j]>0) {bits+=1; statistics['n'-'a']++;}
			    else {bits+=2; statistics['o'-'a']++;}
			    if (chunk[i][j+1]==0 && count>THRESHOLD0)
				bits+=encoder.code0(count-THRESHOLD0,0)[1];
			    else if (chunk[i][j+1]!=0 && count>THRESHOLD)
				bits+=encoder.code0(count-THRESHOLD,0)[1];
			    if (blocks[i][j]>0) {
				bits+=encoder.code0(blocks[i][j+1]-1,0)[1];
				// AREA MIN 6
// 				if (blocks[i][j+1]==1) bits+=encoder.code0(blocks[i][j]-6,0)[1];
// 				else if (blocks[i][j+1]==2) bits+=encoder.code0(blocks[i][j]-3,0)[1];
// 				else if (blocks[i][j+1]<6) bits+=encoder.code0(blocks[i][j]-2,0)[1];
// 				else bits+=encoder.code0(blocks[i][j]-1,0)[1];
				// AREA MIN 8
				if (blocks[i][j+1]==1) bits+=encoder.code0(blocks[i][j]-5,0)[1];
				else if (blocks[i][j+1]==2) bits+=encoder.code0(blocks[i][j]-4,0)[1];
				else if (blocks[i][j+1]==3) bits+=encoder.code0(blocks[i][j]-3,0)[1];
				else if (blocks[i][j+1]<8) bits+=encoder.code0(blocks[i][j]-2,0)[1];
				else bits+=encoder.code0(blocks[i][j]-1,0)[1];
				// AREA MIN 9
// 				if (blocks[i][j+1]==1) bits+=encoder.code0(blocks[i][j]-5,0)[1];
// 				else if (blocks[i][j+1]==2) bits+=encoder.code0(blocks[i][j]-5,0)[1];
// 				else if (blocks[i][j+1]<5) bits+=encoder.code0(blocks[i][j]-3,0)[1];
// 				else if (blocks[i][j+1]<9) bits+=encoder.code0(blocks[i][j]-2,0)[1];
// 				else bits+=encoder.code0(blocks[i][j]-1,0)[1];
			    }
			}
		    }
		    j+=2*count;
		}
	    }
	}

	int codeK=0;
	long min=bits2[0];
	for (i=1;i<bits2.length;i++)
	    if (min>bits2[i]) {min=bits2[i];codeK=i;}
	bits+=min;
 	stat[codeK]++;

	// set the value for the PiEncoder
	piK=codeK;

	long traverseBits=1;
	if (firstN>0) {
	    int prev=traverseList[0];
	    int count=0;
	    for (i=1;i<traverseList.length;i++) {
		if (traverseList[i]==prev) {
		    count++;
		} else {
		    traverseBits+=encoder.code0(prev,0)[1];
		    traverseBits+=encoder.code0(count,0)[1];
		    prev=traverseList[i];
		    count=0;
		}
	    }
	    traverseBits+=encoder.code0(prev,0)[1];
	    traverseBits+=encoder.code0(count,0)[1];
	}

	long alphaBits;
	codeK='a'-'a';
	long max=alpha['a'-'a'];
	for (i='b'-'a';i<='c'-'a';i++)
	    if (max<=alpha[i]) {max=alpha[i];codeK=i;}
	alphaBits=alpha[codeK];
	if (codeK!='a'-'a')
	    alphaBits+=2*alpha['a'-'a'];
	if (codeK!='b'-'a')
	    alphaBits+=2*alpha['b'-'a'];
	if (codeK!='c'-'a')
	    alphaBits+=2*alpha['c'-'a'];

	alphaK=codeK+'a';

	if (codeK=='c'-'a')
	    return bits+alphaBits+1+traverseBits+2;
	return bits+alphaBits+2+traverseBits+2;
    }

    public long writeChunk(final Writer writer) throws Exception {
	if (dim==0) return 0;
	int i,j;

	long bits=0;

	if (firstN==0) {
	    bits+=writer.writeBit(false);
	} else {
	    bits+=writer.writeBit(true);
	    int prev=traverseList[0];
	    int count=0;
	    for (i=1;i<traverseList.length;i++) {
		if (traverseList[i]==prev) {
		    count++;
		} else {
		    bits+=encoder.encode0(prev,0);
		    bits+=encoder.encode0(count,0);
		    prev=traverseList[i];
		    count=0;
		}
	    }
	    bits+=encoder.encode0(prev,0);
	    bits+=encoder.encode0(count,0);
	}

	bits+=writer.writeBits(piK,2);
        switch (alphaK) {
            case 'c':
                bits+=writer.writeBits(1,1);
                break;
            case 'b':
                bits+=writer.writeBits(1,2);
                break;
            default:
                bits+=writer.writeBits(0,2);
                break;
        }

	int lastDegOut;
	lastDegOut=-1;
	for (i=0;i<dim;i++) {
	    bits+=encoder.encode0(chunk[i][LINES],0);
	    if (lastDegOut==-1) {
		bits+=encoder.encode0(chunk[i][OUT_DEGREE],0);
	    } else {
		int r=chunk[i][OUT_DEGREE]-lastDegOut;
		if (r<0) r=2*(-r)-1; else r=2*r;
		bits+=encoder.encode0(r,0);
	    }
	    if (chunk[i][OUT_DEGREE]>0) {
		lastDegOut=chunk[i][OUT_DEGREE];
	    }

	    for (j=FIRST_ELEM;j<chunk[i][LENGTH];j+=2) {
		if (chunk[i][j]==-1) continue;
		if (chunk[i][j+1]==-1) throw new Exception();
		int l,count=0;
		for (l=j+2;l<chunk[i][LENGTH];l+=2) {
		    if ( ( chunk[i][j]==chunk[i][l] ||
			   (chunk[i][j]=='a' && chunk[i][l]=='x') ||
			   (chunk[i][j]=='x' && chunk[i][l]=='a') )
			 && chunk[i][j+1]==chunk[i][l+1]
			 && blocks[i][l]==0 ) {
			count++;
			continue;
		    }
		    break;
		}

		switch (chunk[i][j]) {
		case 'a':
                    switch (alphaK) {
                        case 'a':
                            bits+=writer.writeBits(1,1);
                            break;
                        case 'b':
                            bits+=writer.writeBits(1,2);
                            break;
                        default:
                            bits+=writer.writeBits(1,2);
                            break;
                    }
		    break;
		case 'b':
                    switch (alphaK) {
                        case 'a':
                            bits+=writer.writeBits(1,2);
                            break;
                        case 'b':
                            bits+=writer.writeBits(1,1);
                            break;
                        default:
                            bits+=writer.writeBits(0,2);
                            break;
                    }
		    break;
		case 'c':
                    switch (alphaK) {
                        case 'a':
                            bits+=writer.writeBits(0,2);
                            break;
                        case 'b':
                            bits+=writer.writeBits(0,2);
                            break;
                        default:
                            bits+=writer.writeBits(1,1);
                            break;
                    }
		    break;
		case 'x':
		    break;
		default:
		    throw new Exception("BIT");
		}

		boolean red=blocks[i][j]>0;
		if (chunk[i][j+1]==0 && count>THRESHOLD0)
		    red=true;
		else if (chunk[i][j+1]!=0 && count>THRESHOLD)
		    red=true;
		else
		    count=0;

		if ( !red ) {
		    if (chunk[i][j+1]>=SIGMA) bits+=encoder.encode0(chunk[i][j+1]+1,piK);
		    else bits+=encoder.encode0(chunk[i][j+1],piK);
		} else {
		    bits+=encoder.encode0(SIGMA,piK);
		    bits+=encoder.encode0(chunk[i][j+1],piK);

		    if (blocks[i][j]>0 && count!=0) bits+=writer.writeBits(1,2);
		    else if (blocks[i][j]>0) bits+=writer.writeBits(1,1);
		    else bits+=writer.writeBits(0,2);

		    if (chunk[i][j+1]==0 && count>THRESHOLD0)
			bits+=encoder.encode0(count-THRESHOLD0,0);
		    else if (chunk[i][j+1]!=0 && count>THRESHOLD)
			bits+=encoder.encode0(count-THRESHOLD,0);
		    else
			count=0;

		    if (blocks[i][j]>0) {
			bits+=encoder.encode0(blocks[i][j+1]-1,0);
			// AREA MIN 8
			if (blocks[i][j+1]==1) bits+=encoder.encode0(blocks[i][j]-5,0);
			else if (blocks[i][j+1]==2) bits+=encoder.encode0(blocks[i][j]-4,0);
			else if (blocks[i][j+1]==3) bits+=encoder.encode0(blocks[i][j]-3,0);
			else if (blocks[i][j+1]<8) bits+=encoder.encode0(blocks[i][j]-2,0);
			else bits+=encoder.encode0(blocks[i][j]-1,0);
		    }
		}
		j+=2*count;
	    }
	}

	return bits;
    }

}

