package it.polimi.tiw.Servlets;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.OfferteDAOImpl;
import it.polimi.tiw.dao.Beans.Articolo;
import it.polimi.tiw.dao.Beans.Offerta;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class OfferteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private OfferteDAOImpl offerteDAO;
    private ArticoliDAOImpl articoliDAO;
    private AsteDAOImpl asteDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        offerteDAO  = new OfferteDAOImpl();
        articoliDAO = new ArticoliDAOImpl();
        asteDAO = new AsteDAOImpl();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //Lettura e validazione parametro idAsta
        String idParam = request.getParameter("idAsta");
        if (idParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Parametro idAsta mancante\"}");
            return;
        }

        int idAsta;
        try {
            idAsta = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"idAsta non valido\"}");
            return;
        }

        //controllo cookie per lista aste già visitate
        int oneWeek = 30 * 24 * 60 * 60; //un mese
        List<Integer> visits = new ArrayList<>();
        boolean renderTableAsteVisionateFound = false;
        boolean listaAsteFound = false;

        //cerco se cookie esiste
        Cookie[] cookies = request.getCookies();
        Cookie listaAste = null;
        Cookie renderTableAsteVisionate = null;

        if (cookies != null) {
            for (Cookie c : cookies) {

                if (c.getName().equals("asteLastVisited")) {
                    listaAste = c;
                    String json = URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8.name());
                    visits = new Gson().fromJson(json, new TypeToken<List<Integer>>() {}.getType());
                    listaAsteFound = true;

                } else if(c.getName().equals("renderTableAsteVisionate")){
                    renderTableAsteVisionateFound = true;
                    //se c'è lo salvo in una variabile in modo che poi nel caso posso modificarlo
                    renderTableAsteVisionate = c;
                    renderTableAsteVisionate.setValue(URLEncoder.encode("false", StandardCharsets.UTF_8.name()));
                }

                if(listaAsteFound && renderTableAsteVisionateFound){
                    break;
                }
            }
        }

        if(!renderTableAsteVisionateFound){
            renderTableAsteVisionate = new Cookie("renderTableAsteVisionate", URLEncoder.encode("false", StandardCharsets.UTF_8.name()));
        }
        
        if(!listaAsteFound){
            String emptyJson = new Gson().toJson(visits); // "[]"
            listaAste = new Cookie("asteLastVisited", URLEncoder.encode(emptyJson, StandardCharsets.UTF_8.name()));
        }

        // prende il parametro idAsta dalla richiesta
        try {
            if (!visits.contains(idAsta)) {
                visits.add(idAsta);
                //aggiorno il cookie e imposto il cookie renderTableAsteVisionate a true
                listaAste.setValue(URLEncoder.encode(gson.toJson(visits), StandardCharsets.UTF_8.name()));

                renderTableAsteVisionate.setValue(URLEncoder.encode("true", StandardCharsets.UTF_8.name()));
            }

            listaAste.setMaxAge(oneWeek);
            listaAste.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
            renderTableAsteVisionate.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
            renderTableAsteVisionate.setMaxAge(oneWeek);

            response.addCookie(listaAste);
            response.addCookie(renderTableAsteVisionate);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"problemi con i cookies\"}");
            return;
        }
        

        //Recupero dati dal DB
        try (Connection conn = ConnectionManager.getConnection()) {
            List<Articolo> articoli = articoliDAO.getArticoliByIdAsta(conn, idAsta);
            List<Offerta>  offerte  = offerteDAO.getOfferteByIdAsta(conn, idAsta);

            // Rialzo minimo e prezzo attuale
            double rialzoMinimo  = asteDAO.getRialzoMinimo(conn, idAsta);
            Map<Double, Double> prezziInfo = asteDAO.getPrezzoOffertaMaxANDRialzoMinimo(conn, idAsta);
            double prezzoAttuale = prezziInfo.getOrDefault(rialzoMinimo, 0.0);

            // Costruzione mappa e serializzazione JSON
            Map<String, Object> result = new HashMap<>();
            result.put("articoli",       articoli);
            result.put("offerte",        offerte);
            result.put("rialzo_minimo",  rialzoMinimo);
            result.put("prezzo_attuale", prezzoAttuale);

            String json = gson.toJson(result);

            //Risposta al client js tramite json
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

        } catch (SQLException e) {
            throw new ServletException("Errore in lettura dati offerte", e);
        }
    }
}
