package secretbox.alisha.joshua.secretbox;

public class messages {

    private  String message;
    private String type;
    private String from;
    private Long time;
    private boolean seen;


    public messages(String message, boolean seen, Long time, String type){
        this.message=message;
        this.seen=seen;
        this.time=time;
        this.type=type;


    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public messages(){

    }

}
