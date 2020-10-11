import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
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

	private final ArrayList<VersionFile> versionUpdateList = new ArrayList<VersionFile>();

	private final Rectangle closeBtn = new Rectangle(363, 8, 27, 27);
	private final ProgressBar progressBar = new ProgressBar(50, 125, 300, 50);
	private final Point loadingInfoPosition = new Point(50, 65);

	private final String versionFile = "versions.json";

	private PImage backgroundImage;
	private PImage closeBtnImage;

	protected VersionFile compiledUpdate = null;
	protected Version version;
	protected File launcher, relauncher;
	protected String url = "", mainInfo = "Checking for Update", subInfo = "Loading...";
	protected boolean relaunchNeeded = false;

	private void compileUpdateFiles() throws InterruptedException {
		this.mainInfo = "Compiling Update List";
		this.subInfo = "0/0 (0/0)";
		this.progressBar.progress = 0.0f;

		/**
		 * FIXME Complex Stuff
		 * If the version has the Launcher as an update file we only update to that
		 * version for now and once we update everything except the Launcher then we
		 * make a temp file with new Launcher and then run the Relauncher and close the
		 * Launcher<br>
		 * <br>
		 * The Relauncher will delete old Launcher + config and move new Launcher +
		 * config in place and the Run the Launcher again using the name in the new
		 * config<br>
		 * <br>
		 * If file was create in 1 update and then deleted in another later one, we can
		 * skip downloading it at all<br>
		 * <br>
		 * If a file was modifier and then deleted later we can skip downloading it at
		 * all<br>
		 * <br>
		 * If a file was deleted and then created make sure we can just update the
		 * file instead of deleting and then creating<br>
		 * <br>
		 * While copying a file over if we exit we need to make sure to save info on
		 * where we was with update so we can resume at a later time
		 * 
		 */

		// TODO Determine which Files from each version is needed to update game
		// Check for all the Complex stuff Above
	}

	private void downloadVersionFiles() {
		this.subInfo = "Downloading Update Log";
		this.progressBar.progress = 0.0f;
		try {
			// Read in versions.json
			final URL url = new URL(this.url + "/" + this.versionFile);

			final JSONParser parser = new JSONParser();
			final JSONObject obj = (JSONObject) parser.parse(new InputStreamReader(url.openStream()));

			final JSONArray fileData = (JSONArray) obj.get("versions");

			int index = 0;
			Version lastVersion = new Version(0, 0, 0);
			for (final Object o : fileData) {
				this.progressBar.progress = ++index / fileData.size();
				final Version version = new Version((String) o);

				// If Version is newer than current we need to update to it
				if (!this.relaunchNeeded && this.version.lessThan(version)) {
					final URL versionURL = new URL(this.url + "/" + version + ".json");
					final VersionFile vFile = new VersionFile(version, versionURL);

					// If Launcher gets updated don't update past it
					for (final FileData fd : vFile.getFiles()) {
						if (fd.getAction().equals(FileData.MODIFY)
								&& fd.getFile().getName().equals(this.launcher.getName())) {
							System.out.println("Launcher Update Found in: " + version);
							this.relaunchNeeded = true;
						}
					}

					// Add it to the List to be Updated
					this.versionUpdateList.add(vFile);
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
			this.compiledUpdate = new VersionFile(lastVersion);
		} catch (Exception ex) {
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
	}

	// TODO Way Later On... Add Ability for Launcher to get Updates in background
	// while the game is running
	public void updateFiles() throws InterruptedException {
		// DEBUG Remove Later
		System.out.println("[BEGIN] Updating");

		/**
		 * FIXME Complex Stuff
		 * If the version has the Launcher as an update file we only update to that
		 * version for now and once we update everything except the Launcher then we
		 * make a temp file with new Launcher and then run the Relauncher and close the
		 * Launcher<br>
		 * <br>
		 * The Relauncher will delete old Launcher + config and move new Launcher +
		 * config in place and the Run the Launcher again using the name in the new
		 * config<br>
		 * <br>
		 * If file was create in 1 update and then deleted in another later one, we can
		 * skip downloading it at all<br>
		 * <br>
		 * If a file was modifier and then deleted later we can skip downloading it at
		 * all<br>
		 * <br>
		 * If a file was deleted and then created make sure we delete and then create a
		 * new file to make sure it updates<br>
		 * <br>
		 * While copying a file over if we exit we need to make sure to save info on
		 * where we was with update so we can resume at a later time
		 * 
		 */

		// Download Version Files
		this.downloadVersionFiles();

		if (this.compiledUpdate != null) {
			// FIXME Compile Update Files
			this.compileUpdateFiles();

			// Download Update Files
			this.mainInfo = "Downloading Update Files";
			this.subInfo = "(0/0)";
			this.progressBar.progress = 0.0f;
			// TODO Download the files that are required to update to the latest version
			// into a temp dir
			// Info for Downloading Files https://www.baeldung.com/java-download-file
			Thread.sleep(100);

			// Copy Update Files
			this.mainInfo = "Copying Update Files";
			this.subInfo = "(0/0)";
			this.progressBar.progress = 0.0f;
			// TODO Copy all the files to the correct dir
			// Make sure to account for the Complex Stuff Above
			Thread.sleep(100);

			// Update Version Info in Config
			this.mainInfo = "Finishing Up";
			this.subInfo = "Updating Version Info";
			this.progressBar.progress = 0.0f;
			this.config.setProperty("version", this.compiledUpdate.version.toString());
			this.config.write("launcher.config");

			if (this.relaunchNeeded) {
				// Re-launch the Launcher
				this.subInfo = "Re-Launching Launcher";
				this.progressBar.progress = 0.0f;
				// FIXME Re-Launch the Updater using another program to update the Launcher if
				// needed and then return
			} else {
				// Launch the Game
				this.subInfo = "Launching Deities Online";
				this.progressBar.progress = 1.0f;
				// FIXME Launch Deities Online once all Updates are complete
			}
		}

		// DEBUG Remove Later
		System.out.println("[FINISH] Updating");
	}

	public void settings() {
		size(400, 200);
	}

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#initSurface()
	 */
	public PSurface initSurface() {
		PSurface pSurface = super.initSurface();
		((SmoothCanvas) ((PSurfaceAWT) surface).getNative()).getFrame().setUndecorated(true);
		return pSurface;
	}

	/*
	 * (non-Javadoc)
	 * @see processing.core.PApplet#setup()
	 */
	public void setup() {
		background(0);
		this.skh.overrideEscape(true);

		this.backgroundImage = this.loadImage("Launcher.png");
		this.closeBtnImage = this.backgroundImage.get(this.closeBtn.x, this.closeBtn.y, this.closeBtn.width,
				this.closeBtn.height);

		// Setup Default Config
		this.config.setDefault("version", Version.THIS.toString());
		this.config.setDefault("url", "https://unlishema.org/deities-update");
		this.config.setDefault("launcher", this.sketchPath() + "\\GameLauncher.jar");
		this.config.setDefault("relauncher", this.sketchPath() + "\\Relauch.jar");

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
			System.out.println("Close Clicked");
			// FIXME Make sure we cleanup before closing
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
