import java.util.*;
import java.io.*;



public class WordChessImp implements WordChess {

    public static void main(String[] args) throws FileNotFoundException {
        Scanner sc = new Scanner(new File("corncob_caps.txt"));
        List<String> lines = new ArrayList<String>();
        while (sc.hasNextLine()) {
            lines.add(sc.nextLine());
        }

        String arr[] = new String[lines.size()];

        arr = lines.toArray(arr);

        String x = "DIE";
        String y = "CAT";

        findPath(arr, x, y);

    }



    public static String[] findPath(String[] dictionary, String startWord, String endWord) {

        Set<String> wordlist = new HashSet<>();
        HashMap<String, String> parentNodes = new LinkedHashMap<String, String>();
        Queue<String> queue = new LinkedList<>();

        boolean error = false;
        if (startWord.length() != endWord.length()) {
        error = true;
        }

        int word_len = startWord.length();

        for (String s: dictionary) {
            if (s.length()== word_len ) {
                wordlist.add(s);
            }
        }

        if(!wordlist.contains(endWord)) error = true;


        if(wordlist.contains(startWord)) wordlist.remove(startWord);

        int wordfound = 0;
        queue.offer(startWord);

        while(!queue.isEmpty() && wordfound==0 && error == false) {
            String pointer = queue.remove();

            for(int i =0;i<pointer.length();i++) {

                char[] tempWordArray = pointer.toCharArray();

                for (char c = 'A'; c <= 'Z'; c++) {
                    tempWordArray[i] = c;

                    String newWord = new String(tempWordArray);

                    if (wordlist.contains(newWord)) {
                            parentNodes.put(newWord, pointer);
                            queue.add(newWord);
                            wordlist.remove(newWord);
                            if (newWord == endWord) {
                                wordfound = 1;
                                break;
                            }

                        } 


                    }
            }
            wordlist.remove(pointer);

        }

        ArrayList<String> pathway = new ArrayList<>();
        String hashKey = endWord;
        pathway.add(hashKey);


        while(parentNodes.containsKey(hashKey))
        {
            hashKey = parentNodes.get(hashKey);
            pathway.add(hashKey);
        }

        Collections.reverse(pathway);

        String[] data = pathway.toArray(new String[0]);

        for(String s: pathway) System.out.println(s);

        return data;




    }
    


}