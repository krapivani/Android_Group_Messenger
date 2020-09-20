package edu.buffalo.cse.cse486586.groupmessenger2;

import java.util.Comparator;

public class Messages implements Comparable<Messages>, Comparator<Messages> {

    public double seq;
    public String msg;
    public String port;
    public Boolean deliver;
    //public Integer agreed;

    Messages(String msg, double seq, String port, Boolean deliver){
        this.msg = msg;
        this.seq = seq;
        this.port = port;
        this.deliver = deliver;
        //this.agreed = agreed;
    }

    public Messages() {

    }


    /*public int compareTo(Messages other) {
        return this.seq.compareTo(other.seq);
    }*/

    @Override
    public int compare(Messages lhs, Messages rhs) {

        if(lhs.seq < rhs.seq){
            return -1;
        }
        else if (lhs.seq == rhs.seq){
            return 0;
        }
        return 1;
    }

    /**
     * Compares this object to the specified object to determine their relative
     * order.
     *
     * @param another the object to compare to this instance.
     * @return a negative integer if this instance is less than {@code another};
     * a positive integer if this instance is greater than
     * {@code another}; 0 if this instance has the same order as
     * {@code another}.
     * @throws ClassCastException if {@code another} cannot be converted into something
     *                            comparable to {@code this} instance.
     */
    @Override
    public int compareTo(Messages another) {
        return 0;
    }
}
