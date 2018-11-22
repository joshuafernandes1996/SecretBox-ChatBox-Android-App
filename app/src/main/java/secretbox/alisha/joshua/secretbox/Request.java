package secretbox.alisha.joshua.secretbox;

public class Request {
    String requesttype;

    public Request(){}

    public Request(String requesttype){
        this.requesttype = requesttype;
    }

    public String getRequesttype(){return requesttype;}
    public void setRequesttype(String ReqType){this.requesttype =ReqType;}
}
