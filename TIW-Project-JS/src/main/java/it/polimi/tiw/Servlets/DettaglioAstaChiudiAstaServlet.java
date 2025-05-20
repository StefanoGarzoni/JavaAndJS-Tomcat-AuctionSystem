package it.polimi.tiw.Servlets;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import com.google.gson.Gson;
import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.UtenteDAOImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class DettaglioAstaChiudiAstaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AsteDAOImpl asteDAO;
    private UtenteDAOImpl utenteDAO;
    Gson gson;

    @Override
    public void init() throws ServletException {
        asteDAO = new AsteDAOImpl();
        utenteDAO = new UtenteDAOImpl();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        //Verifica sessione
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        //Recupero idAsta e username da sessione
        Integer idAsta = (Integer) session.getAttribute("idAsta");
        String  username = (String)  session.getAttribute("username");
        if (idAsta == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Parametro idAsta mancante\"}");
            return;
        }

        try (Connection conn = ConnectionManager.getConnection()) {
            //Verifica che l'utente sia creatore
            if (!asteDAO.checkCreatorOfAsta(conn, username, idAsta)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().print("{\"error\":\"Non autorizzato a chiudere l'asta\"}");
                return;
            }
            // Verifica che l'asta possa essere chiusa
            if (!asteDAO.astaCanBeClosed(conn, idAsta)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\":\"Asta non chiudibile\"}");
                return;
            }

            //Chiudo l'asta
            asteDAO.setAstaAsClosed(conn, idAsta, username);

            //modifico l'ultima azione dell'utente
            try{
                utenteDAO.setUserLastActionWasAddedAsta(conn, username, false);
            }
            catch(SQLException e) {
                //setta lo stato della risposta HTTP
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                //scrive il messaggio di errore in formato JSON all'interno del body della risposta
                response.getWriter().print("{\"error\":\"Errore DB durante l'aggiornamento dell'ultima azione\"}");
                e.printStackTrace(System.out);
                return;
            }

            //set del valore del cookie "lastAction"
            boolean tablesAsteCookieFound = false;
            Cookie[] cookies = request.getCookies();

            //scorro l'array di cookie
            //cerco i cookie "lastAction" e "renderAllTablesAste"
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if (c.getName().equals("renderAllTablesAste")) {
                        c.setValue("true");
                        c.setMaxAge(60*60*24*30);
                        tablesAsteCookieFound = true;
                        response.addCookie(c);
                        break;
                    }
                }
            }

            //se i cookie non esistono, li creo
            if(!tablesAsteCookieFound) {
                Cookie tableOpenAsteCookie = new Cookie("renderAllTablesAste", "true");
                tableOpenAsteCookie.setMaxAge(60*60*24*30);
                response.addCookie(tableOpenAsteCookie);
            }

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"errore nella parte di comunicazione con il db :"+e+"\"}");   
            e.printStackTrace(System.out);
            return;
        }
    }
}
