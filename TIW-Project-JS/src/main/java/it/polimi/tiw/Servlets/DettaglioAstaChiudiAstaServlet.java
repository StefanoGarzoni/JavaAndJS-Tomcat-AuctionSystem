package it.polimi.tiw.Servlets;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.AsteDAOImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class DettaglioAstaChiudiAstaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AsteDAOImpl asteDAO;
    Gson gson;

    @Override
    public void init() throws ServletException {
        asteDAO = new AsteDAOImpl();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        //Verifica sessione
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //Recupero idAsta e username da sessione
        Integer idAsta = (Integer) session.getAttribute("idAsta");
        String  username = (String)  session.getAttribute("username");
        if (idAsta == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Parametro idAsta mancante\"}");
            return;
        }

        try (Connection conn = ConnectionManager.getConnection()) {
            //Verifica che l'utente sia creatore
            if (!asteDAO.checkCreatorOfAsta(conn, username, idAsta)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"Non autorizzato a chiudere l'asta\"}");
                return;
            }
            // Verifica che l'asta possa essere chiusa
            if (!asteDAO.astaCanBeClosed(conn, idAsta)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Asta non chiudibile\"}");
                return;
            }
            //Chiudo l'asta
            asteDAO.setAstaAsClosed(conn, idAsta, username);

            //set del valore del cookie "lastAction"
            boolean lastActionCookieFound = false;
            boolean tablesAsteCookieFound = false;
            boolean asteLastVisited = false;
            Cookie render = null;
            Cookie listaAste = null;
            Cookie[] cookies = request.getCookies();

            if (cookies != null) {
                for (Cookie c : cookies) {
                    if (c.getName().equals("lastAction")) {
                        c.setValue("closedAsta");
                        lastActionCookieFound = true;
                        response.addCookie(c);
                    }
                    else if (c.getName().equals("renderAllTablesAste")) {
                        c.setValue("true");
                        tablesAsteCookieFound = true;
                        response.addCookie(c);
                    }
                    else if(c.getName().equals("asteVisionate")){
                        listaAste = c;
                        String json = URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8.name());
                        List<Integer> visits = new Gson().fromJson(json, new TypeToken<List<Integer>>() {}.getType());
                        if (visits.contains(idAsta)) {
                            visits.remove(idAsta);
                            c.setValue(URLEncoder.encode(gson.toJson(visits), StandardCharsets.UTF_8.name()));
                            asteLastVisited = true;
                        }
                        
                    }
                    else if(c.getName().equals("renderTableAsteVisionate")){
                        render = c;
                    }

                    if (lastActionCookieFound && tablesAsteCookieFound && asteLastVisited) {
                        break;
                    }

                }
            }
            if(!lastActionCookieFound) {
                Cookie lastActionCookie = new Cookie("lastAction", "closedAsta");
                lastActionCookie.setMaxAge(60*60*24);
                response.addCookie(lastActionCookie);
            }
            if(!tablesAsteCookieFound) {
                Cookie tableOpenAsteCookie = new Cookie("renderAllTablesAste", "true");
                tableOpenAsteCookie.setMaxAge(60*60*24);
                response.addCookie(tableOpenAsteCookie);
            }
            if(asteLastVisited){
                render.setValue(URLEncoder.encode("true", StandardCharsets.UTF_8.name()));
                listaAste.setMaxAge( 30 * 24 * 60 * 60); //un mese
                listaAste.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                render.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                render.setMaxAge( 30 * 24 * 60 * 60);

                response.addCookie(listaAste);
                response.addCookie(render);
            }

        } catch (SQLException e) {
            throw new ServletException("Errore DB chiusura asta", e);
        }
    }
}
