package sample.data.jpa.conf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@Component
// @WebServlet(urlPatterns = "/audiofiles/*", asyncSupported = true)
public class AudioServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(AudioServlet.class);
	private static final long serialVersionUID = 1L;

	public static String REAL_FILE_DIR = "";

	@Autowired
	private MySettings mySettings;

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		String contextPath = getServletContext().getContextPath();
		try {
			URL url = getServletContext().getResource("/");
			System.out.println("url:" + url);
		}
		catch (MalformedURLException e) {
			throw new UnsupportedOperationException("Auto-generated method stub", e);
		}
		SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
				getServletContext());
	}

	/**
	 * @return the mySettings
	 */
	public MySettings getMySettings() {
		return this.mySettings;
	}

	/**
	 * @param mySettings the mySettings to set
	 */
	public void setMySettings(MySettings mySettings) {
		this.mySettings = mySettings;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String fileUrlPath = request.getRequestURI().replaceFirst(
				request.getContextPath(), "");
		if (fileUrlPath.contains("/audiofiles/realAudio")) {
			REAL_FILE_DIR = this.mySettings.getAudioDir();
		}
		else {
			REAL_FILE_DIR = this.mySettings.getAudioSnippetDir();
		}
		// REAL_FILE_DIR = "C:/zgq/english2/audioDir"; // FIXME
		// REAL_FILE_DIR =
		// "/home/vcap/file/c954809f-84d0-4bba-96bd-fb525fc89cf0/audioDir"; // FIXME
		response.setContentType("audio/mp3");
		// System.out.println("###REAL_FILE_DIR:" + REAL_FILE_DIR);
		// System.out.println("###URI:" + request.getRequestURI());
		// System.out.println("###URL:" + request.getRequestURL());
		// System.out.println("###contextPath:" + request.getContextPath());
		fileUrlPath = fileUrlPath.replace("/audiofiles/realAudio", "");
		fileUrlPath = fileUrlPath.replace("/audiofiles", "");
		String readFilePath = REAL_FILE_DIR + File.separator + fileUrlPath;

		File f = new File(readFilePath);
		logger.info("###REAL_FILE_DIR:" + REAL_FILE_DIR);
		logger.info("###audio file:" + f.getAbsolutePath());
		// BufferedImage bi = ImageIO.read(f);
		OutputStream out = response.getOutputStream();
		// ImageIO.write(bi, "jpg", out);
		IOUtils.write(FileUtils.readFileToByteArray(f), out);
		out.close();
	}
}