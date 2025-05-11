package it.polimi.tiw.Servlets;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.OfferteDAOImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AddOffertaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private OfferteDAOImpl offerteDAO;

    @Override
    public void init() throws ServletException {
        offerteDAO = new OfferteDAOImpl();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
      //Verifica sessione
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String username = (String) session.getAttribute("username");

        //Recupera idAsta da sessione (impostato da OfferteServlet) 
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

        //Prova il casting del prezzo
        double prezzo;
        try {
            prezzo = Double.parseDouble(prezzoStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Parametro prezzo non valido\"}");
            return;
        }

        // Inserisce l'offerta sul DB
        try (Connection conn = ConnectionManager.getConnection()) {
            offerteDAO.insertNewOfferta(conn, idAsta, username, prezzo);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Errore DB durante l'inserimento dell'offerta\"}");
            return;
        }

        //Risposta di successo (JS controlla solo post.ok)
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":\"success\"}");
    }

}
