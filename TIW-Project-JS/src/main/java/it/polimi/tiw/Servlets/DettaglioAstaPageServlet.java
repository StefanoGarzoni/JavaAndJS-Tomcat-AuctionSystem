package it.polimi.tiw.Servlets;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.OfferteDAOImpl;
import it.polimi.tiw.dao.Beans.Asta;
import it.polimi.tiw.dao.Beans.Offerta;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class DettaglioAstaPageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private OfferteDAOImpl offerteDAO;
    private AsteDAOImpl asteDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        offerteDAO = new OfferteDAOImpl();
        asteDAO = new AsteDAOImpl();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        // Verifica sessione e login
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("{\"error\":\"Parametro username mancante in sessione o sessioni assenti\"}");
            return;
        }

        //Lettura e parsing di idAsta
        String idAstaParam = request.getParameter("idAsta");
        if (idAstaParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Parametro idAsta mancante in sessione \"}");
            return;
        }

        int idAsta;
        //Provo il casting
        try {
            idAsta = Integer.parseInt(idAstaParam);
            session.setAttribute("idAsta", idAsta);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"errore nel parsing\"}");
            return;
        }

        String username = (String) session.getAttribute("username");
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = ConnectionManager.getConnection()) {
            //Controllo che l'utente sia creatore dell'asta
            if (!asteDAO.checkCreatorOfAsta(conn, username, idAsta)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            //Verifico se l'asta è aperta
            Asta openAsta = asteDAO.getOpenAstaById(conn, idAsta);
            if (openAsta != null) {
                // Asta aperta: invio dati asta + offerte
                boolean canBeClosed = asteDAO.astaCanBeClosed(conn, idAsta);
                List<Offerta> offerte = offerteDAO.getOfferteInOpenAsta(conn, idAsta);

                result.put("openAsta", openAsta);
                result.put("canBeClosed",canBeClosed);
                result.put("offerte", offerte);
            } else {
                // Asta chiusa: invio dati asta chiusa + info acquirente
                Map<Asta, ArrayList<String>> closedInfo = asteDAO.getInfoFromAClosedAsta(conn, idAsta);
                Asta astaChiusa = closedInfo.keySet().iterator().next();
                ArrayList<String> info = closedInfo.get(astaChiusa);

                result.put("astaChiusa", astaChiusa);
                result.put("nomeAcquirente", info.get(0));
                result.put("prezzo", info.get(1));
                result.put("indirizzo", info.get(2));
            }

            //Serializzo e invio JSON
            String json = gson.toJson(result);
            response.setContentType("application/json");
            //response.setCharacterEncoding("UTF-8");
            response.getWriter().print(json);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"errore nella parte di comunicazione con il db :"+e+"\"}");   
            return;
        }
    }
}
