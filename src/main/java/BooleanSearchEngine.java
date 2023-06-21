import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanSearchEngine implements SearchEngine {
    private final Map<String, SortedSet<PageEntry>> wordToPageEntries = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        if (!(pdfsDir.exists() && pdfsDir.isDirectory())) {
            throw new IOException("Invalid path to pdfs: %s".formatted(pdfsDir));
        }

        indexFiles(pdfsDir);
    }

    private void indexFiles(File pdfsDir) throws IOException {
        for (File pdfFile : pdfsDir.listFiles()) {
            indexFile(pdfFile);
        }
    }

    public void indexFile(File pdfFile) throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfFile));
        final int pagesAmount = pdfDocument.getNumberOfPages();
        final String fileName = pdfFile.getName();

        for (int pageIndex = 1; pageIndex <= pagesAmount; ++pageIndex) {
            Map<String, Long> wordCounting = getCountedPageWords(pdfDocument, pageIndex);

            processPageWords(fileName, pageIndex, wordCounting);
        }
    }

    private static Map<String, Long> getCountedPageWords(PdfDocument pdfDocument, int pageIndex) {
        String[] pageWords = getPageWords(pdfDocument, pageIndex);

        return Stream.of(pageWords)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private void processPageWords(String fileName, int pageIndex, Map<String, Long> wordCounting) {
        wordCounting.forEach((word, value) -> {
            PageEntry pageEntry = new PageEntry(fileName, pageIndex, value);

            addPageEntryToWord(pageEntry, word);
        });
    }

    private void addPageEntryToWord(PageEntry pageEntry, String word) {
        Set<PageEntry> pageEntries = wordToPageEntries.get(word);

        if (pageEntries == null) {
            wordToPageEntries.put(word, new TreeSet<>(Set.of(pageEntry)));
        } else {
            pageEntries.add(pageEntry);
        }
    }

    private static String[] getPageWords(PdfDocument pdfDocument, int pageIndex) {
        PdfPage page = pdfDocument.getPage(pageIndex);
        String textFromPage = PdfTextExtractor.getTextFromPage(page);

        return textFromPage.split("\\P{IsAlphabetic}+");
    }

    @Override
    public List<PageEntry> search(String word) {
        SortedSet<PageEntry> pageEntries = wordToPageEntries.get(word);

        return pageEntries == null
                ? Collections.emptyList()
                : new ArrayList<>(pageEntries);
    }
}
