package it.polimi.tiw.Servlets;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.OfferteDAOImpl;
import it.polimi.tiw.dao.Beans.Articolo;
import it.polimi.tiw.dao.Beans.Offerta;
import jakarta.servlet.ServletException;
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
        asteDAO     = new AsteDAOImpl();
        gson        = new Gson();  // Assicurati di avere la dipendenza Gson in pom.xml
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 1) Controllo sessione
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 2) Lettura e validazione parametro idAsta
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

        // 3) Recupero dati dal DB
        try (Connection conn = ConnectionManager.getConnection()) {
            List<Articolo> articoli = articoliDAO.getArticoliByIdAsta(conn, idAsta);
            List<Offerta>  offerte  = offerteDAO.getOfferteByIdAsta(conn, idAsta);

            // Rialzo minimo e prezzo attuale
            double rialzoMinimo  = asteDAO.getRialzoMinimo(conn, idAsta);
            Map<Double, Double> prezziInfo = asteDAO.getPrezzoOffertaMaxANDRialzoMinimo(conn, idAsta);
            double prezzoAttuale = prezziInfo.getOrDefault(rialzoMinimo, 0.0);

            // 4) Costruzione mappa e serializzazione JSON
            Map<String, Object> result = new HashMap<>();
            result.put("articoli",       articoli);
            result.put("offerte",        offerte);
            result.put("rialzo_minimo",  rialzoMinimo);
            result.put("prezzo_attuale", prezzoAttuale);

            String json = gson.toJson(result);

            // 5) Risposta al client js tramite json
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

        } catch (SQLException e) {
            throw new ServletException("Errore in lettura dati offerte", e);
        }
    }
}
