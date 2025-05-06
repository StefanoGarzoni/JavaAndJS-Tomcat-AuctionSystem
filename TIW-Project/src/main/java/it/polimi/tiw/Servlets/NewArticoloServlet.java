package it.polimi.tiw.Servlets;

import java.io.*;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

//Le immagini sono salvate in: eclipse-workspace\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\TIW-Project\WEB-INF\articlesImages\
// da pietro le immagini solo salvate in: C:\Users\pietr\OneDrive - Politecnico di Milano\PoliMi\3anno\2semestre\TIW - progetto\Progetto-TIPIW\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\TIW-Project\WEB-INF\articlesImages
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 100,      // 100MB in RAM
    maxFileSize = 1024 * 1024 * 100,       // 100MB per file
    maxRequestSize = 1024 * 1024 * 500     // 500MB in totale
)
public class NewArticoloServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private TemplateEngine templateEngine;
	
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(servletContext);
		WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
		
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String redirectionPath = "/TIW-Project/vendo";
		
		String articleName;
		String articleDescription;
		String articlePriceString;
		int articlePrice;
		String imageName;
		
		if(request.getSession(false) == null) {	// if a session already exists (the client logged in)
            response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		
		// input sanitise
		try {
			articleName = request.getParameter("nome");
			articleDescription = request.getParameter("descrizione");
			articlePriceString = request.getParameter("prezzo");
			//Part filePart = request.getPart("immagine");
			
			imageName = saveImage(request);
			
			if(articleName == null || articleDescription == null || imageName == null || articlePriceString == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
				return;
			}
			
			articlePrice = Integer.parseInt(articlePriceString);
			
			if(articlePrice < 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Negative price");
				return;
			}
		}
		catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The selected price is not a number");
			return;
		}
		
		// eseguire la query
		try {
			String username = (String) request.getSession().getAttribute("username");
			
			Connection conn = ConnectionManager.getConnection(); 
			new ArticoliDAOImpl().insertNewArticolo(conn, username, articleName, articleDescription, imageName, articlePrice);
		}
		catch (SQLException e) {
			e.printStackTrace(System.out);
		}
		
		response.sendRedirect(redirectionPath);
	}	
	
	private String saveImage(HttpServletRequest request) throws ServletException, IOException {
		Part filePart = request.getPart("immagine");	
		if(filePart == null) {
			return null;
		}
		
		String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
		String ext = "";
		
		int i = fileName.lastIndexOf('.'); //prende la lunghezza del nome
		if (i > 0) ext = fileName.substring(i); //estrae solo il nome e non l'estensione del file
		String uniqueName = UUID.randomUUID().toString() + ext;
		
		String uploadPath = getServletContext().getRealPath("/WEB-INF/articlesImages");	// file separator serve poichè il separatore dipende dal sistema operativo
		
		File target = new File(uploadPath, uniqueName);
		System.out.println("Scrivo l'immagine in: " + target.getAbsolutePath());

        // salva il file
        filePart.write(uploadPath + File.separator + uniqueName);
	
        return fileName;
	}

}
