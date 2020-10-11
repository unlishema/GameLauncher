

import java.io.File;
import java.text.SimpleDateFormat;

public class FileData {

	public static final String CREATE = "create";
	public static final String MODIFY = "modify";
	public static final String DELETE = "delete";
	public static final String STABLE = "stable";
	
	private final String action;
	private final File file;
	private final long lastModified;
	
	public FileData(final String action, final File file) {
		this.action = action;
		this.file = file;
		this.lastModified = this.file.lastModified();
	}
	
	public FileData(final String action, final File file, final long lastModified) {
		this.action = action;
		this.file = file;
		this.lastModified = lastModified;
	}
	
	public String getAction() {
		return this.action;
	}
	
	public File getFile() {
		return this.file;
	}
	
	public long getLastModified() {
		return this.lastModified;
	}
	
	public String getLastModifiedFormatted() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		return sdf.format(this.lastModified);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();

		sb.append(this.getAction().toUpperCase());
		sb.append(" [");
		sb.append(this.getLastModifiedFormatted());
		sb.append("] ");
		sb.append(this.getFile());
		
		return sb.toString();
	}
}