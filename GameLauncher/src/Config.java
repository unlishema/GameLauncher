

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config extends Properties {

	private static final long serialVersionUID = 1L;

	public Config() {

	}

	public Config(final String fileName) {
		this.read(fileName);
	}
	
	public void setDefault(final String key, final String value) {
		if(this.getProperty(key) == null) {
			this.setProperty(key, value);
		}
	}
	
	public void createIfNotExists(final String fileName) {
		final File file = new File(fileName);
		if(!file.exists()) {
			try {
				file.createNewFile();
				this.write(fileName);
			} catch (final Exception e) {
				System.err.println(e);
			}
		}
	}

	public void read(final String fileName) {
		InputStream fileIn = null;

		try {
			fileIn = new FileInputStream(new File(fileName));
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

	public void write(final String fileName) {
		FileOutputStream fileOut = null;

		try {
			fileOut = new FileOutputStream(new File(fileName));
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