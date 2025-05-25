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
	private JakartaServletWebApplication webApplication ;
	private WebApplicationTemplateResolver templateResolver;
	private ArticoliDAOImpl articoloDAO;

	public void init() throws ServletException {
		articoloDAO = new ArticoliDAOImpl();

		webApplication= JakartaServletWebApplication.buildApplication(getServletContext());
		templateResolver = new WebApplicationTemplateResolver(webApplication);
		
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
		
		// estrazione e sanificazione input
		articleName = request.getParameter("nome");
		articleDescription = request.getParameter("descrizione");
		articlePriceString = request.getParameter("prezzo");
		
		imageName = saveImage(request);
			
		if(articleName == null || articleDescription == null || imageName == null || articlePriceString == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		try {
			articlePrice = Integer.parseInt(articlePriceString);
		}
		catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The selected price is not a number");
			return;
		}
		
		if(articlePrice < 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Negative price");
			return;
		}
		
		// eseguire la query
		String username = (String) request.getSession().getAttribute("username");
		try ( Connection conn = ConnectionManager.getConnection() ) {
			articoloDAO.insertNewArticolo(conn, username, articleName, articleDescription, imageName, articlePrice);
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
		
		String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();	// estrazione del nome del file dell'immagine
		String extension = "";
		
		int i = fileName.lastIndexOf('.');
		if (i > 0) extension = fileName.substring(i); 	// estrae l'estensione del file
		String uniqueName = UUID.randomUUID().toString() + extension;		// genera un nome casuale per salvare il file (per evitare più file con lo stesso nome)
		
		String uploadPath = getServletContext().getInitParameter("articlesImagesUploadPath");

        // scrive l'immagine nel file
        filePart.write(uploadPath + File.separator + uniqueName);
	
        return uniqueName;
	}

}
