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
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
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
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Some articles identifiers are not number");
			return;
		}
		
		// controllo validità rialzo minimo
		float rialzoMinimo;
		try {
			rialzoMinimo = Float.parseFloat(rialzoMinimoString);
			if(rialzoMinimo <= 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Negative or zero rialzo minimo");
				return;
			}
		}
		catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Rialzo minimo is not a number");
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
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "La data di scadenza scelta è nel passato");
				return;
			}
		}
		catch (DateTimeParseException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Data o ora di scadenza non sono date");
			return;
		}
		
		if(selectedArticlesIds.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Non è possibile creare un'asta senza articoli");
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
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You can select only your own articles");
				return;
			}
			
			// controllo che gli articoli che si vogliono inserire non siano già in un'altra asta
			if(!articoliDAO.areAllArticlesFree(conn, selectedArticlesIds)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You can select only articles that are not in an asta yet");
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
				
				// imposto il cookie con la nuova azione
				Cookie lastAction = new Cookie("lastAction", "addedAsta");
				lastAction.setMaxAge(30*60*60*24);	// scadenza a 30gg
	            response.addCookie(lastAction);
			    
				response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    
			    String finalJson = gson.toJson(newAsta);
			   
			    // scrivo il json nella response
			    PrintWriter out = response.getWriter();
			    out.print(finalJson);
			    out.flush();
			}
			catch (SQLException e) {
				conn.rollback();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno al server durante l'inserimento");
			}
			finally {
				conn.setAutoCommit(true);
			}
		}
		catch (SQLException e) {
			e.printStackTrace(System.out);	// stampo su terminale l'errore SQL
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno al server durante l'inserimento");
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
