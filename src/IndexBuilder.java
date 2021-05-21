import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IndexBuilder extends Thread {
    ArrayList<File> allFiles;
    int startIndex;
    int endIndex;
    int size;

    public IndexBuilder(ArrayList<File> allFiles, int startIndex, int endIndex, int size) {
        this.allFiles = allFiles;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.size = size;
    }

    @Override
    public void run() {
        for(int i = startIndex; i< endIndex; i++){
            try (BufferedReader br = new BufferedReader(new FileReader(allFiles.get(i)))){
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.toLowerCase();
                    line = line
                            .replaceAll("<br /><br />", "")
                            .replaceAll("[^A-Za-z0-9]", " ");


                    String[] words = line.split("\\s*(\\s|,|!|_|\\.)\\s*");

                    for (String word : words) {
                        Indexer.invertedIndex.computeIfAbsent(word, k -> new ConcurrentLinkedQueue<String>())
                                //.add(dir.getParent() + "\\" + dir.getName() + "\\" + item.getName())
                                .add(String.valueOf(allFiles.get(i)));
                        //удаление дубликатов
                        Set<String> concurrentHashSet = new HashSet<>(Indexer.invertedIndex.get(word));

                        Indexer.invertedIndex.get(word).clear();
                        Indexer.invertedIndex.get(word).addAll(concurrentHashSet);

                    }
                }
            }catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}