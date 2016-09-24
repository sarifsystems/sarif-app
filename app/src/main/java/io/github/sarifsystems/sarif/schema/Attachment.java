package io.github.sarifsystems.sarif.schema;

/**
 * Created by me on 9/7/2016.
 */
public class Attachment {

    public String fallback;
    public String color;
    public String pretext;
    public String authorName;
    public String authorLink;
    public String title;
    public String titleLink;
    public String text;
    public String imageUrl;
    public String thumbUrl;
    public String footer;
    public String footerIcon;
    public int ts;

    public String authorMarkup() {
        String name = authorName;
        if (name != null && authorLink != null && !authorLink.isEmpty()) {
            name = "<a href=\"" + authorLink + "\">" + name + "</a>";
        }
        return name;
    }

    public String titleMarkup() {
        String name = title;
        if (name != null && titleLink != null && !titleLink.isEmpty()) {
            name = "<a href=\"" + titleLink + "\">" + name + "</a>";
        }
        return name;
    }
}
