import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Created by whimsy on 9/5/14.
 */
public class KWIC {
    private Manber manber;

    private TreeMap<Integer, Integer> dict = new TreeMap<Integer, Integer>();

    public KWIC(List<String> content) {
        StringBuilder sb = new StringBuilder();

        int total = 0;
        int cnt = 0;
        for (String str : content) {
            dict.put(total, cnt++);
            sb.append(str).append("\255");  // specail seperator
            total += str.length() + 1;
        }
       // System.out.println(sb.toString());
        manber = new Manber(sb.toString());
     //   manber.show();
    }

    public List<Integer> find(String query) {
        List<Integer> list = new ArrayList<Integer>();

        for (Integer index : manber.findPrefixMatch(query)) {
            list.add(dict.floorEntry(index).getValue());
        }

        return list;

    }

    private static void test(String filename, String query) throws FileNotFoundException {
        URL path = KWIC.class.getResource(filename);
        Scanner in = new Scanner(new File(path.getFile()));

        List<String> list = new ArrayList<String>();
        while (in.hasNext()) {
            list.add(in.next());
        }

        System.out.println(new KWIC(list).find(query));

    }



    public static void main(String [] args) throws FileNotFoundException {
         test("input0.txt", "cwd");
         test("input1.txt", "er");
    }

}
