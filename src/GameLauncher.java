import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.unlishema.simpleKeyHandler.SimpleKeyHandler;

import processing.awt.PSurfaceAWT;
import processing.awt.PSurfaceAWT.SmoothCanvas;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PSurface;

public class GameLauncher extends PApplet {
	private final Config config = new Config("launcher.config");
	private final SimpleKeyHandler skh = new SimpleKeyHandler(this);

	private final ArrayList<VersionFile> versionList = new ArrayList<VersionFile>();
	private final UpdateFileList updateList = new UpdateFileList();

	private final Rectangle closeBtn = new Rectangle(363, 8, 27, 27);
	private final ProgressBar progressBar = new ProgressBar(50, 125, 300, 50);
	private final Point loadingInfoPosition = new Point(50, 65);

	private final String versionFile = "versions.json";

	private PImage backgroundImage;
	private PImage closeBtnImage;

	protected Version version, versionToUpdateTo;
	protected File launcher, relauncher, game;
	protected String url = "", mainInfo = "Checking for Update", subInfo = "Loading...";
	protected boolean isDownloadingFiles = false, relaunchNeeded = false;

	/**
	 * NOTICE Complex Stuff
	 * If the version has the Launcher as an update file we only update to that
	 * version for now and once we update everything except the Launcher then we
	 * make a temp file with new Launcher and then run the Relauncher and close the
	 * Launcher<br>
	 * <br>
	 * The Relauncher will delete old Launcher + config and move new Launcher +
	 * config in place and the Run the Launcher again using the name in the new
	 * config<br>
	 * <br>
	 * While copying a file over if we exit we need to make sure to save info on
	 * where we was with update so we can resume at a later time
	 */

	private void compileUpdateFiles() {
		this.subInfo = "Compiling Update List";
		this.progressBar.progress = 0.0f;

		int index = 0;
		for (final VersionFile vf : this.versionList) {
			this.updateList.add(vf);
			this.progressBar.progress = (float) (++index) / this.versionList.size();
		}
	}

