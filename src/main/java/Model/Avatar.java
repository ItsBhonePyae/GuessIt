package Model;

public class Avatar {

    private int avatarId;
    private String avatarName;
    private String avatarFile;

    public Avatar() {
    }

    public Avatar(int avatarId, String avatarName, String avatarFile) {
        this.avatarId = avatarId;
        this.avatarName = avatarName;
        this.avatarFile = avatarFile;
    }

    public int getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }

    public String getAvatarName() {
        return avatarName;
    }

    public void setAvatarName(String avatarName) {
        this.avatarName = avatarName;
    }

    public String getAvatarFile() {
        return avatarFile;
    }

    public void setAvatarFile(String avatarFile) {
        this.avatarFile = avatarFile;
    }

    @Override
    public String toString() {
        return "Avatar{" +
                "avatarId=" + avatarId +
                ", avatarName='" + avatarName + '\'' +
                ", avatarFile='" + avatarFile + '\'' +
                '}';
    }
}
