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

//serve per gestire l'upload di contenuti tramite form (js formData)
//Questa annotazione indica al container Java (come Tomcat) che la servlet deve 
//gestire richieste multipart (tipiche di upload file o form complessi)
//NOTA: in verità la pagina offerta js non manda file, ma è necessario avere questa annotazione dato che 
// l'oggetto formData js modifica automaticamente il tipo di richiesta in multipart/form-data (protocollo)
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
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String username = (String) session.getAttribute("username");

        // Recupera idAsta da sessione
        Integer idAsta = (Integer) session.getAttribute("idAsta");
        if (idAsta == null) {
            //setta lo stato della risposta HTTP
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            //scrive il messaggio di errore in formato JSON all'interno del body della risposta
            response.getWriter().print("{\"error\":\"Parametro idAsta mancante in sessione\"}");
            return;
        }

        // Legge il prezzo della richiesta
        String prezzoStr = request.getParameter("prezzo");
        if (prezzoStr == null || prezzoStr.isEmpty()) {
            //setta lo stato della risposta HTTP
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            //scrive il messaggio di errore in formato JSON all'interno del body della risposta
            response.getWriter().print("{\"error\":\"Parametro prezzo mancante\"}");
            return;
        }

        double prezzo;
        try {
            prezzo = Double.parseDouble(prezzoStr);
        } catch (NumberFormatException e) {
            //setta lo stato della risposta HTTP
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            //scrive il messaggio di errore in formato JSON all'interno del body della risposta
            response.getWriter().print("{\"error\":\"Parametro prezzo non valido\"}");
            return;
        }

        // Inserisce l'offerta sul DB e ottiene data e ora in una mappa
        int result;
        Date data;
        Time ora;
        try (Connection conn = ConnectionManager.getConnection()) {

            conn.setAutoCommit(false);

            // mando anche la data e il time come parametri dato che mi servono poi per passarli al js e aggiungere l'offerta 
            // senza rifare il render della pagina
            data = new Date(System.currentTimeMillis());
            ora = new Time(System.currentTimeMillis());
            result = offerteDAO.insertNewOfferta(conn, idAsta, username, prezzo, data, ora);
            if (result == -1) {
                //se l'inserimento non va a buon fine, si esegue il rollback e si comunica l'errore
                conn.rollback();
                //setta lo stato della risposta HTTP
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                //scrive il messaggio di errore in formato JSON all'interno del body della risposta
                response.getWriter().print("{\"error\":\"Errore DB durante l'inserimento dell'offerta\"}");
                return;
            }
            conn.commit();
            conn.setAutoCommit(true);

        } catch (SQLException e) {
            //setta lo stato della risposta HTTP
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            //scrive il messaggio di errore in formato JSON all'interno del body della risposta
            response.getWriter().print("{\"error\":\"Errore DB durante l'inserimento dell'offerta\"}");
            return;
        }

        // crea l'offerta (oggetto) che va restituito al client
        Offerta newOfferta = new Offerta(result, username, idAsta, prezzo, data, ora);

        //set del valore del cookie "lastAction"
        boolean lastActionCookieFound = false;
        Cookie[] cookies = request.getCookies();

        //cerco il cookie tra i cookies
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("lastAction")) {

                    //se lo trovo, modifico il suo valore
                    c.setMaxAge(60*60*24*30); // rinnovo la scadenza di un mese
                    c.setValue("addedOfferta");
                    lastActionCookieFound = true;
                    response.addCookie(c);
                    break;
                }
            }
        }
        
        //se il cookie non esiste, lo creo
        if(!lastActionCookieFound) {
            Cookie lastActionCookie = new Cookie("lastAction", "addedOfferta");
            lastActionCookie.setMaxAge(60*60*24*30); //un mese
            response.addCookie(lastActionCookie);
        }

        //serializzo l'oggetto offerta in json e poi lo metto nella body della risposta
        String jsonString = gson.toJson(newOfferta);
        response.getWriter().print(jsonString);
    }
}
