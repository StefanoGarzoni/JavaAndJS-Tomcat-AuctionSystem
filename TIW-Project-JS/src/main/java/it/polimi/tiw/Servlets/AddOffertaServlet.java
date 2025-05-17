package it.polimi.tiw.Servlets;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import com.google.gson.Gson;
import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.OfferteDAOImpl;
import it.polimi.tiw.dao.Beans.Offerta;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.MultipartConfig;


@MultipartConfig(
	    fileSizeThreshold = 1024 * 1024 * 100,      // 100MB in RAM
	    maxFileSize = 1024 * 1024 * 100,       // 100MB per file
	    maxRequestSize = 1024 * 1024 * 500     // 500MB in totale
	)

public class AddOffertaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private OfferteDAOImpl offerteDAO;
    Gson gson;

    @Override
    public void init() throws ServletException {
        offerteDAO = new OfferteDAOImpl();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Verifica sessione
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String username = (String) session.getAttribute("username");

        // Recupera idAsta da sessione
        Integer idAsta = (Integer) session.getAttribute("idAsta");
        if (idAsta == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Parametro idAsta mancante in sessione\"}");
            return;
        }

        // Legge il prezzo della richiesta
        String prezzoStr = request.getParameter("prezzo");
        if (prezzoStr == null || prezzoStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Parametro prezzo mancante\"}");
            return;
        }

        double prezzo;
        try {
            prezzo = Double.parseDouble(prezzoStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Parametro prezzo non valido\"}");
            return;
        }

        // Inserisce l'offerta sul DB e ottiene data e ora in una mappa
        int result;
        Date data;
        Time ora;
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            data = new Date(System.currentTimeMillis());
            ora = new Time(System.currentTimeMillis());
            result = offerteDAO.insertNewOfferta(conn, idAsta, username, prezzo, data, ora);
            if (result == -1) {
                //se l'inserimento non va a buon fine, si esegue il rollback e si comunica l'errore
                conn.rollback();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Inserimento offerta fallito.");
                return;
            }
            conn.commit();
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Errore DB durante l'inserimento dell'offerta\"}");
            return;
        }

        Offerta newOfferta = new Offerta(result, username, idAsta, prezzo, data, ora);

        //set del valore del cookie "lastAction"
        boolean lastActionCookieFound = false;
        boolean tableOpenAsteCookieFound = false;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("lastAction")) {
                    c.setValue("addedOfferta");
                    lastActionCookieFound = true;
                    response.addCookie(c);
                }
                else if (c.getName().equals("renderTableAsteAperte")) {
                    c.setValue("true");
                    tableOpenAsteCookieFound = true;
                    response.addCookie(c);
                }
                if (lastActionCookieFound && tableOpenAsteCookieFound) {
                    break;
                }
            }
        }
        
        if(!lastActionCookieFound) {
            Cookie lastActionCookie = new Cookie("lastAction", "addedOfferta");
            lastActionCookie.setMaxAge(60*60*24*30);
            response.addCookie(lastActionCookie);
        }
        if(!tableOpenAsteCookieFound) {
            Cookie tableOpenAsteCookie = new Cookie("renderTableAsteAperte", "true");
            tableOpenAsteCookie.setMaxAge(60*60*24*30);
            response.addCookie(tableOpenAsteCookie);
        }

        String jsonString = gson.toJson(newOfferta);
        response.getWriter().print(jsonString);
    }
}
