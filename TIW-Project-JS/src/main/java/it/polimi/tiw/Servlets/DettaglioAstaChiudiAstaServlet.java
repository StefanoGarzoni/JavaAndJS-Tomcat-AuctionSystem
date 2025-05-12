package it.polimi.tiw.Servlets;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.AsteDAOImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class DettaglioAstaChiudiAstaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AsteDAOImpl asteDAO;

    @Override
    public void init() throws ServletException {
        asteDAO = new AsteDAOImpl();
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

        } catch (SQLException e) {
            throw new ServletException("Errore DB chiusura asta", e);
        }
    }
}
