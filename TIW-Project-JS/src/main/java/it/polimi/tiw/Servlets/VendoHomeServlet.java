package it.polimi.tiw.Servlets;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
		
		try(Connection conn = ConnectionManager.getConnection()){
			ArrayList<Asta> openAste = asteDAO.getAllOpenAsteInfoByCreator(conn, username);
			setTempoRimanenteInAste(openAste, lastLoginTimestamp);
			
			ArrayList<Asta> closedAste = asteDAO.getAllClosedAsteInfoByCreator(conn, username);
			
			ArrayList<Articolo> availableArticoli = articoliDAO.getMyArticoli(conn, username);
			
			JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
			WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
			ctx.setVariable("asteAperte", openAste);
			ctx.setVariable("asteChiuse", closedAste);
			ctx.setVariable("availableArticoli", availableArticoli);
			
	        // creo l'oggetto json che contiene le tre liste di elementi
	        JsonArray jsonArrayOpenAste = gson.toJsonTree(openAste).getAsJsonArray();
	        JsonArray jsonArrayClosedAste = gson.toJsonTree(closedAste).getAsJsonArray();
	        JsonArray jsonArrayArticoli = gson.toJsonTree(availableArticoli).getAsJsonArray();

	        // aggiungo le tre liste all'oggetto json finale
	        JsonObject finalObject = new JsonObject();
	        finalObject.add("openAste", jsonArrayOpenAste);
	        finalObject.add("closedAste", jsonArrayClosedAste);
	        finalObject.add("articoli", jsonArrayArticoli);

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
	
	private void setTempoRimanenteInAste(ArrayList<Asta> aste, LocalDateTime lastLoginTimestamp) {
		for(Asta asta : aste) {
			
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
}