	private void downloadUpdateFiles() throws InterruptedException {
		this.mainInfo = "Downloading Update Files";
		this.subInfo = "0 / " + this.updateList.size();
		this.progressBar.progress = 0.0f;
		this.isDownloadingFiles = true;

		int index = 0;
		for (final UpdateData ud : this.updateList) {
			this.progressBar.progress = 0.0f;
			this.subInfo = ++index + " / " + this.updateList.size();

			if (!ud.getAction().equals(FileData.STABLE)) {
				try {
					final String tempPath;
					if (ud.getFile().getName().equals(this.launcher.getName()))
						tempPath = ud.getFile().getPath() + "_dl";
					else
						tempPath = ud.getFile().getPath();

					final String path = tempPath.replaceAll("\\\\", "/");
					final File file = new File(this.sketchPath() + "/" + path);
					if (!file.exists() && !file.getName().contains(".")) file.mkdir();

					if (!file.isDirectory()) {
						final URL url = new URL(this.url + "/" + ud.getVersion() + "/" + path);
						final long fileSizeToDownload = url.openConnection().getContentLengthLong();
						final BufferedInputStream in = new BufferedInputStream(url.openStream());

						final FileOutputStream fos = new FileOutputStream(file);
						byte[] dataBuffer = new byte[1024];
						int bytesRead;
						while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
							fos.write(dataBuffer, 0, bytesRead);
							this.progressBar.progress = ((float) file.length() / fileSizeToDownload);
						}
						in.close();
						fos.close();
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			
			this.progressBar.progress = 1.0f;
		}
		this.isDownloadingFiles = false;
	}

	private void downloadVersionFiles() {
		this.subInfo = "Downloading Update Log";
		this.progressBar.progress = 0.0f;
		try {
			// Read in versions.json
			final URL url = new URL(this.url + "/" + this.versionFile);

			final JSONParser parser = new JSONParser();
			final JSONObject obj = (JSONObject) parser.parse(new InputStreamReader(url.openStream()));

			final JSONArray versionData = (JSONArray) obj.get("versions");

			int index = 0;
			Version lastVersion = null;
			for (final Object o : versionData) {
				this.progressBar.progress = (float) (++index) / versionData.size();
				final Version version = new Version((String) o);

				// If Version is newer than current we need to update to it
				if (!this.relaunchNeeded && this.version.lessThan(version)) {
					final URL versionURL = new URL(this.url + "/" + version + ".json");
					final VersionFile vFile = new VersionFile(version, versionURL);

					// If Launcher gets updated don't update past it
					for (final FileData fd : vFile.getFiles()) {
						if (fd.getAction().equals(FileData.MODIFY)
								&& fd.getFile().getName().equals(this.launcher.getName())) {
							if (Version.debug) System.out.println("Launcher Update Found in: " + version);
							this.relaunchNeeded = true;
						}
						if (fd.getAction().equals(FileData.DELETE)
								&& fd.getFile().getName().equals(this.launcher.getName())) {
							System.err.println("MAJOR ISSUE!!! You Deleted the Launcher in: " + version);
							this.versionToUpdateTo = null;
							return;
						}
					}

					// Add it to the List to be Updated
					this.versionList.add(vFile);
					lastVersion = version;
					if (Version.debug) {
						System.out.println("Update to Version: " + version);
					}
				} else if (this.relaunchNeeded && Version.debugDetail) {
					System.out.println("Skipping Version: " + version);
				} else if (Version.debugDetail) {
					System.out.println("Old Version: " + version);
				}
			}
			this.versionToUpdateTo = lastVersion;
		} catch (final UnknownHostException ex) {
			// DEBUG Temp Only Remove Later
			System.err.println("Trouble Connecting to https://unlishema.org, please check your internet connection!");
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	private void readLauncherConfig() {
		this.version = new Version(this.config.getProperty("version"));
		if (Version.debug) System.out.println("Lanucher Version:\t" + this.version);
		this.url = this.config.getProperty("url");
		if (Version.debug) System.out.println("Update URL:\t" + this.url);
		this.launcher = new File(this.sketchPath() + "\\" + this.config.getProperty("launcher"));
		if (Version.debug) System.out.println("Launcher EXE:\t" + this.launcher);
		this.relauncher = new File(this.sketchPath() + "\\" + this.config.getProperty("relauncher"));
		if (Version.debug) System.out.println("Re-Launcher EXE:\t" + this.relauncher);
		this.game = new File(this.sketchPath() + "\\" + this.config.getProperty("game"));
		if (Version.debug) System.out.println("Game EXE:\t" + this.game);
	}

	/*
	 * TODO zLater On...
	 * Add Ability for Launcher to get Updates in background while the game is
	 * running
	 * Add Ability to Show a Change-Log upon update completion also a Launch Button
	 * maybe???
	 */
	public void updateFiles() throws InterruptedException {
		if (Version.debugDetail) System.out.println("[BEGIN] Updating");

		// Download the Versions.json and store info needed
		this.downloadVersionFiles();
		if (this.versionToUpdateTo == null) {
			// FIXME Throw Error because there is no version to update to
			return;
		}

		// Compile all Version Files into a List of files to update
		this.compileUpdateFiles();
		if (this.updateList.size() == 0) {
			// FIXME Throw Error because there is no files to update
			return;
		}

		// Download the Files that need updated
		this.downloadUpdateFiles();

		// Finish Up
		this.mainInfo = "Finishing Up";

		// Clean Up temp Files and anything else that needs cleaned up
		this.subInfo = "Cleaning Up";
		// FIXME zzLater Make sure we cleanup

		if (this.relaunchNeeded) {
			// Re-launch the Launcher
			this.subInfo = "Re-Launching Launcher";
			this.progressBar.progress = 1.0f;
			// FIXME zzzLater Re-Launch the Updater using another program to update the
			// Launcher if needed and then return
		} else {
			// Launch the Game
			this.subInfo = "Launching Deities Online";
			this.progressBar.progress = 1.0f;
			// FIXME zzzLater Launch Deities Online once all Updates are complete
		}

		if (Version.debugDetail) System.out.println("[FINISH] Updating");
		this.exit();
	}

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#settings()
	 */
	public void settings() {
		this.size(400, 200);
	}

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#initSurface()
	 */
	public PSurface initSurface() {
		final PSurface pSurface = super.initSurface();
		((SmoothCanvas) ((PSurfaceAWT) surface).getNative()).getFrame().setUndecorated(true);
		return pSurface;
	}

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#setup()
	 */
	public void setup() {
		this.background(0);
		this.skh.overrideEscape(true);

		this.backgroundImage = this.loadImage("Launcher.png");
		this.closeBtnImage = this.backgroundImage.get(this.closeBtn.x, this.closeBtn.y, this.closeBtn.width,
				this.closeBtn.height);

		// Setup Default Config
		this.config.setDefault("version", Version.THIS.toString());
		this.config.setDefault("url", "https://unlishema.org/deities-update");
		this.config.setDefault("launcher", this.sketchPath() + "\\GameLauncher.jar");
		this.config.setDefault("relauncher", this.sketchPath() + "\\Relauch.jar");
		this.config.setDefault("game", this.sketchPath() + "\\BasicGame.jar");

		// Read Launcher Config
		this.readLauncherConfig();

		this.thread("updateFiles");
	}

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#draw()
	 */
	public void draw() {
		background(225, 50, 50);

		// Draw Background Image
		noTint();
		image(this.backgroundImage, 0, 0);

		// Highlight & Draw Close Button
		if (this.closeBtn.contains(this.mouseX, this.mouseY))
			tint(200, 200, 150);
		else
			tint(255, 255, 255);
		image(closeBtnImage, this.closeBtn.x, this.closeBtn.y);

		// Draw Loading Info
		fill(225);
		textAlign(PGraphics.LEFT, PGraphics.TOP);
		textSize(20);
		text(this.mainInfo, this.loadingInfoPosition.x, this.loadingInfoPosition.y);
		text(this.subInfo, this.loadingInfoPosition.x, this.loadingInfoPosition.y + 25);

		// Draw Progress Bar
		this.progressBar.draw(this.getGraphics());
	}

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#mousePressed()
	 */
	public void mousePressed() {
		if (this.closeBtn.contains(this.mouseX, this.mouseY)) {
			if (this.isDownloadingFiles) {
				// TODO Write to the Config about files already Downloaded
			}

			// TODO Make sure we cleanup before closing and take into account the info
			// above and below this

			if (Version.debugDetail) System.out.println("Closing Game Launcher");
			this.exit();
		}
	}

	/**
	 * Main Execution for running outside of Processing
	 * 
	 * @param args The Arguments for the Program
	 */
	public static void main(final String[] args) {
		if (args.length > 0) {
			for (final String arg : args) {
				if (arg.equals("-debug")) Version.debug = true;
				if (arg.equals("-detail") && Version.debug) Version.debugDetail = true;
			}
		}

		PApplet.main("GameLauncher");
	}
}
