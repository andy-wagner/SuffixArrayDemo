import java.util.ArrayList;
import java.util.List;

/*************************************************************************
 *  Compilation:  javac Manber.java
 *  Execution:    java Manber < text.txt
 *  Dependencies: In.java
 *
 *  Reads a text corpus from stdin and suffix sorts it in subquadratic
 *  time using a variant of Manber's algorithm.
 *
 *  NOTE: I THINK THIS IS CYCLIC SUFFIX SORTING
 *
 *************************************************************************/

public class Manber {
    private int N;               // length of input string
    private String text;         // input text
    private int[] index;         // offset of ith string in order
    private int[] rank;          // rank of ith string
    private int[] newrank;       // rank of ith string (temporary)
    private int offset;

    public Manber(String s) {
        N    = s.length();
        text = s;
        index   = new int[N+1];
        rank    = new int[N+1];
        newrank = new int[N+1];

        // sentinels
        index[N] = N;
        rank[N] = -1;

        msd();
        doit();
    }

    // 大于等于query的第一个元素的rank, 也就是可能前缀为 query 起始rank
    private int floor(String query) {
        int l = 0;
        int r = N -1;
        while (l < r) {
            int mid = (l + r) / 2;
            if (compare(mid, query) < 0) {
                l = mid + 1;
            } else {
                r = mid;
            }
        }
        return l;
    }

    // 小于等于query+"\255"的第一个元素rank, 也就是前缀为query 的终止rank
    private int ceiling(String query) {
        query += "\255";
        int l = 0;
        int r = N - 1;
        while (l < r) {
            int mid = (l + r) / 2 + 1;
            if (compare(mid, query) > 0) {
                r = mid - 1;
            } else {
                l = mid;
            }
        }
        return l;
    }

    public List<Integer> findPrefixMatch(String query) {
        List<Integer> list = new ArrayList<Integer>();

        int l = floor(query);
        int r = ceiling(query);
        for (int i = l; i <= r; ++i) {
            list.add(index[i]);
        }
        return list;
    }

    private int mod(int x) {
        if (x > N) {
            x -= N;
        }
        return x;
    }
    private int compare(int curRank, String query) {
        int size = Math.min(N, query.length());
        for (int i = 0; i < size; ++i) {
            if (text.charAt(mod(index[curRank] + i)) < query.charAt(i)) return -1;
            if (text.charAt(mod(index[curRank] + i)) > query.charAt(i)) return +1;
        }
        return N - query.length();
    }

    // do one pass of msd sorting by rank at given offset
    private void doit() {
        for (offset = 1; offset < N; offset += offset) {
            //System.out.println("offset = " + offset);

            int count = 0;
            for (int i = 1; i <= N; i++) {
                if (rank[index[i]] == rank[index[i-1]]) count++;
                else if (count > 0) {
                    // sort
                    int left = i-1-count;
                    int right = i-1;
                    quicksort(left, right);

                    // now fix up ranks
                    int r = rank[index[left]];
                    for (int j = left + 1; j <= right; j++) {
                        if (less(index[j-1], index[j]))  {
                            r = rank[index[left]] + j - left;
                        }
                        newrank[index[j]] = r;
                    }

                    // copy back - note can't update rank too eagerly
                    for (int j = left + 1; j <= right; j++) {
                        rank[index[j]] = newrank[index[j]];
                    }

                    count = 0;
                }
            }
        }
    }

    // sort by leading char, assumes extended ASCII (256 values)
    private void msd() {
        // calculate frequencies
        int[] freq = new int[256];
        for (int i = 0; i < N; i++)
            freq[text.charAt(i)]++;

        // calculate cumulative frequencies
        int[] cumm = new int[256];
        for (int i = 1; i < 256; i++)
            cumm[i] = cumm[i-1] + freq[i-1];

        // compute ranks
        for (int i = 0; i < N; i++)
            rank[i] = cumm[text.charAt(i)];

        // sort by first char
        for (int i = 0; i < N; i++)
            index[cumm[text.charAt(i)]++] = i;
    }


    // for debugging
    public void show() {
        String texttext = text + text;  // make cyclic
        System.out.println("j, rank[index[j]], index[j]");
        for (int i = 0; i < N; i++) {
            String s = texttext.substring(index[i], index[i] +  Math.min(40, N));
            System.out.println(s + " " + i + " " + rank[index[i]] + " " + index[i]);
        }
        System.out.println();
    }




/**********************************************************************
 *  Helper functions for comparing suffixes.
 **********************************************************************/

    /**********************************************************************
     * Is the substring text[v..N] lexicographically less than the
     * substring text[w..N] ?
     **********************************************************************/
    private boolean less(int v, int w) {
        if (v + offset >= N) v -= N;
        if (w + offset >= N) w -= N;
        return rank[v + offset] < rank[w + offset];
    }



    /*************************************************************************
     *  Quicksort code from Sedgewick 7.1, 7.2.
     *************************************************************************/

    // swap pointer sort indices
    private void exch(int i, int j) {
        int swap = index[i];
        index[i] = index[j];
        index[j] = swap;
    }


    // SUGGEST REPLACING WITH 3-WAY QUICKSORT SINCE ELEMENTS ARE
    // RANKS AND THERE MAY BE DUPLICATES
    void quicksort(int l, int r) {
        if (r <= l) return;
        int i = partition(l, r);
        quicksort(l, i-1);
        quicksort(i+1, r);
    }

    int partition(int l, int r) {
        int i = l-1, j = r;
        int v = index[r];

        while (true) {

            // find item on left to swap
            while (less(index[++i], v))
                ;

            // find item on right to swap
            while (less(v, index[--j]))
                if (j == l) break;

            // check if pointers cross
            if (i >= j) break;

            exch(i, j);
        }

        // swap with partition element
        exch(i, r);

        return i;
    }
}