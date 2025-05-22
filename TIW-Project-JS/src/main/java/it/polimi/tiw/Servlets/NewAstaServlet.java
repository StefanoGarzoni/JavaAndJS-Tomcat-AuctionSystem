package it.polimi.tiw.Servlets;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.sql.Date;
import com.google.gson.Gson;
import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAO;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import it.polimi.tiw.dao.AsteDAO;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.UtenteDAOImpl;
import it.polimi.tiw.dao.Beans.Articolo;
import it.polimi.tiw.dao.Beans.Asta;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;

@MultipartConfig
public class NewAstaServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Gson gson = new Gson();

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getSession(false) == null) {	// if a session already exists (the client logged in)
            response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		
		// parameters sanitise
		String[] selectedArticlesStrings = request.getParameterValues("codiceArticolo");
		
		String rialzoMinimoString = request.getParameter("rialzoMinimo");
		String dataScadenzaString = request.getParameter("dataScadenza");
		String oraScadenzaString = request.getParameter("oraScadenza");
		
		// controllo presenza di tutti i parametri
		if(selectedArticlesStrings == null || rialzoMinimoString == null || dataScadenzaString == null || oraScadenzaString == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Parametri mancanti\"}");
			return;
		}
		
		// controllo che tutti gli id degli articoli scelti siano interi
		ArrayList<Integer> selectedArticlesIds = new ArrayList<>();	
		try {
			for(String articleIdString: selectedArticlesStrings) {
				int parsedId = Integer.parseInt(articleIdString);
				selectedArticlesIds.add(parsedId);
			}
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Parametri inviati non corretti : errore di parsing\"}");
			return;
		}
		
		// controllo validità rialzo minimo
		float rialzoMinimo;
		try {
			rialzoMinimo = Float.parseFloat(rialzoMinimoString);
			if(rialzoMinimo <= 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            	response.getWriter().print("{\"error\":\"Rialzo minimo negativo o 0\"}");
				return;
			}
		}
		catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Rialzo minimo non corretto : errore di parsing\"}");
			return;
		}
		
		// controllo che la data e ora di scadenza siano nel futuro
		LocalDate data;
		LocalTime ora;
		try {
			data = LocalDate.parse(dataScadenzaString);
			ora = LocalTime.parse(oraScadenzaString);
			
			if(
					data.isBefore(LocalDate.now()) || 
					(data.isEqual(LocalDate.now()) && ora.isBefore(LocalTime.now())))
			{
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            	response.getWriter().print("{\"error\":\"Parametri inviati non corretti : data nel passato\"}");
				return;
			}
		}
		catch (DateTimeParseException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Parametri inviati non corretti : errore di parsing per data o ora\"}");
			return;
		}
		
		if(selectedArticlesIds.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Parametri Articoli mancanti\"}");
			return;
		}
		
		// creazione nuova asta
		String username = (String) request.getSession().getAttribute("username");
		AsteDAO asteDAO = new AsteDAOImpl();
		ArticoliDAO articoliDAO = new ArticoliDAOImpl();
		try {
			Connection conn = ConnectionManager.getConnection();
			
			// controllo che l'utente possegga tutti gli articoli che vengono messi all'asta
			if(!articoliDAO.areAllArticlesOfUser(conn, username, selectedArticlesIds)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Parametri inviati non corretti : gli articoli non sono tuoi\"}");
				return;
			}
			
			// controllo che gli articoli che si vogliono inserire non siano già in un'altra asta
			if(!articoliDAO.areAllArticlesFree(conn, selectedArticlesIds)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            	response.getWriter().print("{\"error\":\"Parametri inviati non corretti : gli articoli non possono già essere in un asta\"}");
				return;
			}
			
			Date dataScadenza = java.sql.Date.valueOf(data);
			Time oraScadenza = java.sql.Time.valueOf(ora);
		
			conn.setAutoCommit(false);
			try {
				int startingPrice = articoliDAO.getSumOfPrice(conn, selectedArticlesIds);
				
				// inserisce un'asta e restituisce l'id generato
				int idNewAsta = asteDAO.insertNewAsta(
						conn,
						username,
						startingPrice, 
						rialzoMinimo, 
						dataScadenza,
						oraScadenza
						);
				
				// aggiornamento degli id_asta negli articoli in essa presenti
				articoliDAO.updateIdAstaInArticles(conn, selectedArticlesIds, idNewAsta);
				
				// estraggo gli articoli dell'asta e creo l'oggetto asta da inviare al client
				ArrayList<Articolo> articoli = new ArticoliDAOImpl().getArticoliByIdAsta(conn, idNewAsta);
	        	
				Asta newAsta = new Asta(idNewAsta, username, startingPrice, rialzoMinimo, dataScadenza, oraScadenza, 0, false, articoli);
	        	setTempoRimanenteInAsta(newAsta, LocalDateTime.now());
				
	        	// eseguo le modifiche sul DB se tutti i campi sono stati aggiornati correttamente senza errori
				conn.commit();
	            
				// imposto l'ultima azione nei cookie
				Cookie lastAction = new Cookie("lastActionAstaCreated", "true");
				lastAction.setMaxAge(60*60*24*30);
				response.addCookie(lastAction);
			    
				response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    
			    String finalJson = gson.toJson(newAsta);
			   
			    // scrivo il json nella response
			    PrintWriter out = response.getWriter();
			    out.print(finalJson);
			}
			catch (SQLException e) {
				conn.rollback();
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().print("{\"error\":\""+e+"\"}");
			}
			finally {
				conn.setAutoCommit(true);
			}
		}
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\""+e+"\"}");
            e.printStackTrace(System.out);
		}
	}

	private void setTempoRimanenteInAsta(Asta asta, LocalDateTime lastLoginTimestamp) {
		// conversion from java.sql.Date and java.sql.Time to LocalDateTime
		LocalDateTime scadenzaAsta = LocalDateTime.of(
				asta.getDataScadenza().toLocalDate(), 
				asta.getOraScadenza().toLocalTime()
		);
		
		long giorniRimanentiAsta = ChronoUnit.DAYS.between(lastLoginTimestamp, scadenzaAsta);	// integer days distance
		LocalDateTime dopoGiorni = lastLoginTimestamp.plusDays(giorniRimanentiAsta);
		long oreRimanentiAsta = Duration.between(dopoGiorni, scadenzaAsta).toHours();	// integer hours distance
		
		asta.setGiorniRimanenti((int)giorniRimanentiAsta);
		asta.setOreRimanenti((int)oreRimanentiAsta);
	}
}
