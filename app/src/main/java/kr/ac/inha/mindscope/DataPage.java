package kr.ac.inha.mindscope;

public class DataPage {
    int color;
    String title;
    String contents;

    public DataPage(String title, String contents, int color) {
        this.title = title;
        this.contents = contents;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}

