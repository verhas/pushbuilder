package javax0.pushbuilder.demo;

import javax.servlet.http.HttpServletRequest;

class RequestParser {
    private final HttpServletRequest req;

    private RequestParser(HttpServletRequest req) {
        this.req = req;
    }

    static RequestParser parse(HttpServletRequest req){
        return new RequestParser(req);
    }

    long get(String name, long defaultValue){
        var s = req.getParameter(name);
        if( s == null ){
            return defaultValue;
        }
        return Long.parseLong(s);
    }
}
