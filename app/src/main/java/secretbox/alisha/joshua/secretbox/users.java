package secretbox.alisha.joshua.secretbox;

public class users {

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setThumb_img(String thumb_img) {
        this.thumb_img = thumb_img;
    }

    public String name;
    public String image;
    public String status;
    public String thumb_img;

    public users() {

    }

    public users(String name, String image, String status) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_img = thumb_img;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getImage() {
        return image;
    }

    public String getThumb_img() {
        return thumb_img;
    }
}
