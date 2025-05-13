package it.polimi.tiw.Servlets;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAO;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import it.polimi.tiw.dao.AsteDAO;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.Beans.Articolo;
import it.polimi.tiw.dao.Beans.Asta;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

public class VendoHomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Gson gson = new Gson();

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		
		if(session == null) {	// se un utente non è loggato, lo reindirizza al login
            response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		
		// se presente, azzera idAsta precedentemente impostato per mostrare l'asta corretta in DettaglioAsta
		if(session.getAttribute("idAsta") != null) {
			session.removeAttribute("idAsta");
		}
		
		AsteDAO asteDAO = new AsteDAOImpl();
		ArticoliDAO articoliDAO = new ArticoliDAOImpl();
		
		String username = (String) session.getAttribute("username");
		LocalDateTime lastLoginTimestamp = (LocalDateTime) session.getAttribute("lastLoginTimestamp");
		
		// estraggo le informazioni richieste dal client
		String tabelleRichieste = request.getParameter("tabelleRichieste");
		if(tabelleRichieste == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		JsonArray tablesToRetrieve = new JsonArray();
		tablesToRetrieve = JsonParser.parseString(tabelleRichieste).getAsJsonArray();
		
		// creo l'oggetto json che conterrà le tre liste di elementi
		JsonObject finalObject = new JsonObject();
		
		try(Connection conn = ConnectionManager.getConnection()){
			
			for (JsonElement table : tablesToRetrieve) {
				
				// in base alle stringhe richieste dal client, richiedo le informazioni necessarie
				switch (table.getAsString()) {
					case ("asteAperte") : {
						ArrayList<Asta> openAste = asteDAO.getAllOpenAsteInfoByCreator(conn, username);
						
						for(Asta asta : openAste)
							AsteDAOImpl.setTempoRimanenteInAsta(asta, lastLoginTimestamp);
						
						JsonArray jsonArrayOpenAste = gson.toJsonTree(openAste).getAsJsonArray();
						finalObject.add("openAste", jsonArrayOpenAste);
						
						break;						
					}
					
					case ("asteChiuse") : {
						ArrayList<Asta> closedAste = asteDAO.getAllClosedAsteInfoByCreator(conn, username);
						JsonArray jsonArrayClosedAste = gson.toJsonTree(closedAste).getAsJsonArray();
						finalObject.add("closedAste", jsonArrayClosedAste);
						
						break;
					}
					
					case ("articoli") : 
						ArrayList<Articolo> availableArticoli = articoliDAO.getMyArticoli(conn, username);
						JsonArray jsonArrayArticoli = gson.toJsonTree(availableArticoli).getAsJsonArray();
						finalObject.add("articoli", jsonArrayArticoli);
						
						break;
				}
			}			
			
			// Converti in stringa JSON
			String finalJson = gson.toJson(finalObject);
			
			// scrivo il json nella response
			PrintWriter out = response.getWriter();
			out.print(finalJson);
			out.flush();
		}
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno al server durante il recupero delle informazioni");
		}
	}
}