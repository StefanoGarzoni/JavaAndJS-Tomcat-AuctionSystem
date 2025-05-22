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
import jakarta.servlet.http.*;

public class VendoHomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Gson gson = new Gson();
	AsteDAO asteDAO = new AsteDAOImpl();
	ArticoliDAO articoliDAO = new ArticoliDAOImpl();

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
		
		String username = (String) session.getAttribute("username");
		LocalDateTime lastLoginTimestamp = (LocalDateTime) session.getAttribute("lastLoginTimestamp");
		
		// in base ai flag presenti nei cookie, carico dal db e invio al client solo le sezioni necessarie
		Cookie[] cookies = request.getCookies();
		
		Cookie renderAllAsteCookie = null;
		Cookie renderOpenAsteCookie = null;
		Cookie renderArticoliCookie = null;
		
		// estraggo i cookie che mi servono
		for (Cookie cookie : cookies) {
			if(cookie.getName().equals("renderAllTablesAste"+username))
				renderAllAsteCookie = cookie;
			else if(cookie.getName().equals("renderTableAsteAperte"+username)) {
				renderOpenAsteCookie = cookie;
			}
			else if(cookie.getName().equals("renderArticoli"+username)) {
				renderArticoliCookie = cookie;
			}
		}
		
		// creo l'oggetto json che conterrà le tre liste di elementi
		JsonObject finalObject = new JsonObject();
		
		try(Connection conn = ConnectionManager.getConnection()){
			// imposto a true quelli non presenti (dovrò renderizzare le sezioni)
			if(renderAllAsteCookie == null || ( renderAllAsteCookie.getName().equals("renderAllTablesAste"+username) && renderAllAsteCookie.getValue().equals("true") )) {
				// aggiungo aste aperte
				ArrayList<Asta> openAste = asteDAO.getAllOpenAsteInfoByCreator(conn, username);
				for(Asta asta : openAste)
					setTempoRimanenteInAsta(asta, lastLoginTimestamp);
				JsonArray jsonArrayOpenAste = gson.toJsonTree(openAste).getAsJsonArray();
				finalObject.add("openAste", jsonArrayOpenAste);
				
				// aggiungo aste chiuse
				ArrayList<Asta> closedAste = asteDAO.getAllClosedAsteInfoByCreator(conn, username);
				JsonArray jsonArrayClosedAste = gson.toJsonTree(closedAste).getAsJsonArray();
				finalObject.add("closedAste", jsonArrayClosedAste);
				
				// imposto a false il cookie => se non avvengono modifiche tra una visualizzazione di vendo e l'altra, 
				// questa sezione non andrà richiesta
				setCookie(response, "renderAllTablesAste"+username, "false", 30);
			}
			
			if(renderOpenAsteCookie == null || (renderOpenAsteCookie.getName().equals("renderTableAsteAperte"+username) && renderOpenAsteCookie.getValue().equals("true"))) {
				// aggiungo aste chiuse
				ArrayList<Asta> closedAste = asteDAO.getAllClosedAsteInfoByCreator(conn, username);
				JsonArray jsonArrayClosedAste = gson.toJsonTree(closedAste).getAsJsonArray();
				finalObject.add("closedAste", jsonArrayClosedAste);
				
				setCookie(response, "renderTableAsteAperte"+username, "false", 30);
			}
			
			if(renderArticoliCookie == null || (renderArticoliCookie.getName().equals("renderArticoli"+username) && renderArticoliCookie.getValue().equals("true"))) {
				// aggiungo gli articoli
				ArrayList<Articolo> availableArticoli = articoliDAO.getMyArticoli(conn, username);
				JsonArray jsonArrayArticoli = gson.toJsonTree(availableArticoli).getAsJsonArray();
				finalObject.add("articoli", jsonArrayArticoli);
				
				setCookie(response, "renderArticoli"+username, "false", 30);
			}
			
			// Converti in stringa JSON
			String finalJson = gson.toJson(finalObject);
			
			// scrivo il json nella response
			PrintWriter out = response.getWriter();
			out.print(finalJson);

		}
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\""+e+"\"}");
            e.printStackTrace(System.out);
		}
	}

	private void setCookie(HttpServletResponse response, String name, String value, int days) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(days*60*60*24);
        response.addCookie(cookie);
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