package DeepLearning;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.omg.CORBA.portable.OutputStream;

public class PgnGamesDownloader {

	private static Logger logger = Logger.getLogger(PgnGamesDownloader.class);

	public static final Pattern PATTERN_DOWNLOAD_LINKS = Pattern.compile(
			"<\\s*a\\s*href\\s*=\\s*\"([^\"]*?\\.(zip|pgn))\"\\s*class\\s*=\\s*\"view\"\\s*>\\s*Download\\s*</a>");

	private File saveDir;
	private File currentSaveFile;
	private LinkedList<String> downloadLinks = new LinkedList<String>();

	public void downloadPgnGames() throws Exception {
		String strPgnMentorWebsite = readPgnMentorWebsite();
		logger.debug(strPgnMentorWebsite);

		saveDir = createDirectoryForPgnGames();

		findOutAllDownloadLinks(strPgnMentorWebsite);
		downloadAll();
	}

	private void downloadAll() {
		for (String downloadLink : downloadLinks) {
			download(downloadLink);
		}

	}

	private void download(String downloadLink) {
		logger.debug("Downloading " + downloadLink);
		setSaveFile(downloadLink);
		BufferedOutputStream out = null;
		InputStream in = null;
		try {
			in = getInputStreamFromDownloadLink(downloadLink);
			out = new BufferedOutputStream(new FileOutputStream(currentSaveFile));
			IOUtils.copy(in, out);
		} catch (IOException ioe) {
			logger.error(ExceptionUtils.getStackTrace(ioe));
		} finally {
			try {
				if(out != null) out.close();
				if(in != null) in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private InputStream getInputStreamFromDownloadLink(String downloadLink) throws IOException
	{
		URL url = new URL("http://www.pgnmentor.com/" + downloadLink);
		if(downloadLink.endsWith(".zip") )
		{
			ZipInputStream zin = new ZipInputStream(url.openStream());
			zin.getNextEntry();
			return zin;
		}
		else if(downloadLink.endsWith(".pgn"))
		{
			return url.openStream();
		}
		return null;
	}

	private void setSaveFile(String downloadLink) {
		currentSaveFile = saveDir;
		String[] parts = downloadLink.split("/");
		for (int i = 0; i < parts.length - 1; ++i) {
			currentSaveFile = new File(currentSaveFile, parts[i]);
			if (!currentSaveFile.exists() && !currentSaveFile.mkdir()) {
				logger.error("The directory of " + currentSaveFile.getAbsolutePath() + " could not be created!");
				continue;
			}
		}

		currentSaveFile = new File(currentSaveFile, parts[parts.length - 1].replaceFirst("\\.zip$", ".pgn"));
	}

	private File createDirectoryForPgnGames() {
		File saveDir = new File("PgnGames");
		if (!saveDir.exists() && !saveDir.mkdir()) {
			System.out.println("Could not create directory " + saveDir.getName());
			System.exit(1);

		}
		return saveDir;
	}

	private String readPgnMentorWebsite() throws Exception {
		URL url = new URL("http://www.pgnmentor.com/files.html");

		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuilder sb = new StringBuilder();

		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}

		reader.close();

		return sb.toString();
	}

	private void findOutAllDownloadLinks(String strPgnMentorWebsite) {
		Matcher matcher = PATTERN_DOWNLOAD_LINKS.matcher(strPgnMentorWebsite);

		while (matcher.find()) {
			downloadLinks.add(matcher.group(1));
		}
	}

	public static void main(String[] args) {
		try {

			PgnGamesDownloader downloader = new PgnGamesDownloader();
			downloader.downloadPgnGames();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}
}
