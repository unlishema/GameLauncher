
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config extends Properties {

	private static final long serialVersionUID = 1L;

	public final File file;

	public Config(final String filePath) {
		this.file = new File(filePath);
		this.read();
	}

	public void setDefault(final String key, final String value) {
		if (this.getProperty(key) == null) {
			this.setProperty(key, value);
		}
	}

	public void createIfNotExists() {
		if (!this.file.exists()) {
			try {
				this.file.createNewFile();
				this.write();
			} catch (final Exception e) {
				System.err.println(e);
			}
		}
	}

	public void read() {
		InputStream fileIn = null;

		try {
			fileIn = new FileInputStream(this.file);
			this.load(fileIn);
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (fileIn != null) {
				try {
					fileIn.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void write() {
		FileOutputStream fileOut = null;

		try {
			fileOut = new FileOutputStream(this.file);
			this.store(fileOut, "Config");
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}