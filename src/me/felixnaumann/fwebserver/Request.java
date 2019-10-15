package me.felixnaumann.fwebserver;

public class Request {
    public String reqid;
    public ClientHeader header;
    public String[] GET;
    public String[] POST;

    public Request(String reqid, ClientHeader header) {
        this.reqid = reqid;
        this.header = header;
    }
}
