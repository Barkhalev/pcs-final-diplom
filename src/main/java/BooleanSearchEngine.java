import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    private Map<String, List<PageEntry>> wordsOnPage = new HashMap<>();
    private static Set<String> stopList = new HashSet<>();
    private final File stopWords = new File("stop-ru.txt");

    private void readStopList() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(stopWords))) {
            String line = br.readLine();
            while (line != null) {
                stopList.add(line);
                line = br.readLine();
            }
        }
    }

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        readStopList();
        File[] pdfs = pdfsDir.listFiles();
        for (var pdf : pdfs) {
            var doc = new PdfDocument(new PdfReader(pdf));
            int pages = doc.getNumberOfPages();
            for (int i = 1; i <= pages; i++) {
                String text = PdfTextExtractor.getTextFromPage(doc.getPage(i));
                String[] words = text.split("\\P{IsAlphabetic}+");
                Map<String, Integer> freqs = new HashMap<>();
                for (String word : words) {
                    if ((word.isEmpty()) || (stopList.contains(word.toLowerCase()))) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                }
                for (String key : freqs.keySet()) {
                    int count = freqs.get(key);
                    PageEntry pageEntry = new PageEntry(pdf.getName(), i, count);
                    if (wordsOnPage.containsKey(key)) {
                        wordsOnPage.get(key).add(pageEntry);
                    } else {
                        List<PageEntry> list = new ArrayList<>();
                        list.add(pageEntry);
                        wordsOnPage.put(key, list);
                    }
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String words) {
        String[] input = words.toLowerCase().split(" ");

        List<PageEntry> respond = new ArrayList<>();
        for (String word : input) {
            respond.addAll(wordsOnPage.getOrDefault(word, Collections.emptyList()));
        }
        Collections.sort(respond);
        return respond;
    }
}
