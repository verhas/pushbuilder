package javax0.pushbuilder.demo;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@WebServlet("/image")
public class ImageServer extends HttpServlet {
    static final String PAR_IMGLAG = "imglag";
    static final String PAR_IMGDELAY = "imgdelay";
    static final String PAR_ETAG = "etag";
    private static final LogStore log = LogStore.INSTANCE;
    private Map<String, Set<String>> etags = new HashMap<>();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var etag = req.getParameter(PAR_ETAG);
        var lag = req.getParameter(PAR_IMGLAG);
        if (lag == null) {
            lag = "300";
        }
        var delay = req.getParameter(PAR_IMGDELAY);
        if (delay == null) {
            delay = "1000";
        }
        var png = req.getParameter("png");
        Set<String> pngs;
        if (etags.containsKey(etag)) {
            pngs = etags.get(etag);
        } else {
            pngs = new HashSet<>();
            etags.put(etag, pngs);
        }
        logRequest(req);
        if (pngs.contains(png)) {
            resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            pngs.add(png);
            resp.setContentType("image/png");
            var sleep = ThrottleTool.sleeper();
            sleep.till(Long.parseLong(lag));
            var f = new File("../webapps/push/www.flaticon.com/packs/miscellaneous-elements/" + png + ".png");
            var len = f.length();
            long progress = 0;
            var timeTotal = Long.parseLong(delay);
            try (var os = resp.getOutputStream(); var is = new FileInputStream(f)) {
                int ch;
                while ((ch = is.read()) != -1) {
                    os.write(ch);
                    progress++;
                    sleep.till(timeTotal * progress / len);
                }
            }
        }
    }

    private void logRequest(HttpServletRequest req) {
        var png = req.getParameter("png");
        var msg = new StringBuilder("hit for " + png + "\n");
        msg.append(req.getMethod()).append(" ").append(req.getQueryString()).append("\n");
        req.getHeaderNames().asIterator().forEachRemaining(
                hn -> msg.append(hn).append(": ").append(req.getHeader(hn)).append("\n")
        );
        log.debug(msg.toString());
    }

}