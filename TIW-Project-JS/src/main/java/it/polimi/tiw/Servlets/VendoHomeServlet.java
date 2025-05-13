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
		
		// in base ai flag presenti nei cookie, carico dal db e invio al client solo le sezioni necessarie
		Cookie[] cookies = request.getCookies();
		
		setUnsetCookies(cookies, response);
		
		// creo l'oggetto json che conterrà le tre liste di elementi
		JsonObject finalObject = new JsonObject();
		
		try(Connection conn = ConnectionManager.getConnection()){
			
			for (Cookie cookie : cookies) {
				
				if(cookie.getName().equals("renderAllTablesAste") && cookie.getValue().equals("true") ) {
					// aggiungo aste aperte
					ArrayList<Asta> openAste = asteDAO.getAllOpenAsteInfoByCreator(conn, username);
					for(Asta asta : openAste)
						AsteDAOImpl.setTempoRimanenteInAsta(asta, lastLoginTimestamp);
					JsonArray jsonArrayOpenAste = gson.toJsonTree(openAste).getAsJsonArray();
					finalObject.add("openAste", jsonArrayOpenAste);
					
					// aggiungo aste chiuse
					ArrayList<Asta> closedAste = asteDAO.getAllClosedAsteInfoByCreator(conn, username);
					JsonArray jsonArrayClosedAste = gson.toJsonTree(closedAste).getAsJsonArray();
					finalObject.add("closedAste", jsonArrayClosedAste);
					
					// imposto a false il cookie => se non avvengono modifiche tra una visualizzazione di vendo e l'altra, 
					// questa sezione non andrà richiesta
					setCookie(response, "renderAllTablesAste", "false", 30);
				}
				else if(cookie.getName().equals("renderTableAsteAperte") && cookie.getValue().equals("true")) {
					// aggiungo aste chiuse
					ArrayList<Asta> closedAste = asteDAO.getAllClosedAsteInfoByCreator(conn, username);
					JsonArray jsonArrayClosedAste = gson.toJsonTree(closedAste).getAsJsonArray();
					finalObject.add("closedAste", jsonArrayClosedAste);
					
					setCookie(response, "renderTableAsteAperte", "false", 30);
				}
				else if(cookie.getName().equals("renderArticoli") && cookie.getValue().equals("true")) {
					// aggiungo gli articoli
					ArrayList<Articolo> availableArticoli = articoliDAO.getMyArticoli(conn, username);
					JsonArray jsonArrayArticoli = gson.toJsonTree(availableArticoli).getAsJsonArray();
					finalObject.add("articoli", jsonArrayArticoli);
					
					setCookie(response, "renderArticoli", "false", 30);
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
	
	private void setUnsetCookies(Cookie[] cookies, HttpServletResponse response) {
		boolean renderAllAsteCookieFound = false;
		boolean renderOpenAsteCookieFound = false;
		boolean renderArticoliCookieFound = false;
		
		// controllo che tutti i cookie siano impostati
		for (Cookie cookie : cookies) {
			if(cookie.getName().equals("renderAllTablesAste"))
				renderAllAsteCookieFound = true;
			else if(cookie.getName().equals("renderAllTablesAste")) {
				renderOpenAsteCookieFound = true;
			}
			else if(cookie.getName().equals("renderArticoli")) {
				renderArticoliCookieFound = true;
			}
		}
		
		// imposto a true quelli non presenti (dovrò renderizzare le sezioni)
		if(!renderAllAsteCookieFound) {
			setCookie(response, "renderAllTablesAste", "true", 30);
		}
		if(!renderOpenAsteCookieFound) {
			setCookie(response, "renderTableAsteAperte", "true", 30);
		}
		if(!renderArticoliCookieFound) {
			setCookie(response, "renderArticoli", "true", 30);
		}
	}

	private void setCookie(HttpServletResponse response, String name, String value, int days) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(days*60*60*24);
        response.addCookie(cookie);
	}
}