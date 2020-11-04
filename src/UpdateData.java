import java.io.File;

public class UpdateData extends FileData {

	private final Version version;

	public UpdateData(final Version version, final String action, final File file, final long lastModified) {
		super(action, file, lastModified);
		this.version = version;
	}

	public UpdateData(final Version version, final FileData fileData) {
		this(version, fileData.getAction(), fileData.getFile(), fileData.getLastModified());
	}

	public Version getVersion() {
		return this.version;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.getVersion().toString());
		sb.append(" ");
		sb.append(super.toString());
		return sb.toString();
	}
}
