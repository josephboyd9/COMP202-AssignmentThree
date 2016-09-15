import java.util.*;
import java.io.*;
import java.net.*;

/*
 * prefixComparator
 *
 * this class is used when sorting an array in Java.
 */
class prefixComparator implements Comparator<prefix>
{
    public int compare(prefix a, prefix b)
    {
	/*
	 * XXX:
	 * return a value such that the array is ordered from
	 * most specific to least specific.  take a look at the
	 * documentation at
	 *
	 * http://docs.oracle.com/javase/7/docs/api/java/util/Comparator.html
	 *
	 * on how the return value from this method will impact sort order.
	 * make sure the longest prefixes are sorted so that they come
	 * first!
	 */
        //a is a shorter prefix than b
		if(a.len<b.len)
            return 1;
        //a is a longer prefix than b
        else if(a.len>b.len)
            return -1;
        //a and b are the same size
        else return 0;
    }
};

/*
 * prefix
 *
 * this class holds details of a prefix: the network address, the
 * prefix length, and details of the autonomous systems that announce
 * it.
 *
 */
class prefix
{
    public int[]       net = {0,0,0,0};
    public int         len;
    public String      asn;

    public prefix(String net, int len, String asn)
    {
	/*
	 * XXX:
	 * initialise the object given the inputs.  break
	 * the network ID into four integers.
	 */
	this.len=len;
	this.asn=asn;
	//this.net=Arrays.stream(net.split("\\.")).mapToInt(Integer::parseInt).toArray();

	
	String[] netS=new String[4];
	if(this.net.length!=4||netS.length!=4) System.out.println("netS size = "+netS.length);
	for(int i=0; i<4; i++){
	    netS=net.split("\\.");
	    this.net[i]=Integer.parseInt(netS[i]);
	}
    }

    public String toString()
    {
	/* pretty print out of the prefix! my lecturer is kind! */
	return net[0] + "." + net[1] + "." + net[2] + "." + net[3] + "/" + len;
    }

    /*
     * match
     *
     * given an address, determine if it is found in this
     * prefix structure or not.
     */
    public boolean match(String addr)
    {
	//0x80=1000 0000, 0xc0=1100 0000, etc
	int[] mask = {0x80, 0xC0, 0xE0, 0xF0, 0xF8, 0xFC, 0xFE, 0xFF};
	int i;

	/*
	 * XXX:
	 * break up the address passed in as a string
	 */
	String[] destS=addr.split("\\.");
	int[] dest = new int[4];
	for(i=0; i<4; i++){
	    dest[i]=Integer.parseInt(destS[i]);
	}
	i=len;
	int maskF=0;
	while(i<0){
	    if(i<=8){
		maskF+=mask[i-1];
		i=0;
	    }else{
		maskF+=mask[7];
		i-=8;
	    }
	}

	////////////////////////////////TROUBLESHOOTING//////////////////////////
       
	//System.out.println("prefix : "+toString());
	//System.out.println("address input : "+addr);
	//for(String s:destS)System.out.print(s);
	//System.out.println();
	//for(int num:dest)System.out.print(num);
	//System.out.println();
	//System.out.println("destination address array size : "+dest.length);
        //System.out.println("mask array size : "+mask.length);
	//System.out.println("net array size : "+net.length);
	////////////////////////////////TROUBLESHOOTING//////////////////////////
	for(i=0; i<4; i++) {
	    /*
	     * XXX:
	     * compare up to four different values in the dotted quad,
	     * (i.e. enough to cover this.len) to determine if this
	     * address is a match or not
	     */
	    if(len-(i*4)>8)
		maskF=mask[7];
	    else maskF=mask[len-(i*4)];
	    int num = maskF&dest[i];
	    if(num!=net[i])
		return false;
	}
	return true;

    }
};

class ip2as
{
    public static void main(String args[])
    {
	if(args.length < 3) {
	    /* always check the input to the program! */
	    System.err.println("usage: ip2as <prefixes> <asnames> [ip0 ip1 .. ipN]");
	    return;
	}

	/* read the prefix list into a list */
	ArrayList<prefix> list = new ArrayList<prefix>();
	try {
	    BufferedReader file = new BufferedReader(new FileReader(args[0]));
	    String line;
	    
	    while((line = file.readLine()) != null) {
		/* XXX: add code to parse the ip2as line */
		String[] in=line.replaceAll("\\/", " ").split(" ");
		String net=in[0];
		String ases=in[2];
		int len=Integer.parseInt(in[1]);

		/* create a new prefix object and stuff it in the list */
		if(8<len&&len<=24){
		    prefix pf = new prefix(net, len, ases);
		    list.add(pf);
		}
	    }
	    file.close();
	}
	catch(FileNotFoundException e) {
	    System.err.println("could not open prefix file " + args[0]);
	    return;
	}
	catch(IOException e) {
	    System.err.println("error reading prefix file " + args[0] + ": " +e);
	}

	/*
	 * take the list of prefixes and transform it into a sorted array
	 * i'd like to thank my lecturer for giving me this code.
	 */
	prefix []x = new prefix[list.size()];
	list.toArray(x);
	Arrays.sort(x, new prefixComparator());
	//for(int i=0;i<20;i++)
	//    System.out.println(x[i].toString()+" "+x[i].asn);

	/*
	 * read in the asnames file so that we can report the
	 * network's name with its ASN
	 */
	ArrayList<String> asNames=new ArrayList<String>();
	try {
	    BufferedReader file = new BufferedReader(new FileReader(args[1]));
	    String line;
	    
	    while((line = file.readLine())!= null)
		asNames.add(line);
	    file.close();
	}
	catch(FileNotFoundException e) {
	    System.err.println("could not open asname file " + args[1]);
	    return;
	}
	catch(IOException e) {
	    System.err.println("error reading asname file " + args[1] + ": " +e);
	}
	String[] y=new String[asNames.size()];
	
	
	/*
	 * for all IP addresses supplied on the command line, print
	 * out the corresponding ASes that announce a corresponding
	 * prefix, as well as their names.  if there is no
	 * corresponding prefix, print the IP address and then say no
	 * corresponding prefix.
	 */
	for(int i=2; i<args.length; i++) {
	    int matched = 0;
	    
	    /*
	     * x contains the sorted array of prefixes, organised longest
	     * to shortest prefix match
	     */
	    for(int j=0; j<x.length; j++) {
		prefix p = x[j];
		
		/*
		 * XXX:
		 * check if this prefix matches the IP address passed in
		 */
		if(p.match(args[i])){
		    for(String asn : p.asn.split("_")){
			System.out.println(args[i]+" "+p.toString()+" "+y[Integer.parseInt(asn)]);
			matched++;
		    }
		}
	    }
	    /*
	     * XXX:
	     * print something out if it was not matched
	     */
	    if(matched==0)
		System.out.println(args[0]+" : no prefix");
	}
	return;
    }
};
