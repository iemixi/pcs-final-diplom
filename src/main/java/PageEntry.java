import lombok.Data;

@Data
public class PageEntry implements Comparable<PageEntry> {
    private final String pdfName;
    private final int page;
    private final long count;

    @Override
    public int compareTo(PageEntry o) {
        return Long.compare(o.getCount(), this.getCount());
    }
}
