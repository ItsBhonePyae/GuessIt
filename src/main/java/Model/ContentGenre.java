package Model;

public class ContentGenre {

    private int contentId;
    private int genreId;

    public ContentGenre() {
    }

    public ContentGenre(int contentId, int genreId) {
        this.contentId = contentId;
        this.genreId = genreId;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    @Override
    public String toString() {
        return "ContentGenre{" +
                "contentId=" + contentId +
                ", genreId=" + genreId +
                '}';
    }
}
