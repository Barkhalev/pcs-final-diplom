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
                for (Map.Entry<String, Integer> entry : freqs.entrySet()) {
                    PageEntry pageEntry = new PageEntry(pdf.getName(), i, entry.getValue());
                    if (!wordsOnPage.containsKey(entry.getKey())) {
                        wordsOnPage.put(entry.getKey(), new ArrayList<>());
                    }
                    wordsOnPage.get(entry.getKey()).add(pageEntry);
                }
            }
        }
        for (Map.Entry<String, List<PageEntry>> entry : wordsOnPage.entrySet()) {
            entry.getValue().sort(PageEntry::compareTo);
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        return this.wordsOnPage.getOrDefault(word.toLowerCase(), Collections.emptyList());
    }
}
