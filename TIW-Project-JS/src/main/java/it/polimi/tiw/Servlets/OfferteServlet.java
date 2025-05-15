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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.OfferteDAOImpl;
import it.polimi.tiw.dao.Beans.Articolo;
import it.polimi.tiw.dao.Beans.Asta;
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
        int oneMonth = 30 * 24 * 60 * 60; //un mese
        JsonArray asteVisionateJsonArray = null;

        //cerco se cookie esiste
        Cookie[] cookies = request.getCookies();

        // cerco il cookie "asteVisionate"
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("asteVisionate")) {
                	// dal cookie estraggo un JsonArray contente gli id delle aste
                    String decodedAsteVisionateJsonCookie = URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8);
                    asteVisionateJsonArray = JsonParser.parseString(decodedAsteVisionateJsonCookie).getAsJsonArray();
                    break;
                }
            }
        }
        
        boolean astaAlreadyVisited = false;
        if(asteVisionateJsonArray != null) {
    		// controllo se l'asta è già stata visionata
	    	for(JsonElement astaVisionata : asteVisionateJsonArray) {
	    		if(astaVisionata.getAsInt() == idAsta)
	    			astaAlreadyVisited = true;
	    	}
    	}
        else {
        	// se il cookie non era già presente, creo un JsonArray vuoto
			asteVisionateJsonArray = new JsonArray();
        }
    	
    	// se non è stata visionata (cookie non esistente o non contiene l'id), la aggiungo
        if (!astaAlreadyVisited) {
        	asteVisionateJsonArray.add(idAsta);
            
        	//creo il cookie aggiornato
        	String encodedAsteVisionate = URLEncoder.encode(gson.toJson(asteVisionateJsonArray), StandardCharsets.UTF_8);
        	
        	Cookie updatedCookie = new Cookie("asteVisionate", encodedAsteVisionate);
            updatedCookie.setMaxAge(oneMonth);
            updatedCookie.setPath(request.getContextPath());
            
            response.addCookie(updatedCookie);
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
