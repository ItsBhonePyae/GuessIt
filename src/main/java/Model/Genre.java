package Model;

public class Genre {

    private int genreId;
    private String contentType;
    private String genreName;

    public Genre() {
    }

    public Genre(int genreId, String contentType, String genreName) {
        this.genreId = genreId;
        this.contentType = contentType;
        this.genreName = genreName;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    @Override
    public String toString() {
        return "Genre{" +
                "genreId=" + genreId +
                ", contentType='" + contentType + '\'' +
                ", genreName='" + genreName + '\'' +
                '}';
    }
}
